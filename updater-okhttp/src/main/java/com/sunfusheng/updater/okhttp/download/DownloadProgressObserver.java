package com.sunfusheng.updater.okhttp.download;

import android.os.Handler;
import android.os.Looper;

import java.io.File;
import java.io.InputStream;
import java.lang.ref.WeakReference;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * @author sunfusheng
 * @since 2019-09-16
 */
public class DownloadProgressObserver implements Observer<InputStream> {
    private static final Handler mMainThreadHandler = new Handler(Looper.getMainLooper());
    private WeakReference<Disposable> mDisposableWeakReference;
    private String mFilePath;
    private IDownloadListener mDownloadListener;

    public DownloadProgressObserver(String filePath, IDownloadListener downloadListener) {
        this.mFilePath = filePath;
        this.mDownloadListener = downloadListener;
    }

    @Override
    public void onSubscribe(Disposable disposable) {
        mDisposableWeakReference = new WeakReference<>(disposable);
        if (mDownloadListener != null) {
            mMainThreadHandler.post(() -> mDownloadListener.onStart());
        }
    }

    @Override
    public void onNext(InputStream inputStream) {
    }

    @Override
    public void onError(Throwable e) {
        dispose();
        if (mDownloadListener != null) {
            mMainThreadHandler.post(() -> mDownloadListener.onError(e));
        }
    }

    @Override
    public void onComplete() {
        dispose();
        if (mDownloadListener != null) {
            mMainThreadHandler.post(() -> mDownloadListener.onSuccess(new File(mFilePath)));
        }
    }

    public void dispose() {
        if (mDisposableWeakReference != null) {
            Disposable disposable = mDisposableWeakReference.get();
            if (disposable != null && !disposable.isDisposed()) {
                disposable.dispose();
            }
            mDisposableWeakReference = null;
        }
    }
}
