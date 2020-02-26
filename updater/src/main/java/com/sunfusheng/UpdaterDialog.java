package com.sunfusheng;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.qw.soul.permission.SoulPermission;

import java.lang.reflect.Field;

/**
 * @author sunfusheng
 * @since 2019-09-11
 */
class UpdaterDialog {
    private ProgressDialog mProgressDialog;
    private OnClickDownloadDialogListener onClickDownloadDialogListener;

    void showUpdateDialog(AppInfo appInfo) {
        Activity topActivity = SoulPermission.getInstance().getTopActivity();
        if (topActivity == null) {
            Log.e("FirUpdater", "topActivity = null");
            return;
        }

        StringBuilder msg = new StringBuilder();
        msg.append("名称：").append(appInfo.name);
        msg.append("\n版本：V").append(appInfo.versionShort);
        msg.append("\n大小：").append(UpdaterUtil.getFileSizeDesc(appInfo.binary.fsize));
        if (!TextUtils.isEmpty(appInfo.changelog)) {
            msg.append("\n\n更新日志：").append(appInfo.changelog);
        }

        AlertDialog alertDialog = new AlertDialog.Builder(topActivity)
                .setCancelable(false)
                .setTitle("应用更新提示")
                .setMessage(msg)
                .setPositiveButton("立即更新", (dialog, which) -> {
                    if (onClickDownloadDialogListener != null) {
                        onClickDownloadDialogListener.onClickDownload();
                    }
                })
                .setNegativeButton("稍后", (dialog, which) -> {
                })
                .create();
        alertDialog.show();

        try {
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#333333"));
            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#9a9a9a"));

            Field alert = AlertDialog.class.getDeclaredField("mAlert");
            alert.setAccessible(true);
            Object alertController = alert.get(alertDialog);
            Field messageView = alertController.getClass().getDeclaredField("mMessageView");
            messageView.setAccessible(true);
            TextView textView = (TextView) messageView.get(alertController);
            textView.setTextSize(13);
            textView.setTextColor(Color.parseColor("#9a9a9a"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void showDownloadDialog() {
        Activity topActivity = SoulPermission.getInstance().getTopActivity();
        if (topActivity == null) {
            Log.e("FirUpdater", "topActivity = null");
            return;
        }

        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(topActivity);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.setMax(100);
            mProgressDialog.setTitle("正在下载");
            mProgressDialog.setButton(DialogInterface.BUTTON_POSITIVE, "后台下载", (dialog, which) -> {
                if (onClickDownloadDialogListener != null) {
                    onClickDownloadDialogListener.onClickBackground();
                }
            });
            mProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消", (dialog, which) -> {
                if (onClickDownloadDialogListener != null) {
                    onClickDownloadDialogListener.onClickCancel();
                }
            });

        }

        if (!mProgressDialog.isShowing()) {
            mProgressDialog.setProgress(0);
            mProgressDialog.show();
        }

        mProgressDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#333333"));
        mProgressDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#9a9a9a"));
    }

    void setDownloadProgress(int progress) {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.setProgress(progress);
        }
    }

    void dismissDownloadDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    void setOnClickDownloadDialogListener(OnClickDownloadDialogListener listener) {
        this.onClickDownloadDialogListener = listener;
    }

    interface OnClickDownloadDialogListener {
        void onClickDownload();

        void onClickBackground();

        void onClickCancel();
    }
}
