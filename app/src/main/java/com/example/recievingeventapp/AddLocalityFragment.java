package com.example.recievingeventapp;

import android.app.Dialog;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
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
import java.util.Locale;


public class AddLocalityFragment extends DialogFragment
    implements OnMapReadyCallback {

    private static final String TAG = "AddLocalityFragment";
    private MapView mapView;
    private String locality;
    private GoogleMap map;

    public AddLocalityFragment() {
    }


    public static AddLocalityFragment newInstance() {
        AddLocalityFragment fragment = new AddLocalityFragment();
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
        return inflater.inflate(R.layout.fragment_add_locality, container, false);
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View v = onCreateView(getLayoutInflater(), null, savedInstanceState);

        mapView = (MapView) v.findViewById(R.id.mapView);
        mapView.onCreate(getArguments());
        mapView.onResume();
        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        mapView.getMapAsync(this);
        Button addLocality = v.findViewById(R.id.addLocality);

        addLocality.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Calling the add locality");
                if (locality == null)
                {
                    return;
                }
                new RequestHandler().runRequest(
                        new Runnable() {
                            @Override
                            public void run() {
                                new FirebaseAccessor().addLocality
                                        (
                                                getContext(),
                                                locality,
                                                new FirebaseAccessor.FirebaseListener() {
                                                    @Override
                                                    public void onSuccess(String successMessage) {
                                                        Toast.makeText(getActivity(), successMessage, Toast.LENGTH_SHORT).show();
                                                        dismiss();
                                                    }

                                                    @Override
                                                    public void onFailure(String errorMessage) {
                                                        Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                        );

                            }
                        },
                        60,
                        false
                );
            }
        });

        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setView(v)
                .setTitle(Html.fromHtml("<font color='#000000'>" + getResources().getString(R.string.addLocality_title) +  "</font>"))
                .create();

        return dialog;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.map = googleMap;
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(37.090200, -100.712900), 3.5f));
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {
                Geocoder coder = new Geocoder(getActivity(), Locale.getDefault());
                Address address = null;
                try {
                    address = coder.getFromLocation(latLng.latitude, latLng.longitude, 1).get(0);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.d(TAG,address.toString());
                if (address != null && "US".equals(address.getCountryCode()))
                {
                    MarkerOptions marker = new MarkerOptions();
                    marker.position(latLng)
                            .title(address.getLocality());
                    map.clear();
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 7.75f));
                    map.addMarker(marker);
                    locality = address.getLocality();
                }
                else
                {
                    locality = null;
                    Toast.makeText(getActivity(), "Could not register area. Click elsewhere.", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }
}