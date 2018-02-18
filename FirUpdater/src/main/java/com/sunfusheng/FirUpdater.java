package com.sunfusheng;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.util.Log;

import java.io.File;

/**
 * @author sunfusheng on 2018/2/17.
 */
public class FirUpdater implements FirDialog.OnClickDownloadDialogListener {

    public String API_TOKEN;
    public String APP_ID;
    private String appVersionUrl;

    private Context context;
    private FirDialog firDialog;
    private FirDownloader firDownloader;
    private FirNotification firNotification;

    private boolean isBackgroundDownload;

    public FirUpdater(Context context, String apiToken, String appId) {
        this.context = context;
        this.API_TOKEN = apiToken;
        this.APP_ID = appId;
        this.appVersionUrl = "http://api.fir.im/apps/latest/" + appId + "?api_token=" + apiToken;

        this.firDialog = new FirDialog();
        this.firDialog.setOnClickDownloadDialogListener(this);
    }

    public void checkVersion() {
        new Thread(() -> {
            boolean result = new FirAppInfo().requestAppInfo(appVersionUrl);
            if (result) {
                FirUpdaterUtils.runOnMainThread(() -> {
                    firDialog.showAppInfoDialog(context, FirAppInfo.appName, FirAppInfo.appChangeLog);
                });
            }
        }).start();
    }

    @Override
    public void onClickDownload(DialogInterface dialog) {
        String fileName = FirAppInfo.appName + "-V" + FirAppInfo.appVersionName + ".apk";
        String filePath = Environment.getExternalStorageDirectory() + File.separator + fileName;

        firNotification = new FirNotification();
        firNotification.createBuilder(context);
        firNotification.setContentTitle("正在下载" + FirAppInfo.appName);

        firDownloader = new FirDownloader(context, FirAppInfo.appInstallUrl, filePath);
        firDownloader.setOnDownLoadListener(new FirDownloader.OnDownLoadListener() {
            @Override
            public void onProgress(int totalLength, int currLength, int progress) {
                Log.d("--->", "progress: " + progress + " totalLength: " + totalLength + " currLength: " + currLength);
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

    @Override
    public void onClickBackgroundDownload(DialogInterface dialog) {
        isBackgroundDownload = true;
    }

    @Override
    public void onClickCancelDownload(DialogInterface dialog) {
        firDownloader.cancel();
    }
}
