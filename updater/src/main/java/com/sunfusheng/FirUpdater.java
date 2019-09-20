package com.sunfusheng;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.sunfusheng.download.IDownloadListener;

import java.io.File;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * @author by sunfusheng on 2019-08-07
 */
public class FirUpdater {
    private static final String TAG = FirUpdater.class.getCanonicalName();
    private volatile static FirUpdater mInstance;
    private Context mContext;
    private String mApiToken;
    private String mAppId;
    private String mApkPath;
    private String mApkName;

    private AppInfo mAppInfo;
    private UpdaterDialog mDialog;
    private UpdaterDownloader mDownloader;
    private int mCurrProgress;
    private UpdaterNotification mNotification;

    public static FirUpdater getInstance(Context context) {
        if (mInstance == null) {
            synchronized (FirUpdater.class) {
                if (mInstance == null) {
                    mInstance = new FirUpdater(context);
                }
            }
        }
        return mInstance;
    }

    private FirUpdater(Context context) {
        this.mContext = context.getApplicationContext();
    }

    public FirUpdater apiToken(String apiToken) {
        this.mApiToken = apiToken;
        return this;
    }

    public FirUpdater appId(String appId) {
        this.mAppId = appId;
        return this;
    }

    public FirUpdater apkPath(String apkPath) {
        this.mApkPath = apkPath;
        return this;
    }

    public FirUpdater apkName(String apkName) {
        this.mApkName = apkName;
        return this;
    }

    public void checkVersion() {
        if (TextUtils.isEmpty(mApiToken) || TextUtils.isEmpty(mAppId)) {
            Toast.makeText(mContext, "请设置 ApiToken 和 AppId.", Toast.LENGTH_SHORT).show();
            return;
        }

        Disposable disposable = UpdaterApi.getUpdaterService().fetchAppInfo(mAppId, mApiToken)
                .subscribeOn(Schedulers.io())
                .filter(it -> it != null && !TextUtils.isEmpty(it.installUrl))
                .filter(it -> it.binary != null && it.binary.fsize > 0)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(it -> {
                    mAppInfo = it;
                    Log.d(TAG, it.toString());
                    int remoteVersionCode = !TextUtils.isEmpty(it.version) ? Integer.parseInt(it.version) : -1;
                    int localVersionCode = UpdaterUtil.getVersionCode(mContext);
                    if (remoteVersionCode <= localVersionCode) {
                        Log.d(TAG, "当前已是最新版本");
                        return;
                    }

                    mDialog = new UpdaterDialog();
                    mDialog.showUpdateDialog(it);
                    mDialog.setOnClickDownloadDialogListener(new UpdaterDialog.OnClickDownloadDialogListener() {
                        @Override
                        public void onClickDownload() {
                            download();
                        }

                        @Override
                        public void onClickBackground() {
                            mDialog.dismissDownloadDialog();
                            mNotification = new UpdaterNotification(mContext, mAppInfo.name);
                            mNotification.setProgress(mCurrProgress);
                        }

                        @Override
                        public void onClickCancel() {
                            mDialog.dismissDownloadDialog();
                            mDownloader.cancel();
                        }
                    });
                }, Throwable::printStackTrace);
    }

    private void download() {
        if (TextUtils.isEmpty(mApkName)) {
            mApkName = mAppInfo.name + "-V" + mAppInfo.versionShort + ".apk";
        }
        if (TextUtils.isEmpty(mApkPath)) {
            mApkPath = Environment.getExternalStorageDirectory() + File.separator;
        }
        String apkPathName = mApkPath + mApkName;

        mDialog.showDownloadDialog();
        mDownloader = new UpdaterDownloader();
        mDownloader.download(mAppInfo.install_url, apkPathName, new IDownloadListener() {
            @Override
            public void onStart() {
            }

            @Override
            public void onProgress(long bytesTransferred, long totalBytes, int percentage) {
                mCurrProgress = percentage;
                mDialog.setDownloadProgress(percentage);
                if (mNotification != null) {
                    mNotification.setProgress(percentage);
                }
            }

            @Override
            public void onSuccess(File file) {
                mDialog.dismissDownloadDialog();
                if (mNotification != null) {
                    mNotification.cancel();
                }
                UpdaterUtil.installApk(mContext, apkPathName);
            }

            @Override
            public void onError(Throwable e) {
                mDialog.dismissDownloadDialog();
                if (mNotification != null) {
                    mNotification.cancel();
                }
                Toast.makeText(mContext, "下载失败，请稍后重试！", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
