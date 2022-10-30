package com.example.recievingeventapp;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.provider.Settings;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ReportFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ReportFragment extends DialogFragment
        implements SelectEventFragment.Listener {

    private static final String TAG = "ReportFragment";

    public ReportFragment() {
        requestHandler = new RequestHandler();
    }

    private Button changeTypeOfEvent;
    private FirebaseAccessor.EventType eventType;
    private RequestHandler requestHandler;

    public static ReportFragment newInstance() {
        ReportFragment fragment = new ReportFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_report, container, false);
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = onCreateView(getLayoutInflater(), null, savedInstanceState);

        EditText details = view.findViewById(R.id.details_of_event);
        Button submitReport = view.findViewById(R.id.report_event);
        changeTypeOfEvent = view.findViewById(R.id.event_type);

        onEventSelected(FirebaseAccessor.EventType.ACTIVE_THREAT);

        changeTypeOfEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectEventFragment.newInstance().show(getChildFragmentManager(), TAG);
            }
        });

        submitReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String eventDetails = details.getText().toString();
                FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(getActivity());
                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getActivity(), "Please enable location permissions in your settings.", Toast.LENGTH_SHORT).show();
                    return;
                }
                client.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        Location location = task.getResult();
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        Geocoder coder = new Geocoder(getActivity(), Locale.getDefault());
                        Address[] address = new Address[1];
                        try {
                            address[0] = coder.getFromLocation(latitude, longitude, 1).get(0);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (address != null) {
                            requestHandler.runRequest(new Runnable() {
                                @Override
                                public void run() {
                                    new FirebaseAccessor()
                                            .createEvent(address[0].getLocality(), eventType, eventDetails,
                                                    new LatLng(latitude, longitude), getActivity());
                                }
                            }, 0, false);
                        }
                    }
                });

//                Toast.makeText(getActivity(), "Error: Your location has not been turned on.", Toast.LENGTH_LONG).show();
//                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//                startActivity(intent);
            }
        });

        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity())
                .setTitle(Html.fromHtml("<font color='#000000'>" + getResources().getString(R.string.report_event) +  "</font>"))
                .setView(view);

        return dialog.create();
    }



    @Override
    public void onEventSelected(FirebaseAccessor.EventType event) {
        eventType = event;
        Drawable icon = getResources().getDrawable(eventType.ICON_ID);
        icon.setTint(getResources().getColor(R.color.white));
        icon.setBounds(0, 0, (int) getResources().getDimension(R.dimen.mediumIconSize), (int) getResources().getDimension(R.dimen.mediumIconSize));
        changeTypeOfEvent.setCompoundDrawables(icon, null, null,null );
        changeTypeOfEvent.setText(eventType.NAME_ID);
        changeTypeOfEvent.setBackgroundColor(getResources().getColor(eventType.COLOR_ID));
    }
}