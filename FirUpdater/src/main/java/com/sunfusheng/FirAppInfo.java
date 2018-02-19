package com.sunfusheng;

import android.text.TextUtils;
import android.util.Log;

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

    private static final String TAG = "FirAppInfo";

    public AppInfo requestAppInfo(String url) {
        Log.d(TAG, url);
        BufferedReader in = null;
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");
            conn.setDoOutput(false);
            conn.setDoInput(true);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.connect();
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    result.append(line);
                }
                return parseResult(result.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            FirUpdaterUtils.closeQuietly(in);
            if (conn != null) {
                conn.disconnect();
            }
        }
        return null;
    }

    public class AppInfo {
        public String appName;
        public int appVersionCode;
        public String appVersionName;
        public String appChangeLog;
        public String appInstallUrl;
        public long appSize;

        @Override
        public String toString() {
            return "AppInfo{" +
                    "\nappName='" + appName +
                    "\nappVersionCode=" + appVersionCode +
                    "\nappVersionName='" + appVersionName +
                    "\nappChangeLog='" + appChangeLog +
                    "\nappInstallUrl='" + appInstallUrl +
                    "\nappSize=" + appSize +
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
            appInfo.appVersionName = object.getString("versionShort");
            appInfo.appChangeLog = object.getString("changelog");
            appInfo.appInstallUrl = object.getString("installUrl");
            if (object.has("binary")) {
                JSONObject binary = object.getJSONObject("binary");
                if (binary != null) {
                    appInfo.appSize = binary.getLong("fsize");
                }
            }
            Log.d(TAG, appInfo.toString());
            return appInfo;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

}
