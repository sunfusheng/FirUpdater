package com.sunfusheng.updater.okhttp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import static android.os.Build.VERSION.SDK_INT;
import static android.support.v4.app.NotificationCompat.VISIBILITY_PUBLIC;

/**
 * @author sunfusheng
 * @since 2019-09-19
 */
class UpdaterNotification {
    private static int NOTIFICATION_ID = 1234;
    private static String CHANNEL_ID = "UpdaterNotification";
    private static String CHANNEL_NAME = "FirUpdater";
    private static int CHANNEL_SEQUENCE = 0;
    private static String LAST_CHANNEL_ID = CHANNEL_ID;

    private Context context;
    private NotificationManager notificationManager;
    private NotificationCompat.Builder builder;

    UpdaterNotification createProgressBuilder(Context context) {
        this.context = context;
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        setChannel();
        builder = new NotificationCompat.Builder(context, CHANNEL_ID);
        builder.setSmallIcon(android.R.drawable.stat_sys_download);
        Drawable icon = UpdaterUtil.getIcon(context);
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
        builder.setVibrate(new long[]{0});
        builder.setPriority(NotificationCompat.PRIORITY_LOW);
        return this;
    }

    private void setChannel() {
        if (SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.deleteNotificationChannel(LAST_CHANNEL_ID);
            CHANNEL_SEQUENCE++;
            CHANNEL_ID = CHANNEL_ID + CHANNEL_SEQUENCE;
            CHANNEL_NAME = CHANNEL_NAME + CHANNEL_SEQUENCE;
            LAST_CHANNEL_ID = CHANNEL_ID;
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
            notificationChannel.enableVibration(false);
            notificationChannel.setVibrationPattern(new long[]{0});
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    public void setContentTitle(String title) {
        builder.setContentTitle(title);
    }

    public void setContentText(String text) {
        builder.setContentText(text);
    }

    public void cancel() {
        notificationManager.notify(NOTIFICATION_ID, builder.build());
        notificationManager.cancel(NOTIFICATION_ID);
    }

    public void setContentIntent(Intent intent) {
        PendingIntent pIntent = PendingIntent.getActivity(context, 1, intent, 0);
        builder.setContentIntent(pIntent);
    }

    public void notifyNotification(int progress) {
        builder.setProgress(100, progress, false);
        notifyNotification();
    }

    public void notifyNotification() {
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}
