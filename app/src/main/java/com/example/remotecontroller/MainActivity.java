package com.example.remotecontroller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

import java.io.IOException;
import java.sql.Array;
import java.util.Arrays;
import java.util.Set;

import static android.view.KeyEvent.ACTION_UP;
import static java.lang.Math.abs;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, KeyEvent.Callback, View.OnLongClickListener {

    private static final int REQUEST_ENABLE_BT = 1;
    private static final String TAG = "MainActivity";
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothThread bluetoothThread;

    private Button button;
    private Button mLpmBnt;
    private Button mPpmBnt;

    private int mPrevX = 0; // poprzednia wspolrzedna X
    private int mPrevY = 0; // poprzednia wspolrzedna Y
    private boolean mFirstTouch = true;
    private boolean mPress = false;
    private byte[] array = new byte[6];
    private int x,y;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(this);
        mLpmBnt = (Button) findViewById(R.id.lpmButton);
        mLpmBnt.setOnClickListener(this);
        mLpmBnt.setOnLongClickListener(this);
        mPpmBnt = (Button) findViewById(R.id.ppmButton);
        mPpmBnt.setOnClickListener(this);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(TAG, "onKeyDown: " + keyCode + " event " + event.getUnicodeChar());
        int key = event.getUnicodeChar();
        Log.d(TAG, " key " + key);
        clearArray();
        array[5] = (byte)key;
        if (key != 0) {
            bluetoothThread.write2(array);
        } else if (keyCode == 67) {
            array[5] =(byte)8;
            bluetoothThread.write2(array); // BACK_SPACE
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        x = (int) event.getX() - mPrevX;
        y = (int) event.getY() - mPrevY;
        mPrevX = (int) event.getX();
        mPrevY = (int) event.getY();
        if (!mFirstTouch) { // jak jest pierwsze dotkniecie to nie wysyla wiadomosci
            if (x != 0 && y != 0) { // jak x,y sa rozne od 0 to wysyla
//                Message ms = new Message();
//                ms.setmX(x);
//                ms.setmY(y);
//                bluetoothThread.write(ms);
                clearArray();
                array[0] = (byte)x;
                array[1] = (byte)y;

//                Log.d(TAG, "onTouchEvent: "+ array[0] + " " + array[1] + " size "+array.length);
                if(abs(x) >1 || abs(y) > 1) {
                    new Thread(() -> {
                        bluetoothThread.write2(array);
                    }).start();
                }
            }
        } else
            mFirstTouch = false;
//        Log.d(TAG, "onTouchEvent: X" + x + " Y " + y);
        if (event.getAction() == ACTION_UP) {
            mFirstTouch = true;
//            Log.d(TAG, "onTouchEvent: UPUPUP");
        }


        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: go");
        getPariedDevices();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: xDD");
        bluetoothThread.cancel();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button: {
                Log.d(TAG, "onClick: Show keyboard");
                InputMethodManager imm = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
            }
            break;
            case R.id.lpmButton: {
                Log.d(TAG, "onClick: LPM");
                if (mPress) {
                    mLpmBnt.setBackgroundColor(Color.GRAY); //TODO zamiana ikonki
                    sendButton(1, false, true); // zwalniamy lewy przycisk
                    mPress = false;
                }else {
                    sendButton(1, true, true);
                }

                break;
            }
            case R.id.ppmButton: {
                Log.d(TAG, "onClick: PPM");
                sendButton(3, true, true);
                break;
            }
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (v.getId() == R.id.lpmButton) {
            mLpmBnt.setBackgroundColor(Color.RED);//TODO zamiana ikonki
            sendButton(1, true, false);
            Log.d(TAG, "onLongClick: xxx");
            mPress = true;
        }
        return true;
    }

    private void getPariedDevices() {
        //TODO dodac wybor uzadzenia
        BluetoothDevice bluetoothDevice = null;
        // Query paired devices
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                bluetoothDevice = device;
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                Log.d(TAG, "onCreate: name " + deviceName + " adress " + deviceHardwareAddress);
            }
        }
        bluetoothThread = new BluetoothThread(bluetoothDevice, bluetoothAdapter);
        bluetoothThread.start();
    }

    private void sendButton(int bnt, boolean press, boolean release) {
        clearArray();
        array[2] = (byte)bnt;
        array[3] =(byte)(press ?1:0);
        array[4] =(byte)(release ?1:0);

        bluetoothThread.write2(array);
    }

    private void clearArray(){
        Arrays.fill(array, (byte) 0);
    }
}
