package com.example.recievingeventapp;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;


public class MapDisplayFragment extends Fragment
        implements OnMapReadyCallback {

    private static final String CODE_KEY = "code";
    private GoogleMap map;

    public static class EventItem
    {
        private Double latitude;
        private Double longitude;
        private String desc;

        public EventItem(Double latitude, Double longitude, String desc)
        {

            this.latitude = latitude;
            this.longitude = longitude;
            this.desc = desc;
        }

        public Double getLatitude() {
            return latitude;
        }

        public Double getLongitude() {
            return longitude;
        }

        public String getDesc() {
            return desc;
        }
    }

    public void setMap(Long code)
    {
        requester.runRequest(
                new Runnable() {
                    @Override
                    public void run() {
                        ArrayList<EventItem> items = new ArrayList<>();
                        new FirebaseAccessor()
                                .getEvents(
                                        getContext(),
                                        code,
                                        items,
                                        new FirebaseAccessor.FirebaseListener()
                                        {

                                            @Override
                                            public void onSuccess(String successMessage) {
                                                Toast.makeText(getContext(), successMessage, Toast.LENGTH_SHORT).show();
                                                for (int i = 0; i < items.size(); i++)
                                                {
                                                    addMarker(items.get(i));
                                                }
                                            }

                                            @Override
                                            public void onFailure(String errorMessage) {
                                                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                );
                    }
                },
                0,
                false);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.map = googleMap;
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(37.090200, -100.712900), 3.5f));
    }

    public interface LogOutHandler
    {
        public void logOut();
    }

    private static final String PHONE_NUMBER_KEY = "phone number";
    private static final String TAG = "MapDisplayFragment";


    private RequestHandler requester = new RequestHandler();
    private LogOutHandler logOutHandler;
    private MapView mapView;
    private String phoneNumber;

    public MapDisplayFragment() {
        // Required empty public constructor
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        logOutHandler = (LogOutHandler) getActivity();
    }

    public static MapDisplayFragment newInstance(String phoneNumber, long code) {
        MapDisplayFragment fragment = new MapDisplayFragment();
        Bundle args = new Bundle();
        args.putString(PHONE_NUMBER_KEY, phoneNumber);
        args.putLong(CODE_KEY, code);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            phoneNumber = getArguments().getString(PHONE_NUMBER_KEY);
            long code = getArguments().getLong(CODE_KEY);
            if (code != -1)
            {

                this.setMap(code);
            }
        }
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_map_display, container, false);
        mapView = (MapView) v.findViewById(R.id.mapView);
        mapView.onCreate(getArguments());
        mapView.onResume();
        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        mapView.getMapAsync(this);
        return v;
    }


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId())
        {
            case R.id.addAreaCode:
                AddAreaCodeFragment.newInstance(phoneNumber).show(getChildFragmentManager(), TAG);
                break;
            case R.id.deleteAreaCode:
                RemoveAreaCodeFragment.newInstance(phoneNumber).show(getChildFragmentManager(), TAG);
                break;
            case R.id.selectAreaCode:
                SelectAreaCodeFragment.newInstance(phoneNumber).show(getChildFragmentManager(), TAG);
                break;
            case R.id.logOut:
                requester.runRequest(
                        new Runnable() {
                            @Override
                            public void run() {
                                new FirebaseAccessor()
                                        .unsubscribeToAreaCodes
                                                (
                                                        getContext(),
                                                        phoneNumber,
                                                        new FirebaseAccessor.FirebaseListener()
                                                        {

                                                            @Override
                                                            public void onSuccess(String successMessage) {
                                                                Toast.makeText(getContext(), successMessage, Toast.LENGTH_SHORT).show();
                                                                logOutHandler.logOut();
                                                            }

                                                            @Override
                                                            public void onFailure(String errorMessage) {
                                                                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                );
                            }
                        },
                        0,
                        false
                );
                break;
            case R.id.refreshCodes:
                requester.runRequest(new Runnable() {
                    @Override
                    public void run() {
                        new FirebaseAccessor().subscribeToAreaCodes
                                (
                                        getContext(),
                                        phoneNumber,
                                        new FirebaseAccessor.FirebaseListener()
                                        {
                                            @Override
                                            public void onSuccess(String successMessage) {
                                                Toast.makeText(getContext(), successMessage, Toast.LENGTH_SHORT).show();
                                            }

                                            @Override
                                            public void onFailure(String errorMessage) {
                                                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                );
                    }
                }, 60, false);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void addMarker(EventItem e)
    {
        MarkerOptions markers = new MarkerOptions();
        markers.position(new LatLng(e.getLatitude(), e.getLongitude()));
        Log.d(TAG, new LatLng(e.getLatitude(), e.getLongitude()) + " --- lat-long coords");
        markers.title(e.getDesc());
        map.addMarker(markers);
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(markers.getPosition(), 10));
    }
}