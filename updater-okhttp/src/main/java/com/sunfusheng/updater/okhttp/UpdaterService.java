package com.sunfusheng.updater.okhttp;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * @author by sunfusheng on 2019-08-13
 */
interface UpdaterService {

    String BASE_URL = "http://api.fir.im/";

    @GET("apps/latest/{app_id}")
    Observable<AppInfo> fetchAppInfo(
            @Path("app_id") String appId,
            @Query("api_token") String apiToken
    );

    @Streaming
    @GET
    Observable<ResponseBody> downloadFile(@Url String fileUrl);

}