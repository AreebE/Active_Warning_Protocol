package com.example.recievingeventapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;

public class RecieverActivity extends AppCompatActivity
        implements LoginFragment.LoginHandler,
            MapDisplayFragment.LogOutHandler,
            SelectAreaCodeFragment.SelectedCodeHandler,
            SelectEventFragment.Listener {

    public static final String CODE_KEY = "code";
    private static final String PHONE_NUMBER_KEY = "phone number";
    private static final String EVENT_KEY = "event";
    private static final String PREFERENCES_FILE = "preferences";
    private String phoneNumber;
    private static final String TAG = "RecieverActivity";
    private SharedPreferences preferences;
    private FirebaseAccessor.EventType currentEvent = FirebaseAccessor.EventType.ACTIVE_THREAT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferences = getSharedPreferences(PREFERENCES_FILE, Context.MODE_MULTI_PROCESS);
        if (!preferences.contains(EVENT_KEY))
        {
            preferences.edit().putString(EVENT_KEY, FirebaseAccessor.EventType.ACTIVE_THREAT.name());
        }
        currentEvent = FirebaseAccessor.EventType.valueOf(preferences.getString(EVENT_KEY, FirebaseAccessor.EventType.ACTIVE_THREAT.name()));
        getWindow().setStatusBarColor(getResources().getColor(currentEvent.COLOR_ID));
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(currentEvent.COLOR_ID)));
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
        View view = findViewById(R.id.fragmentContainerView);
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction()
                .replace(R.id.fragmentContainerView, MapDisplayFragment.newInstance(currentEvent), TAG)
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

    @Override
    public void onEventSelected(FirebaseAccessor.EventType event) {
        preferences.edit().putString(EVENT_KEY, event.name()).apply();
        Intent intent = getIntent();

        finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        startActivity(intent);
    }
}