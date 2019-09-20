package com.sunfusheng;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.FileProvider;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

/**
 * @author sunfusheng
 * @since 2019-08-14
 */
public class UpdaterUtil {

    public static String getPackageName(Context context) {
        return context.getPackageName();
    }

    public static String getName(Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(getPackageName(context), 0);
            return pi.applicationInfo.loadLabel(pm).toString();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Drawable getIcon(Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(getPackageName(context), 0);
            return pi.applicationInfo.loadIcon(pm);
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

    public static Intent getInstallApkIntent(Context context, String apkPath) {
        try {
            File apkFile = new File(apkPath);
            if (!apkFile.exists()) {
                throw new FileNotFoundException("Apk file does not exist!");
            }

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Uri apkUri;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                apkUri = FileProvider.getUriForFile(context, getPackageName(context) + ".file_provider", apkFile);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else {
                apkUri = Uri.fromFile(apkFile);
            }
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            return intent;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void installApk(Context context, String apkPath) {
        context.startActivity(getInstallApkIntent(context, apkPath));
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

    @SuppressLint("DefaultLocale")
    public static String getFileSizeDesc(long byteCount) {
        if (byteCount <= 0) {
            return "0B";
        } else if (byteCount < 1024) {
            return String.format("%.2fB", (double) byteCount);
        } else if (byteCount < 1048576) {
            return String.format("%.2fKB", (double) byteCount / 1024);
        } else if (byteCount < 1073741824) {
            return String.format("%.2fMB", (double) byteCount / 1048576);
        } else {
            return String.format("%.2fGB", (double) byteCount / 1073741824);
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

    private static SharedPreferences sharedPreferences;
    private static final String SP_NAME = "fir_downloader_progress_file_name";

    public static SharedPreferences getSharedPreferences(Context context) {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        }
        return sharedPreferences;
    }

    public static void putCurrLengthValue(Context context, String key, int value) {
        getSharedPreferences(context)
                .edit()
                .putInt(key, value)
                .apply();
    }

    public static int getCurrLengthValue(Context context, String key) {
        return getSharedPreferences(context).getInt(key, 0);
    }

    public static boolean isAppInstalled(Context context, String appPackageName) {
        PackageManager manager = context.getPackageManager();
        if (manager == null) {
            return false;
        }

        List<PackageInfo> infos = manager.getInstalledPackages(0);
        if (infos != null) {
            for (int i = 0; i < infos.size(); i++) {
                String packageName = infos.get(i).packageName;
                if (packageName.equals(appPackageName)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean startApp(Context context, String appPackageName) {
        try {
            PackageManager manager = context.getPackageManager();
            if (manager == null) {
                return false;
            }

            Intent intent = manager.getLaunchIntentForPackage(appPackageName);
            if (intent != null) {
                context.startActivity(intent);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
