package com.example.recievingeventapp;

import android.app.Dialog;
import android.content.Context;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


public class SelectAreaCodeFragment extends DialogFragment {

    public interface SelectedCodeHandler
    {
        public void setCode(Long code);
    }


    private static final String PHONE_NUMBER_KEY = "phone number";
    private int selectedPosition = -1;
    private String phoneNumber;
    private SelectedCodeHandler handler;

    public SelectAreaCodeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        handler = (SelectedCodeHandler) getActivity();
    }

    public static SelectAreaCodeFragment newInstance(String phoneNumber) {
        SelectAreaCodeFragment fragment = new SelectAreaCodeFragment();
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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_select_area_code, container, false);
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View v = onCreateView(getLayoutInflater(), null, savedInstanceState);
        ListView listView = (ListView) v.findViewById(R.id.listOfCodes);
        listView.setAdapter(new CodeAdapter(getActivity()));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                selectedPosition = i;
                ((CodeAdapter) listView.getAdapter()).notifyDataSetChanged();
            }
        });
        Button button = (Button) v.findViewById(R.id.actionButton);
        button.setText(R.string.selectAreaCode);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedPosition == -1)
                {
                    Toast.makeText(getContext(), "Please select a code.", Toast.LENGTH_SHORT).show();
                    return;
                }
                else
                {
                    handler.setCode((Long) listView.getAdapter().getItem(selectedPosition));
                    dismiss();
                }
            }
        });

        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setView(v)
                .setTitle(Html.fromHtml("<font color='#fc5000'>" + getResources().getString(R.string.selectAreaCode_title) +  "</font>"))
                .create();
        return dialog;
    }

    private class CodeAdapter extends ArrayAdapter<Long>
    {

        public CodeAdapter(@NonNull Context context) {
            super(context, 0);
            new RequestHandler().runRequest(
                    new Runnable() {
                        @Override
                        public void run() {
                            ArrayList<Long> codes = new ArrayList<>();
                            new FirebaseAccessor().getAreaCodes(
                                    getContext(),
                                    phoneNumber,
                                    codes,
                                    new FirebaseAccessor.FirebaseListener() {
                                        @Override
                                        public void onSuccess(String successMessage) {
                                            Toast.makeText(getContext(), successMessage, Toast.LENGTH_SHORT).show();
                                            addAll(codes);
                                        }

                                        @Override
                                        public void onFailure(String errorMessage) {
                                            Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    },
                    0,
                    false
            );
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null)
            {
                convertView =  getLayoutInflater().inflate(R.layout.code_item, null);
            }
            ((TextView)convertView.findViewById(R.id.areaCode)).setText(getItem(position).toString());
            if (selectedPosition == position)
            {
                ImageView badgeView = (ImageView) convertView.findViewById(R.id.badge);
                badgeView.setImageDrawable(getResources().getDrawable(R.drawable.badge));
            }
            else
            {
                ImageView badgeView = (ImageView) convertView.findViewById(R.id.badge);
                badgeView.setImageDrawable(getResources().getDrawable(R.drawable.empty));
            }
            return convertView;
        }
    }
}