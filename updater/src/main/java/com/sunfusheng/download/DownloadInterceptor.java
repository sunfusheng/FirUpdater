package com.sunfusheng.download;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * @author sunfusheng
 * @since 2019-09-16
 */
public class DownloadInterceptor implements Interceptor {
    private IDownloadListener mDownloadListener;

    public DownloadInterceptor(IDownloadListener downloadListener) {
        this.mDownloadListener = downloadListener;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Response response = chain.proceed(chain.request());
        return response.newBuilder()
                .body(new DownloadProgressResponseBody(response.body(), mDownloadListener))
                .build();
    }
}
