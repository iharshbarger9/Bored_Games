package com.example.boredgames;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Profile {

    public Profile(String androidID, String displayName, String pfpPath) {
        this.androidID = androidID;
        this.displayName = displayName;
        this.pfpPath = pfpPath;
    }

    public String getAndroidID() {
        return androidID;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getPfpPath() {
        return pfpPath;
    }

    @PrimaryKey
    @NonNull
    public String androidID;

    @ColumnInfo(name = "display_name")
    public String displayName;

    @ColumnInfo(name = "pfp_path")
    public String pfpPath;


}
