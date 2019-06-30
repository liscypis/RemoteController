package com.example.remotecontroller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;


import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothThread extends Thread {
    private static final UUID MY_UUID = UUID.fromString("04b6c6fb-0000-1000-8000-00805f9b34fb");
    private static final String TAG = "BluetoothThread";
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_CONNECT = 2;
    private static final int CONNECT_STATUS = 4;
    private static final String DEVICE_ADDRESS = "device_address";
    private static final String MESSAGE_STATUS = "status";
    private BluetoothAdapter bluetoothAdapter;
    private final BluetoothSocket mmSocket;
    private OutputStream mmOutStream;
    private BluetoothDevice mmDevice;
    private ObjectOutputStream mObjectOutputStreamos;
    private Handler mHandler;

    public BluetoothThread(String mac, BluetoothAdapter adapter, Handler handler) {
        BluetoothSocket tmp = null;
        bluetoothAdapter = adapter;
        mmDevice = bluetoothAdapter.getRemoteDevice(mac);
        mHandler = handler;


        try {
            // Get a BluetoothSocket to connect with the given BluetoothDevice.
            tmp = mmDevice.createRfcommSocketToServiceRecord(MY_UUID);
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
                Log.e(TAG, "Could not close the client socket", closeException);

            }
            Message msg = mHandler.obtainMessage(CONNECT_STATUS);
            Bundle bundle = new Bundle();
            bundle.putString(MESSAGE_STATUS, "Unable to connect");
            msg.setData(bundle);
            mHandler.sendMessage(msg);
            return;
        }

        try {
            mmOutStream = mmSocket.getOutputStream();

            Message msg = mHandler.obtainMessage(CONNECT_STATUS);
            Bundle bundle = new Bundle();
            bundle.putString(MESSAGE_STATUS, "connected");
            msg.setData(bundle);
            mHandler.sendMessage(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


//    void write(Message ms) {
//        String json = gson.toJson(ms);
//        try {
//            mObjectOutputStreamos.writeObject(json);
//            final byte[] utf8Bytes = json.getBytes("UTF-8");
//            Log.d(TAG, "write: " + (utf8Bytes.length)); // prints "11"
//            mObjectOutputStreamos.flush();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    void write2(byte[] ms) {
        try {
            mmOutStream.write(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    void cancel() {
        try {
//            mObjectOutputStreamos.close();
//            mmOutStream.close();
            mmSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close the client socket", e);
        }
    }
}
