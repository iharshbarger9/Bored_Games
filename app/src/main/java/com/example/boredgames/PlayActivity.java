package com.example.boredgames;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothSocket;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class PlayActivity extends AppCompatActivity {

    static BluetoothSocket bluetoothSocket;
    public String OTHER_PLAYERS_DEVICE_NAME;
    public String OTHER_PLAYERS_DISPLAY_NAME;
    public String YOUR_DISPLAY_NAME;
    private boolean amPlayer1;
    private Boolean player1IsX = true;
    private Boolean myTurn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        bluetoothSocket = MainActivity.bluetoothSocket;
        OTHER_PLAYERS_DEVICE_NAME = MainActivity.OTHER_PLAYERS_DEVICE_NAME;

        // Check if there is a bluetooth socket
        if (bluetoothSocket == null) {
            Toast.makeText(getApplicationContext(), R.string.play_no_one_to_play_against,Toast.LENGTH_SHORT).show();
            return; // This page should do nothing if there's no opponent
        }

        // We let player1 be the person who entered the other's device name
        if (OTHER_PLAYERS_DEVICE_NAME != null) {
            amPlayer1 = true;
            Toast.makeText(getApplicationContext(), R.string.play_youre_player_one,Toast.LENGTH_SHORT).show();
        } else {
            amPlayer1 = false;
            Toast.makeText(getApplicationContext(), R.string.play_youre_player_two,Toast.LENGTH_SHORT).show();
        }

        // Set your display name
        AppDatabase.getDatabase(getApplication());
        AppDatabase.getProfile(prof -> {
            YOUR_DISPLAY_NAME = prof.getDisplayName();
            (new MyBluetoothService(sendDisplayNameHandler)).writeTask(bluetoothSocket, YOUR_DISPLAY_NAME.getBytes());
            (new MyBluetoothService(sendDisplayNameHandler)).readTask(bluetoothSocket);
        });


       // sendDisplayName();
        //(new MyBluetoothService(getOpponentDisplayNameHandler)).readTask(bluetoothSocket);


        // Appropriate set imageViews with X or O
        setXorOImageViews(amPlayer1, player1IsX);

        /*if (amPlayer1) {
            (new MyBluetoothService(handler)).writeTask(bluetoothSocket, "hello world".getBytes());
        } else {
            (new MyBluetoothService(handler)).readTask(bluetoothSocket);
        } */

    }

    @SuppressLint("HandlerLeak")
    private final Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch(msg.what)
            {
                case MyBluetoothService.MessageConstants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;

                    // construct a string from the valid bytes in the buffer.
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    Toast.makeText(getApplicationContext(), readMessage, Toast.LENGTH_SHORT).show();
                    break;

                // ...
            }
        }
    };

    @SuppressLint("HandlerLeak")
    private final Handler sendDisplayNameHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch(msg.what)
            {
                case MyBluetoothService.MessageConstants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;

                    // construct a string from the valid bytes in the buffer.
                    String displayName = new String(readBuf, 0, msg.arg1);
                    OTHER_PLAYERS_DISPLAY_NAME = displayName;
                    TextView tv_your_display_name = (TextView) findViewById(R.id.tv_your_display_name);
                    tv_your_display_name.setText(YOUR_DISPLAY_NAME);

                    TextView tv_opponent_display_name = (TextView) findViewById(R.id.tv_opponent_display_name);
                    tv_opponent_display_name.setText(OTHER_PLAYERS_DISPLAY_NAME);
                    break;

                // ...
            }
        }
    };

    private void setXorOImageViews(Boolean amPlayer1, Boolean player1IsX) {
        ImageView iv_play_this_player_token = (ImageView) findViewById(R.id.iv_play_this_player_token);
        ImageView iv_play_other_player_token = (ImageView) findViewById(R.id.iv_play_other_player_token);

        if (player1IsX) {
            if (amPlayer1) {
                iv_play_this_player_token.setImageDrawable(getResources().getDrawable(R.drawable.ttt_x));
                iv_play_other_player_token.setImageDrawable(getResources().getDrawable(R.drawable.ttt_o));
            } else {
                iv_play_this_player_token.setImageDrawable(getResources().getDrawable(R.drawable.ttt_o));
                iv_play_other_player_token.setImageDrawable(getResources().getDrawable(R.drawable.ttt_x));
            }
        } else {
            if (amPlayer1) {
                iv_play_this_player_token.setImageDrawable(getResources().getDrawable(R.drawable.ttt_o));
                iv_play_other_player_token.setImageDrawable(getResources().getDrawable(R.drawable.ttt_x));
            } else {
                iv_play_this_player_token.setImageDrawable(getResources().getDrawable(R.drawable.ttt_x));
                iv_play_other_player_token.setImageDrawable(getResources().getDrawable(R.drawable.ttt_o));
            }
        }
    }

}