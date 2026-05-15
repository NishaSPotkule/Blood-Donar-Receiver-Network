package com.example.blooddonarnet;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage message) {
        super.onMessageReceived(message);

        String title = "Blood Donation";
        String body = "New blood request received";


        if (message.getNotification() != null) {

            if (message.getNotification().getTitle() != null) {
                title = message.getNotification().getTitle();
            }

            if (message.getNotification().getBody() != null) {
                body = message.getNotification().getBody();
            }
        }

        showNotification(title, body);
    }

    private void showNotification(String title, String body) {

        String channelId = "blood_alert";

        NotificationManager manager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel =
                    new NotificationChannel(
                            channelId,
                            "Blood Alerts",
                            NotificationManager.IMPORTANCE_HIGH
                    );

            channel.setDescription("Blood donation notifications");

            manager.createNotificationChannel(channel);
        }


        Intent intent = new Intent(this, DonorDashboardActivity.class);

        intent.addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP
        );

        PendingIntent pendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                                | PendingIntent.FLAG_IMMUTABLE
                );


        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.blood)
                        .setContentTitle(title)
                        .setContentText(body)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent);


        int notificationId = (int) System.currentTimeMillis();

        manager.notify(notificationId, builder.build());
    }

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);

    }
}