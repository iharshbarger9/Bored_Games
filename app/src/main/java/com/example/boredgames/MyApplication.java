package com.example.boredgames;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

public class MyApplication extends Application {
    private BluetoothSocket socket;
    private String opponentDisplayName;
    private String myDisplayName;
    private Boolean isMyTurn;
    private MainActivity mainActivity;
    private BluetoothDevice device;
    private PlayActivity.AcceptThreadTask acceptThreadTask;

    public MainActivity getMainActivity() {
        return this.mainActivity;
    }

    public void setMainActivity(MainActivity activity) {
        this.mainActivity = activity;
    }

    public BluetoothDevice getDevice() {
        return this.device;
    }

    public void setAcceptThreadTask(PlayActivity.AcceptThreadTask t) {
        this.acceptThreadTask = t;
    }

    public void cancelAcceptThreadTask() {
        if (this.acceptThreadTask != null) {
            this.acceptThreadTask.cancel(true);
            this.acceptThreadTask = null;
        }
    }

    public void executeAcceptThreadTask() {
        this.acceptThreadTask.execute();
        //this.acceptThreadTask = null;
    }

    public void setDevice(BluetoothDevice d) {
        this.device = d;
    }

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

    public void setIsMyTurn(Boolean b) {
        isMyTurn = b;
    }

    public Boolean getIsMyTurn() {
        return isMyTurn;
    }
    public String getMyDisplayName() {
        return myDisplayName;
    }

    public void setMyDisplayName(String s) {
        myDisplayName = s;
    }
}
