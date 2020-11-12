package com.digitalsmart.mutify.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import com.digitalsmart.mutify.model.UserLocation;

import java.util.List;

@Dao
public interface UserLocationDAO
{
    @Query("SELECT * FROM userlocation")
    List<UserLocation> getAll();

    @Insert
    void insert(UserLocation userLocation);

    @Delete
    void delete(UserLocation userLocation);
}

