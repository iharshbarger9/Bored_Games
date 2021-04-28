package com.example.boredgames;

import android.content.Context;
import android.os.AsyncTask;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.net.URISyntaxException;

@Database(entities = {Profile.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase {
    public interface ProfileListener {
        void onProfileReturned(Profile profile);
    }

    public abstract ProfileDao profileDao();

    private static AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "app_database").fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    public static void setProfile(Profile profile) {

        new AsyncTask<Profile, Void, Profile> () {
            @Override
            protected Profile doInBackground(Profile... profiles) {
                INSTANCE.profileDao().setProfile(profiles[0]);
                return null;
            }

        }.execute(profile);
    }

    public static void getProfile(ProfileListener listener) {
        new AsyncTask<String, Void, Profile> () {
            protected Profile doInBackground(String... ids) {
                return INSTANCE.profileDao().getProfile();
            }

            protected void onPostExecute(Profile profile) {
                super.onPostExecute(profile);
                listener.onProfileReturned(profile);
            }
        }.execute();
    }

    public static void update(Profile profile) {
        new AsyncTask<Profile, Void, Void>() {
            @Override
            protected Void doInBackground(Profile... profiles) {
                INSTANCE.profileDao().updateProfile(profile);
                return null;
            }
        }.execute(profile);
    }
}

