package com.sunfusheng.updater.okhttp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

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

        int remoteVersionCode = !TextUtils.isEmpty(appInfo.version) ? Integer.parseInt(appInfo.version) : 0;
        int localVersionCode = UpdaterUtil.getVersionCode(topActivity);
        if (remoteVersionCode <= localVersionCode) {
            Log.d("FirUpdater", "当前已是最新版本");
//            return;
        }

        StringBuilder msg = new StringBuilder();
        msg.append("名称：").append(appInfo.name);
        msg.append("\n版本：").append("V" + appInfo.versionShort);
        msg.append("\n大小：").append(UpdaterUtil.getMeasureSize(appInfo.binary.fsize));
        if (!TextUtils.isEmpty(appInfo.changelog)) {
            msg.append("\n\n更新日志：").append(appInfo.changelog);
        }

        AlertDialog alertDialog = new AlertDialog.Builder(topActivity)
                .setCancelable(false)
                .setTitle("应用更新提示")
                .setMessage(msg)
                .setPositiveButton("立即更新", (dialog, which) -> {
                    if (onClickDownloadDialogListener != null) {
                        onClickDownloadDialogListener.onClickDownload(dialog);
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

    public void showDownloadDialog(Context context, int progress) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(context);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.setMax(100);
            mProgressDialog.setTitle("正在下载");
            mProgressDialog.setButton(DialogInterface.BUTTON_POSITIVE, "后台下载", (dialog, which) -> {
                if (onClickDownloadDialogListener != null) {
                    onClickDownloadDialogListener.onClickBackgroundDownload(dialog);
                }
            });
            mProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消", (dialog, which) -> {
                if (onClickDownloadDialogListener != null) {
                    onClickDownloadDialogListener.onClickCancelDownload(dialog);
                }
            });
            mProgressDialog.show();

            mProgressDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#333333"));
            mProgressDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#9a9a9a"));
        }

        if (mProgressDialog.isShowing()) {
            mProgressDialog.setProgress(progress);
        }
    }

    public void dismissDownloadDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
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
