package com.example.remotecontroller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;


import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothThread extends Thread {
    private static final UUID MY_UUID = UUID.fromString("04b6c6fb-0000-1000-8000-00805f9b34fb");
    private static final String TAG = "MainActivity";
    private BluetoothAdapter bluetoothAdapter;
    private final BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;
    private OutputStream mmOutStream;
    private ObjectOutputStream mObjectOutputStreamos;
    private Gson gson = new Gson();

    public BluetoothThread(BluetoothDevice device, BluetoothAdapter adapter) {
        BluetoothSocket tmp = null;
        mmDevice = device;
        bluetoothAdapter = adapter;

        try {
            // Get a BluetoothSocket to connect with the given BluetoothDevice.
            tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
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
            return;
        }

        // The connection attempt succeeded. Perform work associated with
        // the connection in a separate thread.
        manageMyConnectedSocket(mmSocket);
    }

    private void manageMyConnectedSocket(BluetoothSocket mmSocket) {
        try {
            mmOutStream = mmSocket.getOutputStream();
            mObjectOutputStreamos = new ObjectOutputStream(mmOutStream);
            Log.d(TAG, "manageMyConnectedSocket: DUUUUUUUUUUU");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void write(Message ms) {
        String json = gson.toJson(ms);
        try {
            mObjectOutputStreamos.writeObject(json);
            mObjectOutputStreamos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    // Closes the client socket and causes the thread to finish.
    void cancel() {
        try {
            mObjectOutputStreamos.close();
            mmOutStream.close();
            mmSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close the client socket", e);
        }
    }
}
