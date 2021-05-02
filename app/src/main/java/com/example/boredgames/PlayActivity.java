package com.example.boredgames;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.widget.Toast;

public class PlayActivity extends AppCompatActivity {

    BluetoothSocket bluetoothSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        bluetoothSocket = MainActivity.bluetoothSocket;

        if (bluetoothSocket == null) {
            Toast.makeText(getApplicationContext(), R.string.play_no_one_to_play_against,Toast.LENGTH_SHORT).show();
            return;
        }

    }
}