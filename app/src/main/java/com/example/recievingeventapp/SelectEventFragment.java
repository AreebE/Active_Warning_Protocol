package com.example.recievingeventapp;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;


public class SelectEventFragment extends DialogFragment {

    public interface Listener
    {
        public void onEventSelected(FirebaseAccessor.EventType event);
    }

    private Listener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.listener = (Listener) getParentFragment();
    }

    private class EventAdapter extends ArrayAdapter<FirebaseAccessor.EventType>
    {
        public EventAdapter(@NonNull Context context) {
            super(context, R.layout.event_selection, FirebaseAccessor.EventType.values());
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            FirebaseAccessor.EventType current = getItem(position);
//            convertView = super.getView(position, convertView, parent);
            if (convertView == null)
            {
                convertView = getLayoutInflater().inflate(R.layout.event_selection,  null);
            }
            TextView name = convertView.findViewById(R.id.event_type);
            name.setText(current.NAME_ID);
            ImageView image = convertView.findViewById(R.id.event_icon);
            Drawable icon = getActivity().getDrawable(current.ICON_ID);
            icon.setTint(getResources().getColor(current.COLOR_ID));
//            icon.setBounds();
            image.setImageDrawable(icon);
            System.out.println(image);
            return convertView;
        }


    }

    public SelectEventFragment() {
    }


    public static SelectEventFragment newInstance() {
        SelectEventFragment fragment = new SelectEventFragment();
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
        return inflater.inflate(R.layout.fragment_select_event, container, false);
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View v = onCreateView(getLayoutInflater(), null, savedInstanceState);
        ListView listView = v.findViewById(R.id.list_of_events);
        listView.setAdapter(new EventAdapter(getContext()));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FirebaseAccessor.EventType item = ((EventAdapter) listView.getAdapter()).getItem(position);
                listener.onEventSelected(item);
                dismiss();
            }
        });


        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setView(v)
                .setTitle(Html.fromHtml("<font color='#fc5000'>" + getResources().getString(R.string.addAreaCode_title) +  "</font>"))
                .create();

        return dialog;
    }


}