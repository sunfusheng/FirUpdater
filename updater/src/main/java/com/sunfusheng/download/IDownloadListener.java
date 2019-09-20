package com.sunfusheng.download;

import java.io.File;

/**
 * @author sunfusheng
 * @since 2019-09-16
 */
public interface IDownloadListener {
    void onStart();

    void onProgress(long bytesTransferred, long totalBytes, int percentage);

    void onSuccess(File file);

    void onError(Throwable e);
}
