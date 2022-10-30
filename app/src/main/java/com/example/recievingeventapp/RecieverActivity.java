package com.example.recievingeventapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

public class RecieverActivity extends AppCompatActivity
        implements
            MapDisplayFragment.LogOutHandler,
            SelectEventFragment.Listener {

    public static final String LOCALITY_KEY = "locality";
    public static final String PHONE_NUMBER_KEY = "phone number";
    public static final String EVENT_KEY = "event";
    public static final String PREFERENCES_FILE = "preferences";
    public static final String LAT_KEY = "lat";
    public static final String LON_KEY = "lon";

    private static final int PERMISSIONS_LOCALITY = 1092;

    private String phoneNumber;
    private static final String TAG = "RecieverActivity";
    private SharedPreferences preferences;
    private FirebaseAccessor.EventType currentEvent = FirebaseAccessor.EventType.ACTIVE_THREAT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(this, "Please enable location permissions in your settings.", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    PERMISSIONS_LOCALITY);
            return;

        }
        else
        {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (!(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                    || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)))
            {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }

        }

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
    public void logOut() {

    }

    @Override
    public void onEventSelected(FirebaseAccessor.EventType event) {
        preferences.edit().putString(EVENT_KEY, event.name()).apply();
        Intent intent = getIntent();

        finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Intent intent = getIntent();
        finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        startActivity(intent);
    }
}