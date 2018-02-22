package com.sunfusheng;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * @author sunfusheng on 2018/2/18.
 */
public class FirPermissionHelper {

    private static FirPermissionHelper firPermissionHelper;
    private List<String> permissions;

    public static FirPermissionHelper getInstant() {
        if (firPermissionHelper == null) {
            firPermissionHelper = new FirPermissionHelper();
        }
        return firPermissionHelper;
    }

    private FirPermissionHelper() {
    }

    private OnPermissionCallback onPermissionCallback;

    public void requestPermission(Context context, OnPermissionCallback onPermissionCallback) {
        this.onPermissionCallback = onPermissionCallback;

        permissions = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (!isGranted(context, permissions)) {
            FirPermissionActivity.start(context);
        } else {
            this.onPermissionCallback.onGranted();
        }
    }

    public static boolean isGranted(Context context, List<String> permissions) {
        for (String permission : permissions) {
            if (!isGranted(context, permission)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isGranted(Context context, String permission) {
        return PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(context, permission);
    }

    public void setOnPermissionCallback(OnPermissionCallback onPermissionCallback) {
        this.onPermissionCallback = onPermissionCallback;
    }

    public interface OnPermissionCallback {
        void onGranted();

        void onDenied();
    }

    public static class FirPermissionActivity extends Activity {

        private static final int REQUEST_CODE = 1000;

        public static void start(final Context context) {
            Intent starter = new Intent(context, FirPermissionActivity.class);
            starter.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(starter);
        }

        @Override
        protected void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (isGranted(this, firPermissionHelper.permissions)) {
                if (firPermissionHelper.onPermissionCallback != null) {
                    firPermissionHelper.onPermissionCallback.onGranted();
                }
            } else {
                String[] permissions = firPermissionHelper.permissions.toArray(new String[firPermissionHelper.permissions.size()]);
                ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE);
            }
        }

        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            if (requestCode == REQUEST_CODE && isGranted(this, firPermissionHelper.permissions)) {
                if (firPermissionHelper.onPermissionCallback != null) {
                    firPermissionHelper.onPermissionCallback.onGranted();
                }
            } else {
                if (firPermissionHelper.onPermissionCallback != null) {
                    firPermissionHelper.onPermissionCallback.onDenied();
                }
            }
            finish();
        }
    }

}
