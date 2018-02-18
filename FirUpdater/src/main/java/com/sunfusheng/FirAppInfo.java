package com.sunfusheng;

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

    public static int TIME_OUT_MS = 10000;

    public static String appName;
    public static String appVersionCode;
    public static String appVersionName;
    public static String appChangeLog;
    public static String appInstallUrl;
    public static long appSize;

    public boolean requestAppInfo(String url) {
        Log.d(TAG, url);
        BufferedReader in = null;
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");
            conn.setDoOutput(false);
            conn.setDoInput(true);
            conn.setConnectTimeout(TIME_OUT_MS);
            conn.setReadTimeout(TIME_OUT_MS);
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
        return false;
    }

    private boolean parseResult(String result) {
        try {
            JSONObject object = new JSONObject(result);
            appName = object.getString("name");
            appVersionCode = object.getString("version");
            appVersionName = object.getString("versionShort");
            appChangeLog = object.getString("changelog");
            appInstallUrl = object.getString("installUrl");
            if (object.has("binary")) {
                JSONObject binary = object.getJSONObject("binary");
                if (binary != null) {
                    appSize = binary.getLong("fsize");
                }
            }
            Log.d(TAG, "appName: " + appName +
                    "\nappVersionCode: " + appVersionCode +
                    "\nappVersionName: " + appVersionName +
                    "\nappChangeLog: " + appChangeLog +
                    "\nappInstallUrl: " + appInstallUrl +
                    "\nappSize: " + appSize);
            return true;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

}
