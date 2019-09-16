package com.sunfusheng.updater.okhttp.download;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * @author sunfusheng
 * @since 2019-09-16
 */
public interface DownloadService {
    @Streaming
    @GET
    Observable<ResponseBody> download(@Url String url);
}
