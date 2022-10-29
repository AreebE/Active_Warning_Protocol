package com.example.recievingeventapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.FragmentManager;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.messaging.RemoteMessage;

import java.util.Iterator;

public class RecieverActivity extends AppCompatActivity
        implements LoginFragment.LoginHandler,
            MapDisplayFragment.LogOutHandler,
            SelectAreaCodeFragment.SelectedCodeHandler {

    public static final String CODE_KEY = "code";
    private static final String PHONE_NUMBER_KEY = "phone number";
    private String phoneNumber;
    private static final String TAG = "RecieverActivity";
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        preferences = getSharedPreferences(PHONE_NUMBER_KEY, Context.MODE_MULTI_PROCESS);
//        String list = "";
//        Iterator<String> iterator = getIntent().getExtras().keySet().iterator();
//        while (iterator.hasNext())
//        {
//            String key = iterator.next();
//            list += key + " = " + getIntent().getExtras().get(key).toString() + "; ";
//        }
//        System.out.println(list);
//        System.out.println(getIntent().getExtras().keySet().iterator());
//        System.out.println(getIntent().getExtras());
//
//        System.out.println(getIntent().getLongExtra(CODE_KEY, -1));
//        Log.d(TAG, preferences.getString(PHONE_NUMBER_KEY, null));
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction()
                .replace(R.id.fragmentContainerView, MapDisplayFragment.newInstance(), TAG)
                .commit();
    }

    @Override
    public void onLogin(String phoneNumber) {

    }

    @Override
    public void logOut() {

    }

    @Override
    public void setCode(Long code) {

    }
}