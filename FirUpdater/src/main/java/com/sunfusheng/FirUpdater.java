package com.sunfusheng;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;

import java.io.File;

/**
 * @author sunfusheng on 2018/2/17.
 */
public class FirUpdater {

    private Context context;
    private String appVersionUrl;

    private FirAppInfo.AppInfo appInfo;
    private boolean isBackgroundDownload;
    private boolean forceShowDialog;

    private FirDialog firDialog;
    private FirDownloader firDownloader;
    private FirNotification firNotification;

    public FirUpdater(Context context, String apiToken, String appId) {
        this.context = context;
        this.appVersionUrl = "http://api.fir.im/apps/latest/" + appId + "?api_token=" + apiToken;
    }

    public FirUpdater enableForceShowDialog(boolean enable) {
        this.forceShowDialog = enable;
        return this;
    }


    public void checkVersion() {
        new Thread(() -> {
            appInfo = new FirAppInfo().requestAppInfo(appVersionUrl);
            boolean needUpdate = appInfo.appVersionCode > FirUpdaterUtils.getVersionCode(context);

            FirUpdaterUtils.runOnMainThread(() -> {
                if (appInfo != null && (forceShowDialog || needUpdate)) {
                    initFirDialog();
                }
            });
        }).start();
    }

    private void initFirDialog() {
        firDialog = new FirDialog();
        firDialog.showAppInfoDialog(context, appInfo.appName, appInfo.appChangeLog);
        firDialog.setOnClickDownloadDialogListener(new FirDialog.OnClickDownloadDialogListener() {
            @Override
            public void onClickDownload(DialogInterface dialog) {
                downloadApk();
            }

            @Override
            public void onClickBackgroundDownload(DialogInterface dialog) {
                isBackgroundDownload = true;
            }

            @Override
            public void onClickCancelDownload(DialogInterface dialog) {
                firDownloader.cancel();
            }
        });
    }

    private void downloadApk() {
        String fileName = appInfo.appName + "-V" + appInfo.appVersionName + ".apk";
        String filePath = Environment.getExternalStorageDirectory() + File.separator + fileName;

        firNotification = new FirNotification();
        firNotification.createBuilder(context);
        firNotification.setContentTitle("正在下载" + appInfo.appName);

        firDownloader = new FirDownloader(context, appInfo.appInstallUrl, filePath);
        firDownloader.setOnDownLoadListener(new FirDownloader.OnDownLoadListener() {
            @Override
            public void onProgress(int totalLength, int currLength, int progress) {
                firDialog.showDownloadDialog(context, progress);

                if (isBackgroundDownload) {
                    firNotification.setContentText(progress + "%");
                    firNotification.notifyNotification(progress);
                }
            }

            @Override
            public void onSuccess() {
                firDialog.dismissDownloadDialog();
                if (isBackgroundDownload) {
                    firNotification.cancel();
                }
                FirUpdaterUtils.installApk(context, filePath);
            }

            @Override
            public void onError() {

            }
        });
        firDownloader.downLoad();
    }

}
