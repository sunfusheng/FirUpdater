package com.sunfusheng;

import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author sunfusheng on 2018/2/17.
 */
public class FirAppInfo {

    public AppInfo requestAppInfo(String url) {
        try {
            FirUpdaterUtils.logger(url);
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000);

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                StringBuilder result = new StringBuilder();
                String line;

                while ((line = br.readLine()) != null) {
                    result.append(line);
                }

                FirUpdaterUtils.closeQuietly(br);
                return parseResult(result.toString());
            }
        } catch (Exception e) {
            FirUpdaterUtils.loggerError(e);
        }
        return null;
    }

    public class AppInfo {
        // 从 fir.im 获取的信息
        public String appName;
        public int appVersionCode;
        public String appVersionName;
        public String appChangeLog;
        public String appInstallUrl;
        public int appSize;

        // 添加的信息
        public String appId;
        public String apkName;
        public String apkPath;

        @Override
        public String toString() {
            return "AppInfo \n{" +
                    "\n appName='" + appName +
                    "\n appVersionCode=" + appVersionCode +
                    "\n appVersionName='" + appVersionName +
                    "\n appChangeLog='" + appChangeLog +
                    "\n appInstallUrl='" + appInstallUrl +
                    "\n appSize=" + appSize +
                    "\n appId='" + appId +
                    "\n apkName='" + apkName +
                    "\n apkPath='" + apkPath +
                    "\n}";
        }
    }

    private AppInfo parseResult(String result) {
        try {
            JSONObject object = new JSONObject(result);
            AppInfo appInfo = new AppInfo();

            appInfo.appName = object.getString("name");
            String versionCode = object.getString("version");
            if (!TextUtils.isEmpty(versionCode)) {
                appInfo.appVersionCode = Integer.parseInt(versionCode);
            }
            appInfo.appVersionName = "V" + object.getString("versionShort");
            appInfo.appChangeLog = object.getString("changelog");
            appInfo.appInstallUrl = object.getString("installUrl");
            if (object.has("binary")) {
                JSONObject binary = object.getJSONObject("binary");
                if (binary != null) {
                    appInfo.appSize = binary.getInt("fsize");
                }
            }
            return appInfo;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

}
