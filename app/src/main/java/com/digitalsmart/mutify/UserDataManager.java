package com.digitalsmart.mutify;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;

//ViewModel class for managing user data in a singleton manner
//create an instance of UserDataManager when app starts
//call the methods below to access/modify/save user data
public class UserDataManager
{
    private final ArrayList <UserLocation> locations = new ArrayList<>();
    private final LocationsAdapter adapter;

    //constructor of the view model
    //add anything in the constructor to be initialized when the view model is created
    public UserDataManager()
    {
        getUserData();
        adapter = new LocationsAdapter();
    }

    //call this method to retrieve the location list from a local file or a database
    public void getUserData()
    {
        //todo: get saved locations here
    }

    //call this method to save the list to a local file or a database
    public void updateUserData()
    {
        adapter.notifyDataSetChanged();
        //todo: update current save of user data
    }

    //call this method to add a location to the list
    public void add(UserLocation location)
    {
        locations.add(location);
        updateUserData();
        //todo: scroll the recyclerview to the new item added
    }

    //call this method to remove saved location from the list
    public void remove(UserLocation location)
    {
        locations.remove(location);
        updateUserData();
    }

    //return the RecyclerView adapter
    public LocationsAdapter getAdapter()
    {
        return this.adapter;
    }


    //custom adapter for the list
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
