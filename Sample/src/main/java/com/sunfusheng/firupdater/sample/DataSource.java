package com.sunfusheng.firupdater.sample;

/**
 * @author sunfusheng on 2018/2/17.
 */
public class DataSource {

    public static final String API_TOKEN = "3c57fb226edf7facf821501e4eba08d2";
    public static final String FIR_UPDATER_APP_ID = "5a89af10548b7a760110c918";

    public static AppConfig[][] apps = {
            {AppConfig.NULL, AppConfig.MV, AppConfig.GIV, AppConfig.GRVA, AppConfig.GANK},
            {AppConfig.NULL, AppConfig._360, AppConfig.TULING, AppConfig.SHLV}
    };

    public enum AppConfig {
        NULL(0, 0, 0, 0),
        MV(R.attr.key_mv, R.string.mv_title, R.string.mv_subtitle, R.string.mv_app_id),
        GIV(R.attr.key_giv, R.string.giv_title, R.string.giv_subtitle, R.string.giv_app_id),
        GRVA(R.attr.key_grva, R.string.grva_title, R.string.grva_subtitle, R.string.grva_app_id),
        GANK(R.attr.key_gank, R.string.gank_title, R.string.gank_subtitle, R.string.gank_app_id),

        _360(R.attr.key_360, R.string._360_title, R.string._360_subtitle, R.string._360_app_id),
        TULING(R.attr.key_tuling, R.string.tuling_title, R.string.tuling_subtitle, R.string.tuling_app_id),
        SHLV(R.attr.key_shlv, R.string.shlv_title, R.string.shlv_subtitle, R.string.shlv_app_id);

        public int key;
        public int titleId;
        public int subtitleId;
        public int appId;

        AppConfig(int key, int titleId, int subtitleId, int appId) {
            this.key = key;
            this.titleId = titleId;
            this.subtitleId = subtitleId;
            this.appId = appId;
        }
    }
}
