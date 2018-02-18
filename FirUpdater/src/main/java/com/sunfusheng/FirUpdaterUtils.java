package com.sunfusheng;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

/**
 * @author sunfusheng on 2018/2/17.
 */
public class FirUpdaterUtils {

    public static String getPackageName(Context context) {
        return context.getPackageName();
    }

    public static String getName(Context context) {
        return getName(context, getPackageName(context));
    }

    public static String getName(Context context, String packageName) {
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(packageName, 0);
            return pi == null ? null : pi.applicationInfo.loadLabel(pm).toString();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Drawable getIcon(Context context) {
        return getIcon(context, getPackageName(context));
    }

    public static Drawable getIcon(Context context, String packageName) {
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(packageName, 0);
            return pi == null ? null : pi.applicationInfo.loadIcon(pm);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static int getVersionCode(Context context) {
        return getVersionCode(context, getPackageName(context));
    }

    public static int getVersionCode(Context context, String packageName) {
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(packageName, 0);
            return pi == null ? -1 : pi.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static String getVersionName(Context context) {
        return getVersionName(context, getPackageName(context));
    }

    public static String getVersionName(Context context, String packageName) {
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(packageName, 0);
            return pi == null ? null : pi.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean isAppForeground(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (manager == null) {
            return false;
        }

        List<ActivityManager.RunningAppProcessInfo> appProcesses = manager.getRunningAppProcesses();
        if (appProcesses == null || appProcesses.size() == 0) {
            return false;
        }

        for (ActivityManager.RunningAppProcessInfo processInfo : appProcesses) {
            if (processInfo == null) continue;
            if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return processInfo.processName.equals(getPackageName(context));
            }
        }
        return false;
    }

    public static void installApk(Context context, String apkPath) {
        if (TextUtils.isEmpty(apkPath)) {
            return;
        }

        try {
            File apkFile = new File(apkPath);
            if (!apkFile.exists()) {
                throw new FileNotFoundException("Apk file does not exist!");
            }

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Uri apkUri;
            if (Build.VERSION.SDK_INT >= 24) {
                apkUri = FileProvider.getUriForFile(context, getPackageName(context) + ".file_provider", apkFile);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else {
                apkUri = Uri.fromFile(apkFile);
            }
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void closeQuietly(final Closeable... closeables) {
        if (closeables == null || closeables.length == 0) {
            return;
        }

        for (Closeable closeable : closeables) {
            if (closeable == null) {
                continue;
            }

            try {
                closeable.close();
            } catch (IOException ignored) {
            }
        }
    }

    private static final Handler handler = new Handler(Looper.getMainLooper());

    public static void runOnMainThread(Runnable runnable) {
        if (runnable != null) {
            if (Looper.myLooper() != Looper.getMainLooper()) {
                handler.post(runnable);
            } else {
                runnable.run();
            }
        }

    }
}
