package com.example.arifk.ravenmessenger;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {


    private String notificationTitle = null, notificationBody = null,
            click_action = null, sender_Id = null, senderName = null;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {


        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            notificationTitle = remoteMessage.getNotification().getTitle();
            notificationBody = remoteMessage.getNotification().getBody();
            click_action = remoteMessage.getNotification().getClickAction();
        }


        sender_Id = remoteMessage.getData().get("senderId");
        senderName = remoteMessage.getData().get("senderName");

        sendNotification();
    }

    private void sendNotification() {

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this, "Chanenel1")
                        .setSmallIcon(R.drawable.avatar_default)
                        .setContentTitle(notificationTitle)
                        .setContentText(notificationBody)
                        .setSound(defaultSoundUri);


        Intent resultIntent = new Intent(click_action);
        resultIntent.putExtra("profileUser_ID", sender_Id);
        resultIntent.putExtra("profileUser_Name", senderName);
        resultIntent.putExtra("message_status", "Read");


        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        mBuilder.setContentIntent(resultPendingIntent);


        int mNotificationId = (int) System.currentTimeMillis();

        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        mNotifyMgr.notify(mNotificationId, mBuilder.build());
    }


}
