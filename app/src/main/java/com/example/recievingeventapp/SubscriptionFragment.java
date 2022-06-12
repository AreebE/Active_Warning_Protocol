package com.example.recievingeventapp;

import android.app.Dialog;
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
import android.widget.EditText;
import android.widget.Toast;

import java.util.HashSet;


public class SubscriptionFragment extends DialogFragment {

    private static final HashSet<Character> NUMBERS = new HashSet<Character>()
    {{
        add('1');
        add('2');
        add('3');
        add('4');
        add('5');
        add('6');
        add('7');
        add('8');
        add('9');
        add('0');
    }};
    private static final String TAG = "createUserFragment";

    public SubscriptionFragment() {
        // Required empty public constructor
    }


    public static SubscriptionFragment newInstance() {
        SubscriptionFragment fragment = new SubscriptionFragment();
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
        return inflater.inflate(R.layout.fragment_subscription, container, false);
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View v = onCreateView(getLayoutInflater(), null, savedInstanceState);
        EditText phoneNumber = v.findViewById(R.id.phoneNumber);
        EditText areaCode = v.findViewById(R.id.areaCode);
        Button create = v.findViewById(R.id.createUser);
        create.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View view) {
                String phoneNum = phoneNumber.getText().toString();
                for (int i = 0; i < phoneNum.length(); i++)
                {
                    if (!NUMBERS.contains(phoneNum.charAt(i)))
                    {
                        break;
                    }
                }
                if (phoneNum.length() != 10)
                {
                    Toast.makeText(getContext(), "Please add a phone number with only numbers -- no spaces or dashes.", Toast.LENGTH_SHORT).show();
                    return;
                }
                try
                {
                    Long.parseLong(areaCode.getText().toString());
                } catch (NumberFormatException nfe)
                {
                    Toast.makeText(getContext(), "Please write an actual area code.", Toast.LENGTH_SHORT).show();
                    return;
                }
                FirebaseAccessor.FirebaseListener firebaseListener = new FirebaseAccessor.FirebaseListener() {
                    @Override
                    public void onSuccess(String successMessage) {
                        Toast.makeText(getContext(), successMessage, Toast.LENGTH_SHORT).show();
                        dismiss();
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        Log.d(TAG, "this thread");
                        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                    }
                };
                new RequestHandler().runRequest(
                        new Runnable() {
                                @Override
                                public void run() {

                                    new FirebaseAccessor().createAccount
                                            (
                                                    getContext(),
                                                    phoneNumber.getText().toString(),
                                                    Long.parseLong(areaCode.getText().toString()),
                                                    firebaseListener
                                            );

                                }
                        },
                        0,
                        false
                );
            }
        });

        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setView(v)
                .setTitle(Html.fromHtml("<font color='#fc5000'>" + getResources().getString(R.string.subscription) +  "</font>"))
                .create();

        return dialog;
    }
}