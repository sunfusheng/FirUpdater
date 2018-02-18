package com.sunfusheng;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.widget.TextView;

import java.lang.reflect.Field;

/**
 * @author sunfusheng on 2018/2/17.
 */
public class FirDialog {

    private ProgressDialog progressDialog;

    private OnClickDownloadDialogListener onClickDownloadDialogListener;

    public void showAppInfoDialog(Context context, String title, String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(context)
                .setTitle(title + "更新提示")
                .setMessage(message)
                .setPositiveButton("立即更新", (dialog, which) -> {
                    if (onClickDownloadDialogListener != null) {
                        onClickDownloadDialogListener.onClickDownload(dialog);
                    }
                })
                .setNegativeButton("稍后", (dialog, which) -> {
                })
                .create();
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#333333"));
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#9a9a9a"));

        try {
            Field alert = AlertDialog.class.getDeclaredField("mAlert");
            alert.setAccessible(true);
            Object alertController = alert.get(alertDialog);
            Field messageView = alertController.getClass().getDeclaredField("mMessageView");
            messageView.setAccessible(true);
            TextView textView = (TextView) messageView.get(alertController);
            textView.setTextColor(Color.parseColor("#9a9a9a"));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public void showDownloadDialog(Context context, int progress) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(context);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setMax(100);
            progressDialog.setTitle("正在下载");
            progressDialog.setButton(DialogInterface.BUTTON_POSITIVE, "后台下载", (dialog, which) -> {
                if (onClickDownloadDialogListener != null) {
                    onClickDownloadDialogListener.onClickBackgroundDownload(dialog);
                }
            });
            progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消", (dialog, which) -> {
                if (onClickDownloadDialogListener != null) {
                    onClickDownloadDialogListener.onClickCancelDownload(dialog);
                }
            });
            progressDialog.show();
        }

        if (progressDialog.isShowing()) {
            progressDialog.setProgress(progress);
        }
    }

    public void dismissDownloadDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    public void setOnClickDownloadDialogListener(OnClickDownloadDialogListener onClickDownloadDialogListener) {
        this.onClickDownloadDialogListener = onClickDownloadDialogListener;
    }

    public interface OnClickDownloadDialogListener {
        void onClickDownload(DialogInterface dialog);

        void onClickBackgroundDownload(DialogInterface dialog);

        void onClickCancelDownload(DialogInterface dialog);
    }

}
