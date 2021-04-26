package com.example.boredgames;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Set Play Button activity
        Button bt_home_play = (Button) findViewById(R.id.bt_home_play);
        bt_home_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(MainActivity.this, PlayActivity.class);
                MainActivity.this.startActivity(myIntent);
            }
        });

        // Set Stats Button activity
        Button bt_home_stats = (Button) findViewById(R.id.bt_home_stats);
        bt_home_stats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(MainActivity.this, StatsActivity.class);
                MainActivity.this.startActivity(myIntent);
            }
        });

        // Set Profile Button activity
        Button bt_home_profile = (Button) findViewById(R.id.bt_home_profile);
        bt_home_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(MainActivity.this, ProfileActivity.class);
                MainActivity.this.startActivity(myIntent);
            }
        });

        // Set Settings Button activity
        Button bt_home_settings = (Button) findViewById(R.id.bt_home_settings);
        bt_home_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(MainActivity.this, SettingsActivity.class);
                MainActivity.this.startActivity(myIntent);
            }
        });

    }


}