package com.digitalsmart.mutify;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;

//ViewModel class for managing user data in a singleton manner
//create an instance of UserDataManager when app starts
//call the methods below to access/modify/save user data
public class UserDataManager
{
    private final ArrayList <UserLocation> locations = new ArrayList<>();
    private final ArrayList<Geofence> fencesToAdd;
    private final ArrayList<Geofence> fences = new ArrayList<>();
    private final LocationsAdapter adapter;
    private final GoogleMap map;
    private final GeofencingClient geofencingClient;
    private PendingIntent geofencePendingIntent;
    private final Activity activity;
    private final ArrayList<String> locationsToRemove = new ArrayList<>();

    //constructor of the view model
    //add anything in the constructor to be initialized when the view model is created
    public UserDataManager(GoogleMap map, GeofencingClient client, Activity activity)
    {
        this.map = map;
        this.activity = activity;
        this.geofencingClient = client;
        adapter = new LocationsAdapter();
        fencesToAdd = new ArrayList<>();
        getUserData();
    }

    //call this method to retrieve the location list from a local file or a database
    @SuppressLint("MissingPermission")
    public void getUserData()
    {
        //todo: retrieve saved locations from SQLite
        //todo: foreach item in retrieved list of locations,call map.addCircle and map.addMarker
        //todo: do this on a background thread



        //call generateFences after retrieving all the data (background thread finish)
        generateFences();
        if (fencesToAdd.size() > 0)
            geofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent());
    }

    //call this method to save the list to a local file or a database
    public void updateUserData()
    {
        //todo: update current save of user data

        //todo: change marker/circles if necessary
        //call map.clear() and re-draw the markers and circles
        //better implementations are also welcomed


        //if successful call notifyDataSetChanged to update UI (recyclerview)
        adapter.notifyDataSetChanged();
    }

    //call this method to add a location to the list
    @SuppressLint("MissingPermission")
    public void add(UserLocation location)
    {
        //avoid adding duplicate fences
        if (!fences.contains(location.getGeofence()))
        {
            fencesToAdd.add(location.getGeofence());
            locations.add(location);

            geofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
                    .addOnSuccessListener(activity, v -> {
                        fences.addAll(fencesToAdd);
                        fencesToAdd.clear();
                        Log.d("fences", "fences added. Fences saved: " + fences.size());

                        //draw circle and put marker on the map
                        //if geo fence is successfully added
                        map.addCircle(new CircleOptions()
                                .center(location.getLatLng())
                                .radius(location.getRadius())
                                .strokeColor(Color.RED));
                        map.addMarker(new MarkerOptions().position(location.getLatLng()));
                    })
                    .addOnFailureListener(activity, e -> Log.d("fences", e.getMessage()));

            updateUserData();
        }


        //todo: scroll the recyclerview to the new item added
    }

    //call this method to remove saved location from the list
    public void remove(UserLocation location)
    {
        locations.remove(location);
        fencesToAdd.remove(location.getGeofence());
        locationsToRemove.add(location.getId());
        Task<Void> removal = geofencingClient.removeGeofences(locationsToRemove);
        removal.addOnCompleteListener(task -> locationsToRemove.clear());
        updateUserData();
    }

    //return the RecyclerView adapter
    public LocationsAdapter getAdapter()
    {
        return this.adapter;
    }

    //retrieve the saved list of geo fences
    private void generateFences()
    {
        locations.forEach(userLocation -> fencesToAdd.add(userLocation.getGeofence()));
        fences.addAll(fencesToAdd);
    }



    //methods needed for adding new geo fences, do not call them directly
    //call add() to add new fences
    private PendingIntent getGeofencePendingIntent()
    {
        if (geofencePendingIntent != null)
        {
            return geofencePendingIntent;
        }
        Intent intent = new Intent(activity.getApplicationContext(), GeofenceBroadcastReceiver.class);
        geofencePendingIntent = PendingIntent.getBroadcast(activity.getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return geofencePendingIntent;
    }
    private GeofencingRequest getGeofencingRequest()
    {
        return new GeofencingRequest
                .Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_DWELL)
                .addGeofences(fencesToAdd)
                .build();
    }


    //custom adapter for the list
    @SuppressWarnings("rawtypes")
    private class LocationsAdapter extends RecyclerView.Adapter
    {
        @NonNull
        @NotNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType)
        {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.userlocation_list_item, parent, false);
            return new LocationViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull @NotNull RecyclerView.ViewHolder holder, int position)
        {
            UserLocation l = locations.get(position);
            LocationViewHolder locationViewHolder = (LocationViewHolder)holder;

            if (l != null) {
                locationViewHolder.name.setText(l.getName());

                //todo: remove this line
                //currently displaying location information as a string
                locationViewHolder.location.setText(l.getCountry() + " " + l.getAddressLine());
            }
        }

        @Override
        public int getItemCount() {
            return locations.size();
        }

        //custom ViewHolder for displaying each item in the list
        private class LocationViewHolder extends RecyclerView.ViewHolder
        {

            private final TextView name;

            //todo: remove this line once this TextView is replaced, see userlocation_list_item.xml
            private final TextView location;
            public LocationViewHolder(View itemView)
            {
                super(itemView);
                name = itemView.findViewById(R.id.text);

                //todo: remove this line once the TextView is replaced
                location = itemView.findViewById(R.id.location);
            }
        }
    }
}
