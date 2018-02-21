package com.sunfusheng;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import static android.support.v4.app.NotificationCompat.DEFAULT_LIGHTS;
import static android.support.v4.app.NotificationCompat.VISIBILITY_PUBLIC;

/**
 * @author sunfusheng on 2018/2/17.
 */
public class FirNotification {

    public static final String CHANNEL_ID = "FirNotification";
    public static final int NOTIFICATION_ID = 1234;

    private Context context;
    private NotificationManager manager;
    private NotificationCompat.Builder builder;

    public void createBuilder(Context context) {
        this.context = context;
        manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        builder = new NotificationCompat.Builder(context, CHANNEL_ID);
        builder.setTicker("正在下载");
        builder.setSmallIcon(android.R.drawable.stat_sys_download);
        Drawable icon = FirUpdaterUtils.getIcon(context);
        if (icon != null && icon instanceof BitmapDrawable) {
            builder.setLargeIcon(((BitmapDrawable) icon).getBitmap());
        }
        builder.setAutoCancel(false);
        builder.setOngoing(true);
        builder.setProgress(100, 0, false);
        builder.setWhen(System.currentTimeMillis());
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 1, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setFullScreenIntent(pendingIntent, false);
        builder.setVisibility(VISIBILITY_PUBLIC);
        builder.setContentIntent(pendingIntent);
        builder.setDefaults(DEFAULT_LIGHTS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            builder.setPriority(Notification.PRIORITY_HIGH);
        }
    }

    public void setContentTitle(String title) {
        builder.setContentTitle(title);
    }

    public void setContentText(String text) {
        builder.setContentText(text);
    }

    public void setIcon(int resId) {
        builder.setSmallIcon(resId);
    }

    public void cancel() {
        manager.notify(NOTIFICATION_ID, builder.build());
        manager.cancel(NOTIFICATION_ID);
    }

    public void setContentIntent(Intent intent) {
        PendingIntent pIntent = PendingIntent.getActivity(context, 1, intent, 0);
        builder.setContentIntent(pIntent);
    }

    public void notifyNotification(int progress) {
        builder.setProgress(100, progress, false);
        manager.notify(NOTIFICATION_ID, builder.build());
    }

    public void notifyNotification() {
        manager.notify(NOTIFICATION_ID, builder.build());
    }
}
