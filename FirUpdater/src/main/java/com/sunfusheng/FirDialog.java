package com.sunfusheng;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.widget.TextView;

import java.lang.reflect.Field;

/**
 * @author sunfusheng on 2018/2/17.
 */
public class FirDialog {

    private AlertDialog alertDialog;
    private ProgressDialog progressDialog;

    private OnClickDownloadDialogListener onClickDownloadDialogListener;

    public void showAppInfoDialog(Context context, FirAppInfo.AppInfo appInfo) {
        if (alertDialog == null) {
            StringBuilder sb = new StringBuilder();
            sb.append("名称：").append(appInfo.appName);
            sb.append("\n版本：").append(appInfo.appVersionName);
            sb.append("\n文件大小：").append(FirUpdaterUtils.getMeasureSize(appInfo.appSize));
            if (!TextUtils.isEmpty(appInfo.appChangeLog)) {
                sb.append("\n\n更新日志：").append(appInfo.appChangeLog);
            }

            alertDialog = new AlertDialog.Builder(context)
                    .setCancelable(false)
                    .setTitle("应用更新提示")
                    .setMessage(sb)
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
                textView.setTextSize(13);
                textView.setTextColor(Color.parseColor("#9a9a9a"));
            } catch (IllegalAccessException e) {
                FirUpdaterUtils.loggerError(e);
            } catch (NoSuchFieldException e) {
                FirUpdaterUtils.loggerError(e);
            }
        }
    }

    public void showDownloadDialog(Context context, int progress) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(context);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
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

            progressDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#333333"));
            progressDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#9a9a9a"));
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
