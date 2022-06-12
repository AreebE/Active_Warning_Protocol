package com.example.recievingeventapp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseAccessor {

    private static final String TAG = "FirebaseAccessor";



    public interface FirebaseListener
    {
        public void onSuccess(String successMessage);
        public void onFailure(String errorMessage);
    }

    // For the user database:
    public static final String USER_DATABASE = "recipients";
    public static final String PHONE_NUMBER_KEY = "phone number";
    public static final String AREA_CODES_KEY = "area codes";

    // For the event database:
    private static final String EVENT_DATABASE = "events";
    private static final String EVENT_ITEM_LIST_KEY = "event item list";
    private static final String SINGLE_AREA_CODE_KEY = "area code";

    // For each event item:
    private static final String EVENT_DESC_KEY = "event desc";
    private static final String LAT_KEY = "latitude";
    private static final String LONG_KEY = "longitude";

    private FirebaseFirestore database;

    public FirebaseAccessor()
    {
        database = FirebaseFirestore.getInstance();
    }

    public void getEvents(
            Context context,
            Long code,
            ArrayList<MapDisplayFragment.EventItem> items,
            FirebaseListener listener)
    {
        if (!connectedToInternet(context))
        {
            listener.onFailure("Failed to Connect -- Check your Internet.");
            return;
        }
        database.collection(EVENT_DATABASE)
                .whereEqualTo(SINGLE_AREA_CODE_KEY, code)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.getResult().isEmpty())
                        {
                            listener.onFailure("Could not find anyone.");
                        }
                        else
                        {
                            List<Map<String, Object>> events = (List<Map<String, Object>>) task.getResult().getDocuments().get(0).get(EVENT_ITEM_LIST_KEY);
                            for (int i = 0; i < events.size(); i++)
                            {
                                Map<String, Object> currentEvent = events.get(i);
                                MapDisplayFragment.EventItem ei = new MapDisplayFragment.EventItem
                                        (
                                                (Double) currentEvent.get(LAT_KEY),
                                                (Double) currentEvent.get(LONG_KEY),
                                                (String) currentEvent.get(EVENT_DESC_KEY)
                                        );
                                items.add(ei);
                            }
                            listener.onSuccess("Loaded the events. Reporting them now:");
                        }
                    }
                });
    }

    public void verifyUser(
            Context c,
            String phoneNumber,
            FirebaseListener listener
    )
    {
        if (!connectedToInternet(c))
        {
            listener.onFailure("Failed to Connect -- Check your Internet.");
            return;
        }
        Log.d(TAG, "\"" + phoneNumber + "\"");
        database.collection(USER_DATABASE)
                .whereEqualTo(PHONE_NUMBER_KEY, phoneNumber)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.getResult().isEmpty())
                        {
                            listener.onFailure("It appears your account doesn't exist. :(");
                        }
                        else
                        {
                            listener.onSuccess("Your account does exist!");
                        }
                    }
                });
    }

    public void getAreaCodes(
            Context c,
            String phoneNumber,
            ArrayList<Long> areaCodeList,
            FirebaseListener listener)
    {
        if (!connectedToInternet(c))
        {
            listener.onFailure("Failed to Connect -- Check your Internet.");
            return;
        }
        database.collection(USER_DATABASE)
                .whereEqualTo(PHONE_NUMBER_KEY, phoneNumber)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.getResult().isEmpty())
                        {
                            listener.onFailure("It appears your account doesn't exist. :(");
                        }
                        else
                        {
                            Map<String, Object> data = task.getResult().getDocuments().get(0).getData();
                            List<Long> areaCodes = (ArrayList<Long>) data.get(AREA_CODES_KEY);
                            if (areaCodes.size() == 0)
                            {
                                listener.onSuccess("You don't have any area codes right now.");
                                return;
                            }
                            for (int i = 0; i < areaCodes.size(); i++)
                            {
                                areaCodeList.add(areaCodes.get(i));
                            }
                            listener.onSuccess("All your area code(s) are loaded!");
                        }
                    }
                });
    }

    public void removeAreaCode(
            Context c,
            String phoneNumber,
            Long areaCode,
            FirebaseListener listener)
    {
        if (!connectedToInternet(c))
        {
            listener.onFailure("Failed to Connect -- Check your Internet.");
            return;
        }
        database.collection(USER_DATABASE)
                .whereEqualTo(PHONE_NUMBER_KEY, phoneNumber)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.getResult().isEmpty())
                        {
                            listener.onFailure("It appears your account doesn't exist. :(");
                        }
                        else
                        {
                            Map<String, Object> data = task.getResult().getDocuments().get(0).getData();
                            List<Long> areaCodes = (ArrayList<Long>) data.get(AREA_CODES_KEY);
                            for (int i = 0; i < areaCodes.size(); i++)
                            {
                                if (areaCodes.get(i) == areaCode) {
                                    areaCodes.remove(areaCode);
                                    data.put(AREA_CODES_KEY, areaCodes);
                                    database.collection(USER_DATABASE)
                                            .document(task.getResult().getDocuments().get(0).getId())
                                            .update(data)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    listener.onSuccess("Your area code has been removed!");
                                                    TopicHandler.unsubscribeToTopic(c, areaCode + "");
                                                }
                                            });
                                    return;
                                }
                            }
                            listener.onFailure("Could not find the area code.");

                        }
                    }
                });
    }

    public void addAreaCode(
            Context c,
            String phoneNumber,
            Long areaCode,
            FirebaseListener listener)
    {
        if (!connectedToInternet(c))
        {
            listener.onFailure("Failed to Connect -- Check your Internet.");
            return;
        }
        database.collection(USER_DATABASE)
            .whereEqualTo(PHONE_NUMBER_KEY, phoneNumber)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.getResult().isEmpty())
                        {
                            listener.onFailure("It appears your account doesn't exist. :(");
                        }
                        else
                        {
                            Map<String, Object> data = task.getResult().getDocuments().get(0).getData();
                            List<Long> areaCodes = (ArrayList<Long>) data.get(AREA_CODES_KEY);
                            for (int i = 0; i < areaCodes.size(); i++)
                            {
                                if (areaCodes.get(i) == areaCode)
                                {
                                    listener.onFailure("This code has already been added.");
                                    return;
                                }
                            }
                            areaCodes.add(areaCode);
                            data.put(AREA_CODES_KEY, areaCodes);
                            database.collection(USER_DATABASE)
                                    .document(task.getResult().getDocuments().get(0).getId())
                                    .update(data)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            listener.onSuccess("Your area code has been added!");
                                            TopicHandler.subscribeToTopic(c, areaCode + "");
                                        }
                                    });
                        }
                    }
                });
    }


    public void subscribeToAreaCodes(
            Context c,
            String phoneNumber,
            FirebaseListener listener)
    {
        if (!connectedToInternet(c))
        {
            listener.onFailure("Failed to Connect -- Check your Internet.");
            return;
        }
        database.collection(USER_DATABASE)
                .whereEqualTo(PHONE_NUMBER_KEY, phoneNumber)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        List<DocumentSnapshot> snapshotList = task.getResult().getDocuments();
                        if (snapshotList.size() == 0)
                        {
                            listener.onFailure("Your account does not exist. Please sign up.");
                            return;
                        }
                        DocumentSnapshot user = snapshotList.get(0);
                        List<Long> areaCodes = ((List<Long>) user.getData().get(AREA_CODES_KEY));
                        for (int i = 0; i < areaCodes.size(); i++)
                        {
                            TopicHandler.subscribeToTopic(c, areaCodes.get(i).toString());
                        }
                        listener.onSuccess("Subscribed to area codes.");
                    }
                });
    }

    public void unsubscribeToAreaCodes(
            Context c,
            String phoneNumber,
            FirebaseListener listener)
    {
        if (!connectedToInternet(c))
        {
            listener.onFailure("Failed to Connect -- Check your Internet.");
            return;
        }
        database.collection(USER_DATABASE)
                .whereEqualTo(PHONE_NUMBER_KEY, phoneNumber)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        List<DocumentSnapshot> snapshotList = task.getResult().getDocuments();
                        if (snapshotList.size() == 0)
                        {
                            listener.onFailure("Your account does not exist. Please sign up.");
                            return;
                        }
                        DocumentSnapshot user = snapshotList.get(0);
                        List<Long> areaCodes = ((List<Long>) user.getData().get(AREA_CODES_KEY));
                        for (int i = 0; i < areaCodes.size(); i++)
                        {
                            TopicHandler.unsubscribeToTopic(c, areaCodes.get(i).toString());
                        }
                        listener.onSuccess("Unsubscribed to previous area codes.");
                    }
                });
    }

    public void createAccount(
            Context c,
            String phoneNumber,
            Long firstCode,
            FirebaseListener listener)
    {
        if (!connectedToInternet(c))
        {
            listener.onFailure("Failed to Connect -- Check your Internet.");
            return;
        }

        database.collection(USER_DATABASE)
                .whereEqualTo(PHONE_NUMBER_KEY, phoneNumber)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.getResult().isEmpty())
                        {
                            Map<String, Object> data = new HashMap<>();
                            data.put(PHONE_NUMBER_KEY, phoneNumber);
                            ArrayList<Long> areaCodes = new ArrayList<>();
                            areaCodes.add(firstCode);
                            data.put(AREA_CODES_KEY, areaCodes);
                            database.collection(USER_DATABASE)
                                    .add(data)
                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                            listener.onSuccess("Successfully created you!");
                                            TopicHandler.subscribeToTopic(c, firstCode + "");
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            listener.onFailure("Failed to add you. :(");
                                        }
                                    });
                        }
                        else
                        {
                            listener.onFailure("You already exist in this database.");
                        }
                    }
                });
    }

    public boolean connectedToInternet(
            Context c)
    {
        try
        {
            ConnectivityManager manager = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
            Network network = manager.getActiveNetwork();
            if (network != null)
            {
                return manager.getNetworkCapabilities(network).hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                        && manager.getNetworkCapabilities(network).hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
            }
            return false;
        } catch (NullPointerException npe)
        {
            return false;
        }
    }
}
