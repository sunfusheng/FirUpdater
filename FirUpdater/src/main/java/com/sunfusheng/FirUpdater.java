package com.sunfusheng;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.text.TextUtils;

import java.io.File;

/**
 * @author sunfusheng on 2018/2/17.
 */
public class FirUpdater {

    private Context context;
    private String appVersionUrl;

    private FirAppInfo.AppInfo appInfo;
    private String apkName;
    private String apkPath;
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

    public FirUpdater setApkName(String apkName) {
        this.apkName = apkName;
        return this;
    }

    public FirUpdater setApkPath(String apkPath) {
        this.apkPath = apkPath;
        return this;
    }

    public void checkVersion() {
        new Thread(() -> {
            appInfo = new FirAppInfo().requestAppInfo(appVersionUrl);
            if (appInfo == null) {
                return;
            }

            boolean needUpdate = appInfo.appVersionCode > FirUpdaterUtils.getVersionCode(context);
            if (forceShowDialog || needUpdate) {
                FirUpdaterUtils.runOnMainThread(this::initFirDialog);
            }
        }).start();
    }

    private void initFirDialog() {
        firDialog = new FirDialog();
        firDialog.showAppInfoDialog(context, appInfo);
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
        if (TextUtils.isEmpty(apkName)) {
            apkName = appInfo.appName + "-V" + appInfo.appVersionName + ".apk";
        }
        if (TextUtils.isEmpty(apkPath)) {
            apkPath = Environment.getExternalStorageDirectory() + File.separator + apkName;
        }

        firNotification = new FirNotification();
        firNotification.createBuilder(context);
        firNotification.setContentTitle("正在下载" + appInfo.appName);

        firDownloader = new FirDownloader(context, appInfo.appInstallUrl, apkPath);
        firDownloader.setOnDownLoadListener(new FirDownloader.OnDownLoadListener() {
            @Override
            public void onProgress(int progress) {
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
                FirUpdaterUtils.installApk(context, apkPath);
            }

            @Override
            public void onError() {

            }
        });
        firDownloader.downLoad();
    }

    public FirDialog getFirDialog() {
        return firDialog;
    }

    public FirNotification getFirNotification() {
        return firNotification;
    }
}
