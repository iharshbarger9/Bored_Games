package com.example.boredgames;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.OnLifecycleEvent;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;

public class PlayActivity extends AppCompatActivity {

     private enum Slot { X, O, empty }

    private static class MyHandler extends Handler {
        private final WeakReference<PlayActivity> mActivity;

        public MyHandler(PlayActivity activity) {
            mActivity = new WeakReference<PlayActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg)
        {
            PlayActivity activity = mActivity.get();
            switch(msg.what)
            {
                case MyBluetoothService.MessageConstants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;

                    // construct a string from the valid bytes in the buffer.
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    Log.e("message received", readMessage);
                    String[] type_value = readMessage.split(",");
                    String type = type_value[0];
                    String value = type_value[1];

                    switch (type) {
                        case "displayName":
                            // value is other device's display name
                            OTHER_PLAYERS_DISPLAY_NAME = value;
                            ((TextView) activity.findViewById(R.id.tv_opponent_display_name)).setText(OTHER_PLAYERS_DISPLAY_NAME);
                            break;
                        case "newMove":
                            // value is the opponent's move
                            ImageView iv;
                            activity.myTurn = !activity.myTurn;
                            switch(value) {
                                case "a1":
                                    iv = (ImageView) activity.findViewById(R.id.ibt_a1);
                                    activity.updateSlot(iv, 0, activity.theirPiece(activity.amPlayer1, activity.player1IsX));
                                    break;
                                case "a2":
                                    iv = (ImageView) activity.findViewById(R.id.ibt_a2);
                                    activity.updateSlot(iv, 1, activity.theirPiece(activity.amPlayer1, activity.player1IsX));
                                    break;
                                case "a3":
                                    iv = (ImageView) activity.findViewById(R.id.ibt_a3);
                                    activity.updateSlot(iv, 2, activity.theirPiece(activity.amPlayer1, activity.player1IsX));
                                    break;
                                case "b1":
                                    iv = (ImageView) activity.findViewById(R.id.ibt_b1);
                                    activity.updateSlot(iv, 3, activity.theirPiece(activity.amPlayer1, activity.player1IsX));
                                    break;
                                case "b2":
                                    iv = (ImageView) activity.findViewById(R.id.ibt_b2);
                                    activity.updateSlot(iv, 4, activity.theirPiece(activity.amPlayer1, activity.player1IsX));
                                    break;
                                case "b3":
                                    iv = (ImageView) activity.findViewById(R.id.ibt_b3);
                                    activity.updateSlot(iv, 5, activity.theirPiece(activity.amPlayer1, activity.player1IsX));
                                    break;
                                case "c1":
                                    iv = (ImageView) activity.findViewById(R.id.ibt_c1);
                                    activity.updateSlot(iv, 6, activity.theirPiece(activity.amPlayer1, activity.player1IsX));
                                    break;
                                case "c2":
                                    iv = (ImageView) activity.findViewById(R.id.ibt_c2);
                                    activity.updateSlot(iv, 7, activity.theirPiece(activity.amPlayer1, activity.player1IsX));
                                    break;
                                case "c3":
                                    iv = (ImageView) activity.findViewById(R.id.ibt_c3);
                                    activity.updateSlot(iv, 8, activity.theirPiece(activity.amPlayer1, activity.player1IsX));
                                    break;
                            }
                            break;
                    }
                    break;

                // ...
            }
        }
    }

    //static BluetoothSocket bluetoothSocket;
    //public String OTHER_PLAYERS_DEVICE_NAME;
    public static String OTHER_PLAYERS_DISPLAY_NAME;
    public static String MY_DISPLAY_NAME;
    private boolean amPlayer1;
    private Boolean player1IsX = true;
    private Boolean myTurn;
    private final MyHandler myHandler = new MyHandler(this);
    private Slot[] board = new Slot[9];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        Log.e("on create play", "onCreate() for play activity");

        // Check if there is a bluetooth socket
        if (((MyApplication) getApplication()).getSocket() == null) {
            Toast.makeText(getApplicationContext(), R.string.play_no_one_to_play_against,Toast.LENGTH_SHORT).show();
            return; // This page should do nothing if there's no opponent
        }

        // Set your display name and send your to opponent
        AppDatabase db = AppDatabase.getDatabase(getApplication());
        AppDatabase.getProfile(profile -> {
            MY_DISPLAY_NAME = profile.getDisplayName();
            (new MyBluetoothService(myHandler)).writeTask(((MyApplication) getApplication()).getSocket(), ("displayName," + MY_DISPLAY_NAME).getBytes());
            (new MyBluetoothService(myHandler)).readTask(((MyApplication) getApplication()).getSocket());
            TextView tv_your_display_name = (TextView) findViewById(R.id.tv_your_display_name);
            tv_your_display_name.setText(MY_DISPLAY_NAME);

            //Log.e("dispName gotten from db", "updated my disp name from db");
        });

        if (MY_DISPLAY_NAME != null) {
            TextView tv_your_display_name = (TextView) findViewById(R.id.tv_your_display_name);
            tv_your_display_name.setText(MY_DISPLAY_NAME);


        }
        if (OTHER_PLAYERS_DISPLAY_NAME != null) {
            TextView tv_opponent_display_name = (TextView) findViewById(R.id.tv_opponent_display_name);
            tv_opponent_display_name.setText(OTHER_PLAYERS_DISPLAY_NAME);
        }


        // Arbitrarily choose player1
        if ((int) ((MyApplication) getApplication()).getSocket().getRemoteDevice().getName().getBytes()[0] < (int) BluetoothAdapter.getDefaultAdapter().getName().getBytes()[0]) {
            amPlayer1 = true;
            myTurn = true;
            Toast.makeText(getApplicationContext(), R.string.play_youre_player_one,Toast.LENGTH_SHORT).show();
        } else {
            amPlayer1 = false;
            myTurn = false;
            Toast.makeText(getApplicationContext(), R.string.play_youre_player_two,Toast.LENGTH_SHORT).show();
        }


        // Set your display name



       // sendDisplayName();
        //(new MyBluetoothService(getOpponentDisplayNameHandler)).readTask(bluetoothSocket);


        // Appropriate set imageViews with X or O
        setXorOImageViews(amPlayer1, player1IsX);

        /*if (!amPlayer1) {
            (new MyBluetoothService(handler)).writeTask(((MyApplication) getApplication()).getSocket(), "hello world".getBytes());
        } else {
            (new MyBluetoothService(handler)).readTask(((MyApplication) getApplication()).getSocket());
        } */
        startGame();
    }

    private static final Handler handlerx = new Handler(Looper.myLooper())
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
                    Log.e("message received", readMessage);
                    String[] type_value = readMessage.split(",");
                    String type = type_value[0];
                    String value = type_value[1];

                    switch (type) {
                        case "displayName":
                            // value is other device's display name
                            OTHER_PLAYERS_DISPLAY_NAME = value;
                            break;
                        case "theirMove":
                            // value is the opponent's move
                            break;
                    }
                    break;

                // ...
            }
        }
    };

    @SuppressLint("HandlerLeak")
    private static final Handler xsendDisplayNameHandler = new Handler(Looper.myLooper())
    {
        @Override
        public void handleMessage(Message msg) {
            Log.e("received display name", "received other players display name");

            switch (msg.what) {
                case MyBluetoothService.MessageConstants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;

                    // construct a string from the valid bytes in the buffer.
                    String theirDisplayName = new String(readBuf, 0, msg.arg1);
                    OTHER_PLAYERS_DISPLAY_NAME = theirDisplayName;

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

    @Override
    protected void onResume() {
        super.onResume();
     /*   // Set your display name
        AppDatabase.getDatabase(getApplication());
        AppDatabase.getProfile(prof -> {
            YOUR_DISPLAY_NAME = prof.getDisplayName();
            (new MyBluetoothService(sendDisplayNameHandler)).writeTask(bluetoothSocket, YOUR_DISPLAY_NAME.getBytes());
            (new MyBluetoothService(sendDisplayNameHandler)).readTask(bluetoothSocket);
        }); */
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        Log.e("state saved", "onSaveInstanceState called");
        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.
        savedInstanceState.putString("myDisplayName", MY_DISPLAY_NAME);
        savedInstanceState.putString("theirDisplayName", OTHER_PLAYERS_DISPLAY_NAME);
        // etc.
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        String myDisplayName = savedInstanceState.getString("myDisplayName");
        String theirDisplayName = savedInstanceState.getString("theirDisplayName");

        MY_DISPLAY_NAME = myDisplayName;
        TextView tv_your_display_name = (TextView) findViewById(R.id.tv_your_display_name);
        tv_your_display_name.setText(MY_DISPLAY_NAME);

        OTHER_PLAYERS_DISPLAY_NAME = theirDisplayName;
        TextView tv_opponent_display_name = (TextView) findViewById(R.id.tv_opponent_display_name);
        tv_opponent_display_name.setText(OTHER_PLAYERS_DISPLAY_NAME);

    }

    public void a1Click(View v) {
        if (!myTurn) return;
        if (board[0] != Slot.empty) return;

        // It is this player's turn and the slot they clicked on is empty
        ImageView ibt_a1 = (ImageView) findViewById(R.id.ibt_a1);
        Slot myPiece = myPiece(amPlayer1, player1IsX);
        updateSlot(ibt_a1, 0, myPiece);

        (new MyBluetoothService(myHandler)).writeTask(((MyApplication) getApplication()).getSocket(), "newMove,a1".getBytes());

        myTurn = false;
    }
    public void a2Click(View v) {
        if (!myTurn) return;
        if (board[1] != Slot.empty) return;

        ImageView ibt_a2 = (ImageView) findViewById(R.id.ibt_a1);
        Slot myPiece = myPiece(amPlayer1, player1IsX);
        updateSlot(ibt_a2, 1, myPiece);


        (new MyBluetoothService(myHandler)).writeTask(((MyApplication) getApplication()).getSocket(), "newMove,a2".getBytes());
        myTurn = false;


    }
    public void a3Click(View v) {
        if (!myTurn) return;
        if (board[2] != Slot.empty) return;

        ImageView ibt_a3 = (ImageView) findViewById(R.id.ibt_a1);
        Slot myPiece = myPiece(amPlayer1, player1IsX);
        updateSlot(ibt_a3, 2, myPiece);


        (new MyBluetoothService(myHandler)).writeTask(((MyApplication) getApplication()).getSocket(), "newMove,a3".getBytes());
        myTurn = false;

    }
    public void b1Click(View v) {
        if (!myTurn) return;
        if (board[3] != Slot.empty) return;

        ImageView ibt_b1 = (ImageView) findViewById(R.id.ibt_a1);
        Slot myPiece = myPiece(amPlayer1, player1IsX);
        updateSlot(ibt_b1, 3, myPiece);


        (new MyBluetoothService(myHandler)).writeTask(((MyApplication) getApplication()).getSocket(), "newMove,b1".getBytes());
        myTurn = false;

    }
    public void b2Click(View v) {
        if (!myTurn) return;
        if (board[4] != Slot.empty) return;

        ImageView ibt_b2 = (ImageView) findViewById(R.id.ibt_a1);
        Slot myPiece = myPiece(amPlayer1, player1IsX);
        updateSlot(ibt_b2, 4, myPiece);


        (new MyBluetoothService(myHandler)).writeTask(((MyApplication) getApplication()).getSocket(), "newMove,b2".getBytes());
        myTurn = false;

    }
    public void b3Click(View v) {
        if (!myTurn) return;
        if (board[5] != Slot.empty) return;

        ImageView ibt_b3 = (ImageView) findViewById(R.id.ibt_a1);
        Slot myPiece = myPiece(amPlayer1, player1IsX);
        updateSlot(ibt_b3, 5, myPiece);


        (new MyBluetoothService(myHandler)).writeTask(((MyApplication) getApplication()).getSocket(), "newMove,b3".getBytes());
        myTurn = false;

    }
    public void c1Click(View v) {
        if (!myTurn) return;
        if (board[6] != Slot.empty) return;

        ImageView ibt_c1 = (ImageView) findViewById(R.id.ibt_a1);
        Slot myPiece = myPiece(amPlayer1, player1IsX);
        updateSlot(ibt_c1, 6, myPiece);


        (new MyBluetoothService(myHandler)).writeTask(((MyApplication) getApplication()).getSocket(), "newMove,c1".getBytes());
        myTurn = false;

    }
    public void c2Click(View v) {
        if (!myTurn) return;
        if (board[7] != Slot.empty) return;

        ImageView ibt_c2 = (ImageView) findViewById(R.id.ibt_a1);
        Slot myPiece = myPiece(amPlayer1, player1IsX);
        updateSlot(ibt_c2, 7, myPiece);


        (new MyBluetoothService(myHandler)).writeTask(((MyApplication) getApplication()).getSocket(), "newMove,c2".getBytes());
        myTurn = false;

    }
    public void c3Click(View v) {
        if (!myTurn) return;
        if (board[8] != Slot.empty) return;

        ImageView ibt_c3 = (ImageView) findViewById(R.id.ibt_a1);
        Slot myPiece = myPiece(amPlayer1, player1IsX);
        updateSlot(ibt_c3, 8, myPiece);


        (new MyBluetoothService(myHandler)).writeTask(((MyApplication) getApplication()).getSocket(), "newMove,c3".getBytes());
        myTurn = false;

    }

    private void clearBoard() {
        ImageView ibt_a1 = (ImageView) findViewById(R.id.ibt_a1);
        ImageView ibt_a2 = (ImageView) findViewById(R.id.ibt_a2);
        ImageView ibt_a3 = (ImageView) findViewById(R.id.ibt_a3);
        ImageView ibt_b1 = (ImageView) findViewById(R.id.ibt_b1);
        ImageView ibt_b2 = (ImageView) findViewById(R.id.ibt_b2);
        ImageView ibt_b3 = (ImageView) findViewById(R.id.ibt_b3);
        ImageView ibt_c1 = (ImageView) findViewById(R.id.ibt_c1);
        ImageView ibt_c2 = (ImageView) findViewById(R.id.ibt_c2);
        ImageView ibt_c3 = (ImageView) findViewById(R.id.ibt_c3);

        ibt_a1.setImageDrawable(null);
        ibt_a2.setImageDrawable(null);
        ibt_a3.setImageDrawable(null);
        ibt_b1.setImageDrawable(null);
        ibt_b2.setImageDrawable(null);
        ibt_b3.setImageDrawable(null);
        ibt_c1.setImageDrawable(null);
        ibt_c2.setImageDrawable(null);
        ibt_c3.setImageDrawable(null);

        for (int i = 0; i < 9; i++) {
            board[i] = Slot.empty;
        }
    }

    private void startGame() {
        clearBoard();

        if (!myTurn) {
            (new MyBluetoothService(myHandler)).readTask(((MyApplication) getApplication()).getSocket());
        }

    }

    private Boolean gameOver() {
        if (board[0] == board[3] && board[3] == board[6]) return true;
        if (board[1] == board[4] && board[4] == board[7]) return true;
        if (board[2] == board[5] && board[5] == board[8]) return true;
        if (board[0] == board[1] && board[1] == board[2]) return true;
        if (board[3] == board[4] && board[4] == board[5]) return true;
        if (board[6] == board[7] && board[7] == board[8]) return true;
        if (board[0] == board[4] && board[4] == board[8]) return true;
        if (board[2] == board[4] && board[4] == board[6]) return true;
        return false;
    }

    private void updateSlot(ImageView iv, int id, Slot piece) {
        Drawable drawable = null;
        if (piece == Slot.empty) {
            drawable = null;
        } else if (piece == Slot.X) {
            drawable = getResources().getDrawable(R.drawable.ttt_x);
        } else if (piece == Slot.O) {
            drawable = getResources().getDrawable(R.drawable.ttt_o);
        }
        iv.setImageDrawable(drawable);
        board[id] = piece;
    }

    private Slot myPiece(Boolean amPlayer1, Boolean player1IsX) {
        if (amPlayer1) {
            if (player1IsX) {
                return Slot.X;
            } else {
                return Slot.O;
            }
        } else {
            if (player1IsX) {
                return Slot.O;
            } else {
                return Slot.X;
            }
        }
    }

    private Slot theirPiece(Boolean amPlayer1, Boolean player1IsX) {
        if (amPlayer1) {
            if (player1IsX) {
                return Slot.O;
            } else {
                return Slot.X;
            }
        } else {
            if (player1IsX) {
                return Slot.X;
            } else {
                return Slot.O;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("play act. destroyed", "Play activity has been destroyed and onDestroy() called");
    }

}