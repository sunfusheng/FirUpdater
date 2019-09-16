package com.sunfusheng.updater.okhttp.download;

import java.io.File;

/**
 * @author sunfusheng
 * @since 2019-09-16
 */
public interface IDownloadListener {
    void onStart();

    void onSuccess(File file);

    void onError(Throwable e);

    void onProgress(long bytesTransferred, long totalBytes, int percentage);
}
