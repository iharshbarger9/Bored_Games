package com.example.boredgames;

import android.app.Application;
import android.bluetooth.BluetoothSocket;

public class MyApplication extends Application {
    private BluetoothSocket socket;
    private String opponentDisplayName;

    public BluetoothSocket getSocket() {
        return socket;
    }

    public String getOpponentDisplayName() {
        return opponentDisplayName;
    }

    public void setSocket(BluetoothSocket s) {
        socket = s;
    }

    public void setOpponentDisplayName(String s) {
        opponentDisplayName = s;
    }

}
