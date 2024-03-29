package com.example.recievingeventapp;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
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
import android.widget.Button;
import android.widget.Toast;


import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;


public class MapDisplayFragment extends Fragment
        implements OnMapReadyCallback,
        SelectEventFragment.Listener
        {

    private static final String EVENT_KEY = "event";
    private FirebaseAccessor.EventType currentType;
    private GoogleMap map;
    private boolean inSelectMode;
    private Menu menu;



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

        @NonNull
        @Override
        public String toString() {
            return latitude + ", " + longitude + "; " + desc;
        }
    }

    public void setMap(String locality)
    {
        requester.runRequest(
                new Runnable() {
                    @Override
                    public void run() {
                        ArrayList<EventItem> items = new ArrayList<>();
                        new FirebaseAccessor()
                                .getEvents(
                                        getContext(),
                                        locality,
                                        currentType,
                                        items,
                                        new FirebaseAccessor.FirebaseListener()
                                        {

                                            @Override
                                            public void onSuccess(String successMessage) {
                                                Toast.makeText(getContext(), successMessage, Toast.LENGTH_SHORT).show();
                                                map.clear();
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
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {
                if (inSelectMode) {
                    inSelectMode = false;
                    Geocoder coder = new Geocoder(getActivity(), Locale.getDefault());
                    Address address = null;
                    try {
                        address = coder.getFromLocation(latLng.latitude, latLng.longitude, 1).get(0);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (address != null && "US".equals(address.getCountryCode())) {
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 7.75f));
                        setMap(address.getLocality());
                    }
                    Drawable icon = menu.findItem(R.id.selectArea).getIcon();
                    Toast.makeText(getActivity(), "Deactivated select mode. Searching for events...", Toast.LENGTH_SHORT).show();
                    icon.setTintMode(PorterDuff.Mode.SRC_ATOP);
                }
            }
        });
    }

    public interface LogOutHandler
    {
        public void logOut();
    }


    private static final String PHONE_NUMBER_KEY = "phone number";
    private static final String TAG = "MapDisplayFragment";


    private RequestHandler requester = new RequestHandler();
    private LogOutHandler logOutHandler;
    private SelectEventFragment.Listener listener;

    private MapView mapView;
//    private String phoneNumber;

    public MapDisplayFragment() {
        // Required empty public constructor
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        logOutHandler = (LogOutHandler) getActivity();
        listener = (SelectEventFragment.Listener) getActivity();
    }

    @Override
    public void onEventSelected(FirebaseAccessor.EventType event) {
        listener.onEventSelected(event);
    }

    public static MapDisplayFragment newInstance(FirebaseAccessor.EventType currentEvent) {
        MapDisplayFragment fragment = new MapDisplayFragment();
        Bundle args = new Bundle();
        args.putString(EVENT_KEY, currentEvent.name());
//        args.putLong(CODE_KEY, code);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
//            phoneNumber = getArguments().getString(PHONE_NUMBER_KEY);
//            long code = getArguments().getLong(CODE_KEY);
//            if (code != -1)
//            {
//
//                this.setMap(code);
//            }
            currentType = FirebaseAccessor.EventType.valueOf(getArguments().getString(EVENT_KEY));
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
        updateTheme(v, false);
        Button events = v.findViewById(R.id.select_event);
        events.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectEventFragment.newInstance().show(getChildFragmentManager(), TAG);
            }
        });

        Button reportButton = v.findViewById(R.id.report_event);
        reportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReportFragment.newInstance().show(getChildFragmentManager(), TAG);
            }
        });
        return v;
    }


    private void updateTheme(View v, boolean shouldRefresh)
    {
        Button events = v.findViewById(R.id.select_event);
        events.setBackgroundColor(getResources().getColor(currentType.COLOR_ID));
        Drawable icon = getResources().getDrawable(currentType.ICON_ID);
        icon.setTint(getResources().getColor(R.color.white));
        icon.setBounds(0, 0, (int) getResources().getDimension(R.dimen.mediumIconSize), (int) getResources().getDimension(R.dimen.mediumIconSize));
        events.setCompoundDrawables(icon, null, null,null );
        events.setText(currentType.NAME_ID);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main_menu, menu);
        this.menu = menu;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId())
        {
            case R.id.addLocality:
                AddLocalityFragment.newInstance().show(getChildFragmentManager(), TAG);
                break;
            case R.id.removeLocality:
                RemoveLocalityFragment.newInstance().show(getChildFragmentManager(), TAG);
                break;
            case R.id.selectArea:
                Drawable icon = item.getIcon();
                if (inSelectMode)
                {
                    Toast.makeText(getActivity(), "Deactivated select mode.", Toast.LENGTH_SHORT).show();
                    icon.setTintMode(PorterDuff.Mode.SRC_ATOP);
                }
                else
                {
                    Toast.makeText(getActivity(), "Activated select mode. Tap on the map to get events.", Toast.LENGTH_SHORT).show();
                    icon.setTintMode(PorterDuff.Mode.SCREEN);
                }
                inSelectMode = !inSelectMode;
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void addMarker(EventItem e)
    {
        Log.d(TAG, e.toString());
        MarkerOptions markers = new MarkerOptions();
        markers.position(new LatLng(e.getLatitude(), e.getLongitude()));
//        Log.d(TAG, new LatLng(e.getLatitude(), e.getLongitude()) + " --- lat-long coords");
        markers.title(e.getDesc());
        map.addMarker(markers);
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(markers.getPosition(), 10));
    }





}