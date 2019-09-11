package com.sunfusheng.updater.okhttp;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * @author by sunfusheng on 2019-08-07
 */
public class FirUpdater {
    private volatile static FirUpdater mInstance;
    private Context mContext;
    private String mApiToken;
    private String mAppId;
    private String mApkPath;
    private UpdaterDialog mUpdaterDialog;

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
        if (context == null) {
            throw new NullPointerException("context = null");
        }
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
                    mUpdaterDialog = new UpdaterDialog();
                    mUpdaterDialog.showUpdateDialog(it);
                }, Throwable::printStackTrace);
    }

}
