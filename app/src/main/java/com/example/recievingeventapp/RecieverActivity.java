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
        phoneNumber = preferences.getString(PHONE_NUMBER_KEY, null);
        if (phoneNumber != null)
        {
            onLogin(phoneNumber);
        }
        else
        {
            logOut();
        }
    }

    @Override
    public void onLogin(String phoneNumber) {

        long code = -1;
        try
        {
            code = Long.parseLong((String) getIntent().getExtras().get(CODE_KEY));
        } catch (NumberFormatException|NullPointerException nfe)
        {
            Log.d(TAG, nfe.toString());

            try {
                code = Long.parseLong(getIntent().getStringExtra(CODE_KEY));

            } catch (NumberFormatException|NullPointerException npe)
            {
                Log.d(TAG, npe.toString());

            }
        } catch (ClassCastException cce)
        {
            code = getIntent().getExtras().getLong(CODE_KEY);
        }
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction()
                .replace(R.id.fragmentContainerView, MapDisplayFragment.newInstance(phoneNumber, code), TAG)
                .commit();
        this.phoneNumber = phoneNumber;

        SharedPreferences.Editor writer = preferences.edit();
        writer.putString(PHONE_NUMBER_KEY, this.phoneNumber);
        writer.commit();
    }

    @Override
    public void logOut() {
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction()
                .replace(R.id.fragmentContainerView, LoginFragment.newInstance(), TAG)
                .commit();
        SharedPreferences.Editor writer = preferences.edit();
        writer.putString(PHONE_NUMBER_KEY, null);
        writer.commit();
    }


    @Override
    public void setCode(Long code) {
        FragmentManager manager = getSupportFragmentManager();
        MapDisplayFragment mapDisplayFragment = (MapDisplayFragment) manager.findFragmentById(R.id.fragmentContainerView);
        mapDisplayFragment.setMap(code);
    }
}