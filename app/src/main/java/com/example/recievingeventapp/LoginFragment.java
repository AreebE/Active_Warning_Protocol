package com.example.recievingeventapp;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;


public class LoginFragment extends Fragment {

    private static final String TAG = "LoginFragment";

    public interface LoginHandler
    {
        public void onLogin(String phoneNumber);
    }

    private LoginHandler loginHandler;

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        loginHandler = (LoginHandler) getActivity();
    }

    public static LoginFragment newInstance() {
        LoginFragment fragment = new LoginFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View displayView = inflater.inflate(R.layout.fragment_login, container, false);
        EditText phoneNumber = displayView.findViewById(R.id.phoneNumber);
        new FirebaseAccessor().createEvent
                (
                        "First Cloud",
                        "206",
                        FirebaseAccessor.SHOOTING_EVENT,
                        "I want to make a test",
                        new LatLng(24.4020, 49.3103),
                        getActivity());
        Button loginButton = displayView.findViewById(R.id.login_button);
        Log.d(TAG, loginButton.toString());
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "clicked login");
                new RequestHandler().runRequest(
                        new Runnable()
                        {
                            @Override
                            public void run() {
                                new FirebaseAccessor()
                                        .verifyUser
                                                (
                                                        getContext(),
                                                        phoneNumber.getText().toString(),
                                                        new FirebaseAccessor.FirebaseListener()
                                                        {
                                                            @Override
                                                            public void onSuccess(String successMessage) {
                                                                Toast.makeText(getContext(), successMessage, Toast.LENGTH_SHORT).show();
                                                                loginHandler.onLogin(phoneNumber.getText().toString());
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
        });

        Button createButton = displayView.findViewById(R.id.createButton);
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SubscriptionFragment.newInstance().show(getChildFragmentManager(), TAG);
            }
        });
        return displayView;
    }






}