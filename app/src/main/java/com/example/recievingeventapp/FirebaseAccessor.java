package com.example.recievingeventapp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseAccessor {



    public enum EventType
    {
        ACTIVE_THREAT(R.drawable.shooter_icon, R.style.active_shooting_theme, R.string.shooting_name, R.color.shoot_color),
        FIRE(R.drawable.fire_icon, R.style.fire_theme, R.string.fire_name, R.color.fire_color),
        FLOOD(R.drawable.flood_icon, R.style.flood_theme, R.string.flood_name, R.color.flood_event),
        LIGHTNING(R.drawable.lightning_icon, R.style.lightning_theme, R.string.lightning_name, R.color.lightning_event),
        EARTHQUAKE(R.drawable.earthquake_icon, R.style.earthquake_theme, R.string.earthquake_name, R.color.earthquake_event),
        TORNADO(R.drawable.tornado_icon, R.style.tornado_theme, R.string.tornado_name, R.color.tornado_event),
    CHEMICAL(R.drawable.chemical_icon, R.style.chemical_theme, R.string.chemical_name, R.color.chemical_event),
    SNOWSTORM(R.drawable.snowstorm_icon, R.style.snowstorm_theme, R.string.snowstorm_name, R.color.snowstorm_event),
    AVALANCHE(R.drawable.avalanche_icon, R.style.avalanche_theme, R.string.avalanche_name, R.color.avalanche_event),
    HEAT_WAVE(R.drawable.heat_wave, R.style.heat_wave_theme, R.string.heat_wave_name, R.color.heat_wave_event),
    COLD_WAVE(R.drawable.cold_wave_icon, R.style.cold_wave_theme, R.string.cold_wave_name, R.color.cold_wave_event);

    public final int ICON_ID;
    public final int THEME_ID;
    public final int NAME_ID;
    public final int COLOR_ID;

    EventType(int icon, int theme, int name, int color)
    {
        ICON_ID = icon;
        THEME_ID = theme;
        NAME_ID = name;
        COLOR_ID = color;
    }
}

    private static final String TAG = "FirebaseAccessor";



public interface FirebaseListener
{
    public void onSuccess(String successMessage);
    public void onFailure(String errorMessage);
}


    // For the event database:
    private static final String EVENT_DATABASE = "events";
    public static final String LOCALITY_KEY = "locality";
    public static final String EVENT_TYPE_KEY = "eventType";
    public static final String LAT_KEY = "lat";
    public static final String LONG_KEY = "lon";
    public static final String BODY_KEY = "body";


    private FirebaseFirestore database;

    public FirebaseAccessor()
    {
        database = FirebaseFirestore.getInstance();
    }

    public void getEvents(
            Context context,
            String locality,
            EventType type,
            ArrayList<MapDisplayFragment.EventItem> items,
            FirebaseListener listener)
    {
        if (!connectedToInternet(context))
        {
            listener.onFailure("Failed to Connect -- Check your Internet.");
            return;
        }
        database.collection(EVENT_DATABASE)
                .whereEqualTo(LOCALITY_KEY, locality)
                .whereEqualTo(EVENT_TYPE_KEY, type.name())
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
//                            List<Map<String, Object>> events = (List<Map<String, Object>>) task.getResult().getDocuments().get(0).get(EVENT_ITEM_LIST_KEY);
                            List<DocumentSnapshot> events = task.getResult().getDocuments();
                            for (int i = 0; i < events.size(); i++)
                            {
                                Map<String, Object> currentEvent = events.get(i).getData();
                                MapDisplayFragment.EventItem ei = new MapDisplayFragment.EventItem
                                        (
                                                (Double) currentEvent.get(LAT_KEY),
                                                (Double) currentEvent.get(LONG_KEY),
                                                (String) currentEvent.get(BODY_KEY)
                                        );
                                items.add(ei);
                            }
                            listener.onSuccess("Loaded the events. Reporting them now:");
                        }
                    }
                });
    }


    public void removeLocality(
            Context c,
            String locality,
            FirebaseListener listener)
    {
        if (!connectedToInternet(c))
        {
            listener.onFailure("Failed to Connect -- Check your Internet.");
            return;
        }
        TopicHandler.unsubscribeToTopic(c, locality + "");
    }

    public void addLocality(
            Context c,
            String locality,
            FirebaseListener listener)
    {
        if (!connectedToInternet(c))
        {
            listener.onFailure("Failed to Connect -- Check your Internet.");
            return;
        }
        TopicHandler.subscribeToTopic(c, locality + "");
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

    public void createEvent(
            String locality,
            EventType type,
            String body,
            LatLng coordinates,
            Context context
    )
    {
        if (!connectedToInternet(context))
        {
            return;
        }
        Map<String, Object> data = new HashMap<>();
        data.put(LOCALITY_KEY, locality);
        data.put(EVENT_TYPE_KEY, type.name());
        data.put(BODY_KEY, body);
        data.put(LAT_KEY, coordinates.latitude);
        data.put(LONG_KEY, coordinates.longitude);
        database.collection(EVENT_DATABASE)
                .add(data)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(context, "Can create!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
