package com.example.recievingeventapp;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessaging;


public class TopicHandler extends IntentService {

    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_SUBSCRIBE = "subscribe";
    private static final String ACTION_UNSUBSCRIBE = "unsubscribe";

    private static final String TOPIC_KEY = "topic";

    public TopicHandler() {
        super("MyIntentService");
    }

    public static void subscribeToTopic(Context context, String topic) {
        Intent intent = new Intent(context, TopicHandler.class);
        intent.setAction(ACTION_SUBSCRIBE);
        intent.putExtra(TOPIC_KEY, topic);
        context.startService(intent);
    }

    public static void unsubscribeToTopic(Context context, String topic) {
        Intent intent = new Intent(context, TopicHandler.class);
        intent.setAction(ACTION_UNSUBSCRIBE);
        intent.putExtra(TOPIC_KEY, topic);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        if (intent != null)
        {
            String action = intent.getAction();
            String topic = intent.getStringExtra(TOPIC_KEY);
            switch (action)
            {
                case ACTION_SUBSCRIBE:
                    FirebaseMessaging.getInstance().subscribeToTopic(topic);
                    break;
                case ACTION_UNSUBSCRIBE:
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(topic);
                    break;

            }
        }
    }
}