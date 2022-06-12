package com.example.recievingeventapp;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.InputMismatchException;


public class AddAreaCodeFragment extends DialogFragment {

    private static final String PHONE_NUMBER_KEY = "phone number";

    private String phoneNumber;

    public AddAreaCodeFragment() {
    }


    public static AddAreaCodeFragment newInstance(String phoneNumber) {
        AddAreaCodeFragment fragment = new AddAreaCodeFragment();
        Bundle args = new Bundle();
        args.putString(PHONE_NUMBER_KEY, phoneNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            phoneNumber = getArguments().getString(PHONE_NUMBER_KEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_area_code, container, false);
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View v = onCreateView(getLayoutInflater(), null, savedInstanceState);
        EditText areaCode = v.findViewById(R.id.areaCode);
        Button addCode = v.findViewById(R.id.addAreaCode);

        addCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try
                {
                    Long.parseLong(areaCode.getText().toString());
                } catch (NumberFormatException nfe)
                {
                    Toast.makeText(getContext(), "Please write an actual area code.", Toast.LENGTH_SHORT).show();
                    return;
                }
                new RequestHandler().runRequest(
                        new Runnable() {
                            @Override
                            public void run() {
                                new FirebaseAccessor().addAreaCode
                                        (
                                                getContext(),
                                                phoneNumber,
                                                Long.parseLong(areaCode.getText().toString()),
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
                .setTitle(Html.fromHtml("<font color='#fc5000'>" + getResources().getString(R.string.addAreaCode_title) +  "</font>"))
                .create();

        return dialog;
    }
}