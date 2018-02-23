package com.sunfusheng;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.text.TextUtils;
import android.widget.Toast;

import java.io.File;

/**
 * @author sunfusheng on 2018/2/17.
 */
public class FirUpdater {

    private Context context;
    private String apiToken;
    private String appId;
    private String appVersionUrl;
    private String apkPath;
    private FirAppInfo.AppInfo appInfo;
    private boolean isBackgroundDownload = false;
    private boolean forceShowDialog = false;

    private FirDialog firDialog;
    private FirDownloader firDownloader;
    private FirNotification firNotification;

    public FirUpdater(Context context) {
        this(context, null, null);
    }

    public FirUpdater(Context context, String apiToken, String appId) {
        this.context = context;
        this.apiToken = apiToken;
        this.appId = appId;
    }

    public FirUpdater apiToken(String apiToken) {
        this.apiToken = apiToken;
        return this;
    }

    public FirUpdater appId(String appId) {
        this.appId = appId;
        return this;
    }

    public FirUpdater apkPath(String apkPath) {
        this.apkPath = apkPath;
        return this;
    }

    public FirUpdater forceShowDialog(boolean enable) {
        this.forceShowDialog = enable;
        return this;
    }

    public void checkVersion() {
        if (TextUtils.isEmpty(apiToken) || TextUtils.isEmpty(appId)) {
            Toast.makeText(context, "请设置 API TOKEN && APP ID", Toast.LENGTH_LONG).show();
            return;
        }

        this.appVersionUrl = "http://api.fir.im/apps/latest/" + appId + "?api_token=" + apiToken;

        if (firDownloader != null && firDownloader.isGoOn()) {
            Toast.makeText(context, "正在下载【" + appInfo.apkName + "】，请稍后", Toast.LENGTH_LONG).show();
            return;
        }

        FirPermissionHelper.getInstant().requestPermission(context, new FirPermissionHelper.OnPermissionCallback() {
            @Override
            public void onGranted() {
                requestAppInfo();
            }

            @Override
            public void onDenied() {
                FirUpdaterUtils.loggerError("申请权限未通过");
            }
        });
    }

    private void requestAppInfo() {
        new Thread(() -> {
            appInfo = new FirAppInfo().requestAppInfo(appVersionUrl);
            if (appInfo == null) {
                return;
            }

            String apkName = appInfo.appName + "-" + appInfo.appVersionName + ".apk";
            if (TextUtils.isEmpty(apkPath)) {
                apkPath = Environment.getExternalStorageDirectory() + File.separator;
            }


            appInfo.appId = appId;
            appInfo.apkName = apkName;
            appInfo.apkPath = apkPath;
            appInfo.apkLocalUrl = apkPath + apkName;
            FirUpdaterUtils.logger(appInfo.toString());

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
        File apkFile = new File(appInfo.apkLocalUrl);
        if (apkFile.exists()) {
            FirUpdaterUtils.installApk(context, appInfo.apkLocalUrl);
            return;
        }

        firNotification = new FirNotification();
        firNotification.createBuilder(context);
        firNotification.setContentTitle("正在下载" + appInfo.appName);

        firDownloader = new FirDownloader(context.getApplicationContext(), appInfo);
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
                FirUpdaterUtils.installApk(context, appInfo.apkLocalUrl);
            }

            @Override
            public void onError() {

            }
        });
        firDownloader.downloadApk();
    }

    public FirDialog getFirDialog() {
        return firDialog;
    }

    public FirNotification getFirNotification() {
        return firNotification;
    }
}
