package com.sunfusheng.firupdater.sample;

/**
 * @author sunfusheng on 2018/2/17.
 */
public class DataUtil {

    public static AppConfig[][] apps = {
            {AppConfig.NULL, AppConfig.GROUP_RECYCLER_VIEW_ADAPTER}
    };

    public enum AppConfig {
        NULL(0, 0, 0, null),
        GROUP_RECYCLER_VIEW_ADAPTER(R.attr.key_grva, R.string.grva_title, R.string.grva_subtitle, "5a80151e959d6949f2ecb2e8");

        public int key;
        public int titleId;
        public int subtitleId;
        public String appToken;
        public String appId;

        AppConfig(int key, int titleId, int subtitleId, String appId) {
            this.key = key;
            this.titleId = titleId;
            this.subtitleId = subtitleId;
            this.appToken = "3c57fb226edf7facf821501e4eba08d2";
            this.appId = appId;
        }
    }
}
