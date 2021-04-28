package com.example.boredgames;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ProfileDao {

    @Query("SELECT * FROM profile")
    Profile getProfile();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void setProfile(Profile profile);

    @Update
    void updateProfile(Profile profile);

}
