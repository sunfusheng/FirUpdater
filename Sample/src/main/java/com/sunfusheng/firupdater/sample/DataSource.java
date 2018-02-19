package com.sunfusheng.firupdater.sample;

/**
 * @author sunfusheng on 2018/2/17.
 */
public class DataSource {

    public static final String API_TOKEN = "3c57fb226edf7facf821501e4eba08d2";
    public static final String FIR_UPDATER_APP_ID = "5a89af10548b7a760110c918";

    public static AppConfig[][] apps = {
            {AppConfig.NULL, AppConfig.GROUP_RECYCLER_VIEW_ADAPTER}
    };

    public enum AppConfig {
        NULL(0, 0, 0, null),
        GROUP_RECYCLER_VIEW_ADAPTER(R.attr.key_grva, R.string.grva_title, R.string.grva_subtitle, "5a80151e959d6949f2ecb2e8");

        public int key;
        public int titleId;
        public int subtitleId;
        public String appId;

        AppConfig(int key, int titleId, int subtitleId, String appId) {
            this.key = key;
            this.titleId = titleId;
            this.subtitleId = subtitleId;
            this.appId = appId;
        }
    }
}
