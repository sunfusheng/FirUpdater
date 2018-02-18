package com.sunfusheng;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

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
        builder.setSmallIcon(android.R.drawable.stat_sys_download);
        builder.setAutoCancel(false);
        builder.setOngoing(true);
        builder.setShowWhen(false);
        builder.setProgress(100, 7, false);
        builder.setOngoing(true);
        builder.setShowWhen(false);
        builder.setDefaults(NotificationCompat.DEFAULT_ALL);
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
