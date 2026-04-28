package com.example.blooddonarnet;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage message) {

        String title = message.getNotification().getTitle();
        String body = message.getNotification().getBody();

        showNotification(title, body);
    }

    private void showNotification(String title, String message) {

        String channelId = "blood_alert";

        NotificationManager manager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel =
                    new NotificationChannel(channelId, "Blood Alerts",
                            NotificationManager.IMPORTANCE_HIGH);

            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, channelId)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setSmallIcon(R.drawable.blood)
                        .setAutoCancel(true);

        manager.notify(1, builder.build());
    }
}
