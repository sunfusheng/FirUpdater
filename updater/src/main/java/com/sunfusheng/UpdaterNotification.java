package com.sunfusheng;

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
    private static int NOTIFICATION_ID = 8888;
    private static String CHANNEL_ID = "UpdaterNotification";
    private static String CHANNEL_NAME = "FirUpdater";
    private static int CHANNEL_SEQUENCE = 0;
    private static String LAST_CHANNEL_ID = CHANNEL_ID;

    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mNotificationBuilder;

    UpdaterNotification(Context context, String title) {
        mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        setChannel();
        mNotificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID);
        mNotificationBuilder.setContentTitle(title);
        mNotificationBuilder.setSmallIcon(android.R.drawable.stat_sys_download);
        Drawable icon = UpdaterUtil.getIcon(context);
        if (icon instanceof BitmapDrawable) {
            mNotificationBuilder.setLargeIcon(((BitmapDrawable) icon).getBitmap());
        }
        mNotificationBuilder.setAutoCancel(false);
        mNotificationBuilder.setOngoing(true);
        mNotificationBuilder.setProgress(100, 0, false);
        mNotificationBuilder.setWhen(System.currentTimeMillis());
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 1, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT);
        mNotificationBuilder.setFullScreenIntent(pendingIntent, false);
        mNotificationBuilder.setVisibility(VISIBILITY_PUBLIC);
        mNotificationBuilder.setContentIntent(pendingIntent);
        mNotificationBuilder.setVibrate(new long[]{0});
        mNotificationBuilder.setPriority(NotificationCompat.PRIORITY_LOW);
    }

    private void setChannel() {
        if (SDK_INT >= Build.VERSION_CODES.O) {
            mNotificationManager.deleteNotificationChannel(LAST_CHANNEL_ID);
            CHANNEL_SEQUENCE++;
            CHANNEL_ID = CHANNEL_ID + CHANNEL_SEQUENCE;
            CHANNEL_NAME = CHANNEL_NAME + CHANNEL_SEQUENCE;
            LAST_CHANNEL_ID = CHANNEL_ID;
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
            notificationChannel.enableVibration(false);
            notificationChannel.setVibrationPattern(new long[]{0});
            mNotificationManager.createNotificationChannel(notificationChannel);
        }
    }

    void setProgress(int progress) {
        mNotificationBuilder.setContentText("下载更新中..." + progress + "%");
        mNotificationBuilder.setProgress(100, progress, false);
        mNotificationManager.notify(NOTIFICATION_ID, mNotificationBuilder.build());
    }

    void setContentIntent(Context context, Intent intent) {
        PendingIntent pIntent = PendingIntent.getActivity(context, 1, intent, 0);
        mNotificationBuilder.setContentIntent(pIntent);
    }

    void cancel() {
        mNotificationManager.notify(NOTIFICATION_ID, mNotificationBuilder.build());
        mNotificationManager.cancel(NOTIFICATION_ID);
    }
}
