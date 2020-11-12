package com.digitalsmart.mutify.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import com.digitalsmart.mutify.model.UserLocation;



@Database(entities = {UserLocation.class}, version = 1)
public abstract class UserLocationDatabase extends RoomDatabase
{
    public abstract UserLocationDAO userLocationDAO();
}
