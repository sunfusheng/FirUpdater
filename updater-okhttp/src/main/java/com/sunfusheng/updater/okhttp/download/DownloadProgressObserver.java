package com.sunfusheng.updater.okhttp.download;

import java.io.File;
import java.io.InputStream;
import java.lang.ref.WeakReference;

import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

/**
 * @author sunfusheng
 * @since 2019-09-16
 */
public class DownloadProgressObserver implements Observer<InputStream> {
    private WeakReference<Disposable> mDisposableWeakReference;
    private Scheduler.Worker mMainThreadWorker;
    private String mFilePath;
    private IDownloadListener mDownloadListener;

    public DownloadProgressObserver(String filePath, IDownloadListener downloadListener) {
        this.mMainThreadWorker = AndroidSchedulers.mainThread().createWorker();
        this.mFilePath = filePath;
        this.mDownloadListener = downloadListener;
    }

    @Override
    public void onSubscribe(Disposable disposable) {
        mDisposableWeakReference = new WeakReference<>(disposable);
        if (mDownloadListener != null) {
            mMainThreadWorker.schedule(() -> {
                mDownloadListener.onStart();
            });
        }
    }

    @Override
    public void onNext(InputStream inputStream) {
    }

    @Override
    public void onError(Throwable e) {
        release();
        if (mDownloadListener != null) {
            mMainThreadWorker.schedule(() -> {
                mDownloadListener.onError(e);
            });
        }
    }

    @Override
    public void onComplete() {
        release();
        if (mDownloadListener != null) {
            mMainThreadWorker.schedule(() -> {
                mDownloadListener.onSuccess(new File(mFilePath));
            });
        }
    }

    private void release() {
        if (mDisposableWeakReference != null) {
            Disposable disposable = mDisposableWeakReference.get();
            if (disposable != null && !disposable.isDisposed()) {
                disposable.dispose();
            }
            mDisposableWeakReference = null;
        }
    }
}
