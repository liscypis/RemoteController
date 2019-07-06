package com.example.remotecontroller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothThread extends Thread {
    private static final UUID MY_UUID = UUID.fromString("04b6c6fb-0000-1000-8000-00805f9b34fb");
    private static final String TAG = "BluetoothThread";

    private BluetoothAdapter bluetoothAdapter;
    private final BluetoothSocket mmSocket;
    private OutputStream mOutStream;
    private BluetoothDevice mDevice;
    private Handler mHandler;

    public BluetoothThread(String mac, BluetoothAdapter adapter, Handler handler) {
        BluetoothSocket tmp = null;
        bluetoothAdapter = adapter;
        mDevice = bluetoothAdapter.getRemoteDevice(mac);
        mHandler = handler;

        try {
            tmp = mDevice.createRfcommSocketToServiceRecord(MY_UUID);
            Log.d(TAG, "BluetoothThread: socked created");
        } catch (IOException e) {
            Log.e(TAG, "Socket's create() method failed", e);
        }
        mmSocket = tmp;
    }

    public void run() {
        bluetoothAdapter.cancelDiscovery();

        try {
            mmSocket.connect();
        } catch (IOException connectException) {
            try {
                mmSocket.close();
            } catch (IOException closeException) {
                Log.e(TAG, "Couldn't close the client socket", closeException);
            }
            sendMessageToActivity(Constants.UNABLE_TO_CONNECT);
            return;
        }

        try {
            mOutStream = mmSocket.getOutputStream();
            sendMessageToActivity(Constants.CONNECTED);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void sendMessageToActivity(String status) {
        Message msg = mHandler.obtainMessage(Constants.CONNECT_STATUS);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.MESSAGE_STATUS, status);
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    void write(byte[] ms) {
        try {
            mOutStream.write(ms);
            mOutStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Couldn't close the client socket", e);
        }
    }
}
