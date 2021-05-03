package com.example.boredgames;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Update;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    public String APP_NAME = "Bored Games";
    public UUID MY_UUID = UUID.fromString("c6fccb66-aa27-11eb-bcbc-0242ac130002");
    private Integer DISCOVERABLE_DURATION = 35;
    public String OTHER_PLAYERS_DEVICE_NAME;
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    public static BluetoothSocket bluetoothSocket;

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            // Use a temporary object that is later assigned to mmServerSocket
            // because mmServerSocket is final.
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code.
                tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord(APP_NAME, MY_UUID);
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
            bluetoothAdapter.cancelDiscovery();

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


    private class AcceptThreadTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            AcceptThread acceptThread = new AcceptThread();
            acceptThread.run();
            acceptThread.cancel();
            return true;
        }
        @Override
        protected void onPostExecute(Boolean result) {
            Toast.makeText(getApplicationContext(), R.string.home_connection_successful,Toast.LENGTH_SHORT).show();
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
            Toast.makeText(getApplicationContext(), R.string.home_connection_successful,Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //UpdateStats updatestats = new UpdateStats();
        //updatestats.incrementWin("lood");

        // Set Connect To Button activity (client side)
        Button bt_home_connect_to = (Button) findViewById(R.id.bt_home_connect_to);
        bt_home_connect_to.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Integer REQUEST_ENABLE_BT = 1; // arbitrary  request code

                // Get an instance of bluetooth adapter
                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

                // Ensures that the device is bluetooth capable
                if (bluetoothAdapter == null) {
                    // Device doesn't support Bluetooth
                    Toast.makeText(getApplicationContext(), R.string.home_link_cant_use_bluetooth,Toast.LENGTH_SHORT).show();
                    return;
                }

                // Asks the user to turn on bluetooth if it isn't already
                if (!bluetoothAdapter.isEnabled()) {
                    Toast.makeText(getApplicationContext(), R.string.home_link_turn_on_bluetooth,Toast.LENGTH_SHORT).show();
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                    return;
                }

                // Check if device is already connected
                Boolean alreadyConnected = bluetoothSocket != null;                       // Implement a real check for this
                if (alreadyConnected) { return; }

                // Can now try to connect to remote device given its device name
                EditText home_et_device_name = (EditText) findViewById(R.id.home_et_device_name);
                OTHER_PLAYERS_DEVICE_NAME = home_et_device_name.getText().toString();

                Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
                BluetoothDevice otherDevice = null;
                if (pairedDevices.size() > 0) {

                    for (BluetoothDevice device : pairedDevices) {
                        String deviceName = device.getName();
                        String deviceHardwareAddress = device.getAddress(); // MAC address
                        if (deviceName.equalsIgnoreCase(OTHER_PLAYERS_DEVICE_NAME)) {
                            // Found other device in list of paired devices, so try to connect to it
                            otherDevice = device;
                            new ConnectThreadTask().execute(otherDevice);
                            break;
                        }
                    }
                    if (otherDevice == null) {
                        Toast.makeText(getApplicationContext(), R.string.home_arent_paired_with,Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), R.string.home_arent_paired_with_any_devices,Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Set Accept Connections Button activity (server side)
        Button bt_home_accept_connections = (Button) findViewById(R.id.bt_home_accept_connections);
        bt_home_accept_connections.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Integer REQUEST_ENABLE_BT = 1; // arbitrary  request code

                // Get an instance of bluetooth adapter
                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

                // Ensures that the device is bluetooth capable
                if (bluetoothAdapter == null) {
                    // Device doesn't support Bluetooth
                    Toast.makeText(getApplicationContext(), R.string.home_link_cant_use_bluetooth,Toast.LENGTH_SHORT).show();
                    return;
                }

                // Asks the user to turn on bluetooth if it isn't already
                if (!bluetoothAdapter.isEnabled()) {
                    Toast.makeText(getApplicationContext(), R.string.home_link_turn_on_bluetooth,Toast.LENGTH_SHORT).show();
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                    return;
                }

                // Check if device is already connected
                Boolean alreadyConnected = bluetoothSocket != null;                       // Implement a real check for this
                if (alreadyConnected) { return; }

                // Reaching this point means the device has bluetooth on and is not already connected and is ready to accept a connection
                new AcceptThreadTask().execute();
            }
        });

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

    public void manageMyConnectedSocket(BluetoothSocket socket) {
        Log.e("Connection established", "Connection has been established");
        bluetoothSocket = socket;

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //...

        // Don't forget to unregister the ACTION_FOUND receiver.
        //unregisterReceiver(receiver);
        try {
            bluetoothSocket.close();
        } catch (IOException e) {

        }
    }

}