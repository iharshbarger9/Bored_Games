package com.example.boredgames;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.UUID;

public class PlayActivity extends AppCompatActivity {

     private enum Slot { X, O, empty }

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            // Use a temporary object that is later assigned to mmServerSocket
            // because mmServerSocket is final.
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code.
                tmp = BluetoothAdapter.getDefaultAdapter().listenUsingRfcommWithServiceRecord(APP_NAME, MY_UUID);
            } catch (IOException e) {
                Log.e("socket listen() failed", "Socket's listen() method failed", e);
            }
            mmServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned.
            while (true) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    Log.e("socket accept() failed", "Socket's accept() method failed", e);
                    break;
                }

                if (socket != null) {
                    // A connection was accepted. Perform work associated with
                    // the connection in a separate thread.
                    manageMyConnectedSocket(socket);
                    try {
                        mmServerSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }

        // Closes the connect socket and causes the thread to finish.
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e("cnnct socket not closed", "Could not close the connect socket", e);
            }
        }
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket
            // because mmSocket is final.
            BluetoothSocket tmp = null;
            mmDevice = device;

            try {
                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                // MY_UUID is the app's UUID string, also used in the server code.
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e("socket create failed", "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            BluetoothAdapter.getDefaultAdapter().cancelDiscovery();

            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    Log.e("client scket not closed", "Could not close the client socket", closeException);
                }
                return;
            }

            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
            manageMyConnectedSocket(mmSocket);
        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e("client scket not closed", "Could not close the client socket", e);
            }
        }
    }

    public class AcceptThreadTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            AcceptThread acceptThread = new AcceptThread();
            acceptThread.run();
            acceptThread.cancel();
            return true;
        }
        @Override
        protected void onPostExecute(Boolean result) {
        }
    }

    private class ConnectThreadTask extends AsyncTask<BluetoothDevice, Void, Boolean> {
        @Override
        protected Boolean doInBackground(BluetoothDevice... devices) {
            ConnectThread connectThread = new ConnectThread(devices[0]);
            connectThread.run();
            return true;
        }
        @Override
        protected void onPostExecute(Boolean result) {
        }
    }

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
                case MyBluetoothService.MessageConstants.MESSAGE_WRITE:
                    Log.e("message sent", "message sent");
                    break;
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
                            ((MyApplication) activity.getApplication()).setOpponentDisplayName(value);
                            ((TextView) activity.findViewById(R.id.tv_opponent_display_name)).setText(value);
                            activity.closeSocket();
                            break;
                        case "newMove":
                            // value is the opponent's move
                            ImageView iv;
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
                                case " ":
                                    break;
                            }
                            Log.e("got opponents move", value);
                            // Update that it's my turn
                            activity.closeSocket();
                            break;
                        case "gameWon":
                        case "gameDraw":
                            // value is the opponent's move
                            // ran by the device that did not make the game-ending move
                            ImageView i;
                            switch(value) {
                                case "a1":
                                    i = (ImageView) activity.findViewById(R.id.ibt_a1);
                                    activity.updateSlot(i, 0, activity.theirPiece(activity.amPlayer1, activity.player1IsX));
                                    break;
                                case "a2":
                                    i = (ImageView) activity.findViewById(R.id.ibt_a2);
                                    activity.updateSlot(i, 1, activity.theirPiece(activity.amPlayer1, activity.player1IsX));
                                    break;
                                case "a3":
                                    i = (ImageView) activity.findViewById(R.id.ibt_a3);
                                    activity.updateSlot(i, 2, activity.theirPiece(activity.amPlayer1, activity.player1IsX));
                                    break;
                                case "b1":
                                    i = (ImageView) activity.findViewById(R.id.ibt_b1);
                                    activity.updateSlot(i, 3, activity.theirPiece(activity.amPlayer1, activity.player1IsX));
                                    break;
                                case "b2":
                                    i = (ImageView) activity.findViewById(R.id.ibt_b2);
                                    activity.updateSlot(i, 4, activity.theirPiece(activity.amPlayer1, activity.player1IsX));
                                    break;
                                case "b3":
                                    i = (ImageView) activity.findViewById(R.id.ibt_b3);
                                    activity.updateSlot(i, 5, activity.theirPiece(activity.amPlayer1, activity.player1IsX));
                                    break;
                                case "c1":
                                    i = (ImageView) activity.findViewById(R.id.ibt_c1);
                                    activity.updateSlot(i, 6, activity.theirPiece(activity.amPlayer1, activity.player1IsX));
                                    break;
                                case "c2":
                                    i = (ImageView) activity.findViewById(R.id.ibt_c2);
                                    activity.updateSlot(i, 7, activity.theirPiece(activity.amPlayer1, activity.player1IsX));
                                    break;
                                case "c3":
                                    i = (ImageView) activity.findViewById(R.id.ibt_c3);
                                    activity.updateSlot(i, 8, activity.theirPiece(activity.amPlayer1, activity.player1IsX));
                                    break;
                                case " ":
                                    break;
                            }
                            Log.e("got opponents move", value);
                            if (activity.myPiece(activity.amPlayer1, activity.player1IsX) == Slot.X) {
                                // The game ended and this player was X, meaning the other player ended the game as O, meaning it's still their turn and this player must be ready for their move
                                ((MyApplication) activity.getApplication()).executeAcceptThreadTask();
                            }
                            activity.player1IsX = !activity.player1IsX;

                            ((MyApplication) activity.getApplication()).setIsMyTurn(activity.amPlayer1 == activity.player1IsX); // For at the end of games
                            activity.clearBoard();
                            activity.setXorOImageViews(activity.amPlayer1, activity.player1IsX);
                            activity.closeSocket();
                            break;
                    }
                    break;
                // ...
            }
        }
    }

    private boolean amPlayer1;
    private Boolean player1IsX = true;
    private final MyHandler myHandler = new MyHandler(this);
    private Slot[] board = new Slot[9];
    public UUID MY_UUID = UUID.fromString("c6fccb66-aa27-11eb-bcbc-0242ac130002");
    public String APP_NAME = "Bored Games";
    public String myMoveToSend = " ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        updateDisplayNameViews();

        // Nothing else should happen if there is no remote device
        if (((MyApplication) getApplication()).getDevice() == null) {
            Toast.makeText(getApplicationContext(), R.string.play_no_one_to_play_against,Toast.LENGTH_SHORT).show();
            return;
        }

        // Arbitrarily choose player1 based on device names
        if ((int) ((MyApplication) getApplication()).getSocket().getRemoteDevice().getName().getBytes()[0] < (int) BluetoothAdapter.getDefaultAdapter().getName().getBytes()[0]) {
            amPlayer1 = true;
            ((MyApplication) getApplication()).setIsMyTurn(player1IsX == amPlayer1);
            Toast.makeText(getApplicationContext(), R.string.play_youre_player_one,Toast.LENGTH_SHORT).show();
        } else {
            amPlayer1 = false;
            ((MyApplication) getApplication()).setIsMyTurn(player1IsX == amPlayer1);
            Toast.makeText(getApplicationContext(), R.string.play_youre_player_two,Toast.LENGTH_SHORT).show();
        }

        // Appropriately set imageViews with X or O
        setXorOImageViews(amPlayer1, player1IsX);

        startGame();
    }

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
        Log.e("on resume called", "on resume called");
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
       // savedInstanceState.putString("myDisplayName", ((MyApplication) getApplication()).getMyDisplayName());
       // savedInstanceState.putString("theirDisplayName", ((MyApplication) getApplication()).getOpponentDisplayName());
        // etc.
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        updateDisplayNameViews();
        Log.e("restoring state", "onRestoreInstanceState called");
    }

    public void a1Click(View v) {
        updateDisplayNameViews();
        if (!myTurn()) return;
        if (board[0] != Slot.empty) return;

        ((MyApplication) getApplication()).cancelAcceptThreadTask();

        // It is this player's turn and the slot they clicked on is empty
        ImageView ibt_a1 = (ImageView) findViewById(R.id.ibt_a1);
        Slot myPiece = myPiece(amPlayer1, player1IsX);
        updateSlot(ibt_a1, 0, myPiece);
        myMoveToSend = "a1";

        new ConnectThreadTask().execute(((MyApplication) getApplication()).getDevice());
    }
    public void a2Click(View v) {
        updateDisplayNameViews();
        if (!myTurn()) return;
        if (board[1] != Slot.empty) return;

        ((MyApplication) getApplication()).cancelAcceptThreadTask();

        ImageView ibt_a2 = (ImageView) findViewById(R.id.ibt_a2);
        Slot myPiece = myPiece(amPlayer1, player1IsX);
        updateSlot(ibt_a2, 1, myPiece);
        myMoveToSend = "a2";


        new ConnectThreadTask().execute(((MyApplication) getApplication()).getDevice());
    }
    public void a3Click(View v) {
        updateDisplayNameViews();
        if (!myTurn()) return;
        if (board[2] != Slot.empty) return;

        ((MyApplication) getApplication()).cancelAcceptThreadTask();

        ImageView ibt_a3 = (ImageView) findViewById(R.id.ibt_a3);
        Slot myPiece = myPiece(amPlayer1, player1IsX);
        updateSlot(ibt_a3, 2, myPiece);
        myMoveToSend = "a3";


        new ConnectThreadTask().execute(((MyApplication) getApplication()).getDevice());
    }
    public void b1Click(View v) {
        updateDisplayNameViews();
        if (!myTurn()) return;
        if (board[3] != Slot.empty) return;

        ((MyApplication) getApplication()).cancelAcceptThreadTask();

        ImageView ibt_b1 = (ImageView) findViewById(R.id.ibt_b1);
        Slot myPiece = myPiece(amPlayer1, player1IsX);
        updateSlot(ibt_b1, 3, myPiece);
        myMoveToSend = "b1";

        new ConnectThreadTask().execute(((MyApplication) getApplication()).getDevice());
    }
    public void b2Click(View v) {
        updateDisplayNameViews();
        if (!myTurn()) return;
        if (board[4] != Slot.empty) return;

        ((MyApplication) getApplication()).cancelAcceptThreadTask();

        ImageView ibt_b2 = (ImageView) findViewById(R.id.ibt_b2);
        Slot myPiece = myPiece(amPlayer1, player1IsX);
        updateSlot(ibt_b2, 4, myPiece);
        myMoveToSend = "b2";


        if (!((MyApplication) getApplication()).getSocket().isConnected()) {
            Log.e("socket disconnected", "bluetooth socket no longer connected");
        } else {
            Log.e("socket connected", "bluetooth socket still connected");
        }

        new ConnectThreadTask().execute(((MyApplication) getApplication()).getDevice());
    }
    public void b3Click(View v) {
        updateDisplayNameViews();
        if (!myTurn()) return;
        if (board[5] != Slot.empty) return;

        ((MyApplication) getApplication()).cancelAcceptThreadTask();

        ImageView ibt_b3 = (ImageView) findViewById(R.id.ibt_b3);
        Slot myPiece = myPiece(amPlayer1, player1IsX);
        updateSlot(ibt_b3, 5, myPiece);
        myMoveToSend = "b3";

        new ConnectThreadTask().execute(((MyApplication) getApplication()).getDevice());
    }
    public void c1Click(View v) {
        updateDisplayNameViews();
        if (!myTurn()) return;
        if (board[6] != Slot.empty) return;

        ((MyApplication) getApplication()).cancelAcceptThreadTask();

        ImageView ibt_c1 = (ImageView) findViewById(R.id.ibt_c1);
        Slot myPiece = myPiece(amPlayer1, player1IsX);
        updateSlot(ibt_c1, 6, myPiece);
        myMoveToSend = "c1";

        new ConnectThreadTask().execute(((MyApplication) getApplication()).getDevice());
    }
    public void c2Click(View v) {
        updateDisplayNameViews();
        if (!myTurn()) return;
        if (board[7] != Slot.empty) return;

        ((MyApplication) getApplication()).cancelAcceptThreadTask();

        ImageView ibt_c2 = (ImageView) findViewById(R.id.ibt_c2);
        Slot myPiece = myPiece(amPlayer1, player1IsX);
        updateSlot(ibt_c2, 7, myPiece);
        myMoveToSend = "c2";


        new ConnectThreadTask().execute(((MyApplication) getApplication()).getDevice());
    }
    public void c3Click(View v) {
        updateDisplayNameViews();
        if (!myTurn()) return;
        if (board[8] != Slot.empty) return;

        ((MyApplication) getApplication()).cancelAcceptThreadTask();


        ImageView ibt_c3 = (ImageView) findViewById(R.id.ibt_c3);
        Slot myPiece = myPiece(amPlayer1, player1IsX);
        updateSlot(ibt_c3, 8, myPiece);
        myMoveToSend = "c3";

        new ConnectThreadTask().execute(((MyApplication) getApplication()).getDevice());
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

        if (!myTurn()) {
            new AcceptThreadTask().execute();
        }
    }

    private Boolean gameWon() {
        if (board[0] == board[3] && board[3] == board[6] && board[0] != Slot.empty) return true;
        if (board[1] == board[4] && board[4] == board[7] && board[4] != Slot.empty) return true;
        if (board[2] == board[5] && board[5] == board[8] && board[2] != Slot.empty) return true;
        if (board[0] == board[1] && board[1] == board[2] && board[0] != Slot.empty) return true;
        if (board[3] == board[4] && board[4] == board[5] && board[4] != Slot.empty) return true;
        if (board[6] == board[7] && board[7] == board[8] && board[6] != Slot.empty) return true;
        if (board[0] == board[4] && board[4] == board[8] && board[4] != Slot.empty) return true;
        if (board[2] == board[4] && board[4] == board[6] && board[4] != Slot.empty) return true;
        return false;
    }

    private Boolean gameDraw() {
        // Checks if board is full
        if (board[0] == Slot.empty) return false;
        if (board[1] == Slot.empty) return false;
        if (board[2] == Slot.empty) return false;
        if (board[3] == Slot.empty) return false;
        if (board[4] == Slot.empty) return false;
        if (board[5] == Slot.empty) return false;
        if (board[6] == Slot.empty) return false;
        if (board[7] == Slot.empty) return false;
        if (board[8] == Slot.empty) return false;
        return true;
    }

    private Boolean gameOngoing() {
        // Checks for an empty board position
        if (board[0] != Slot.empty) return true;
        if (board[1] != Slot.empty) return true;
        if (board[2] != Slot.empty) return true;
        if (board[3] != Slot.empty) return true;
        if (board[4] != Slot.empty) return true;
        if (board[5] != Slot.empty) return true;
        if (board[6] != Slot.empty) return true;
        if (board[7] != Slot.empty) return true;
        if (board[8] != Slot.empty) return true;
        return false;
    }

    private void updateSlot(ImageView iv, int id, Slot piece) {
        // Updates board[] array and given image view
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
        // Player 1 or 2 doesn't change, which player is X (and therefore goes first), switches between games
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
        // Returns opposite of myPiece
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

    private Boolean myTurn() {
        return ((MyApplication) getApplication()).getIsMyTurn();
    }

    public void closeSocket() {
        try {
            ((MyApplication) getApplication()).getSocket().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateDisplayNameViews() {
        TextView tv_your_display_name = (TextView) findViewById(R.id.tv_your_display_name);
        tv_your_display_name.setText(((MyApplication) getApplication()).getMyDisplayName());

        TextView tv_opponent_display_name = (TextView) findViewById(R.id.tv_opponent_display_name);
        tv_opponent_display_name.setText(((MyApplication) getApplication()).getOpponentDisplayName());
    }

    public void manageMyConnectedSocket(BluetoothSocket socket) {
        Log.e("Connection established", "Connection has been established");
        //bluetoothSocket = socket;

        // Store socket in Application subclass for application-wide accessibility
        MyApplication myApp = (MyApplication) getApplication();
        myApp.setSocket(socket);
        myApp.setDevice(socket.getRemoteDevice());

        String type = "newMove,";
        if (!myMoveToSend.equals(" ")) {
            // This will be ran by the player who just made a move
            // Check if this move has ended the game and appropriately update the stats if so
            if (gameWon()) {
                type = "gameWon,";
                player1IsX = !player1IsX;
                ((MyApplication) getApplication()).setIsMyTurn(amPlayer1 == player1IsX); // For at the end of games
                this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        clearBoard();
                        setXorOImageViews(amPlayer1, player1IsX);
                    }
                });

                UpdateStats updatestats = new UpdateStats();
                updatestats.incrementWin(((MyApplication) getApplication()).getMyDisplayName());
                updatestats.incrementLoss(((MyApplication) getApplication()).getOpponentDisplayName());

            } else if (gameDraw()) {
                type = "gameDraw,";
                player1IsX = !player1IsX;
                ((MyApplication) getApplication()).setIsMyTurn(amPlayer1 == player1IsX); // For at the end of games
                this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        clearBoard();
                        setXorOImageViews(amPlayer1, player1IsX);
                    }
                });

                UpdateStats updatestats = new UpdateStats();
                updatestats.incrementDraw(((MyApplication) getApplication()).getMyDisplayName());
                updatestats.incrementDraw(((MyApplication) getApplication()).getOpponentDisplayName());

            } else {
                // Game is still going, so switch whose to not your turn, since you just made a move
                ((MyApplication) getApplication()).setIsMyTurn(false);
            }
        } else {
            // This will be ran by the player who did not just make a move, so it's their turn now
            ((MyApplication) getApplication()).setIsMyTurn(true);
        }

        (new MyBluetoothService(myHandler)).writeTask(((MyApplication) getApplication()).getSocket(), (type + myMoveToSend).getBytes());
        (new MyBluetoothService(myHandler)).readTask(((MyApplication) getApplication()).getSocket());

        myMoveToSend = " ";

        // There's a special case where player O's move ends the game. In this case, the next move is still player O (X and O switches
        // each game and X always goes first)
        AcceptThreadTask waitingToAcceptTask = new AcceptThreadTask();
        ((MyApplication) getApplication()).setAcceptThreadTask(waitingToAcceptTask);
        if (!myTurn()) {
            waitingToAcceptTask.execute();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("play act. destroyed", "Play activity has been destroyed and onDestroy() called");
        try {
            MyApplication myApp = ((MyApplication) getApplication());
            myApp.getSocket().close();
            myApp.setSocket(null);
            myApp.setDevice(null);
            myApp.setOpponentDisplayName(null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        // You can't leave in the middle of a game. Just finish the game of tic-tac-toe first.
        if (gameOngoing()) {
            // Do nothing. Player must finish the game
            return;
        } else {
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(R.string.play_leave_game)
                    .setMessage(R.string.play_are_you_sure_you_dont_want_to_play)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Close disconnect from opponent by closing socket
                            try {
                                ((MyApplication) getApplication()).getSocket().close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            ((MyApplication) getApplication()).setOpponentDisplayName(null);
                            ((MyApplication) getApplication()).setDevice(null);
                            finish();
                        }

                    })
                    .setNegativeButton("No", null)
                    .show();
        }
    }
}