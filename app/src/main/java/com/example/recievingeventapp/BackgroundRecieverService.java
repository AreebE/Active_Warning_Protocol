package com.example.recievingeventapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;


public class BackgroundRecieverService extends FirebaseMessagingService
{

    public static final String AREA_CODE = "area code";

    private static final String CHANNEL_ID = "awero9";
    private static final String TAG = "BackgroundReciever";

    public BackgroundRecieverService()
    {

        FirebaseMessaging.getInstance().subscribeToTopic("")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d("TEST", "ready to go");
                    }
                });

    }




    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);

        Intent i = new Intent(this, RecieverActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        System.out.println(message.toString() + " -- " + message.getData().toString());
        System.out.println(message.getData().get("code"));
        i.putExtra(RecieverActivity.CODE_KEY, message.getData().get("code"));

        System.out.println(i);
        System.out.println(i.getStringExtra("code"));
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_IMMUTABLE);
        Log.d("TEST", "recieved a notification");
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "testChannel", NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription("Test a first ragam");
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);
        RemoteMessage.Notification dataRecieved =  message.getNotification();
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle(dataRecieved.getTitle())
                .setContentText(dataRecieved.getBody())
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();

        manager.notify(0, notification);

        message.getData();
    }

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
    }
}
