package com.example.remotecontroller;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import java.util.Arrays;

import static android.view.KeyEvent.ACTION_UP;
import static java.lang.Math.abs;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, KeyEvent.Callback, View.OnLongClickListener {

    private static final String TAG = "MainActivity";
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothThread bluetoothThread;

    private ImageView mShowKeyboardIV;
    private Button mLpmBnt;
    private Button mPpmBnt;

    private Boolean mIsConnected;

    private int mPrevX = 0; // poprzednia wspolrzedna X
    private int mPrevY = 0; // poprzednia wspolrzedna Y
    private boolean mFirstTouch = true;
    private boolean mPress = false;
    private byte[] array = new byte[7];
    private int x, y;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        mShowKeyboardIV = (ImageView) findViewById(R.id.showKeyboardBnt);
        mShowKeyboardIV.setOnClickListener(this);
        mLpmBnt = (Button) findViewById(R.id.lpmButton);
        mLpmBnt.setOnClickListener(this);
        mLpmBnt.setOnLongClickListener(this);
        mPpmBnt = (Button) findViewById(R.id.ppmButton);
        mPpmBnt.setOnClickListener(this);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Log.d(TAG, "onCreate: Device not support BT");
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, Constants.REQUEST_ENABLE_BT);
        }

        init();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.connectBNT: {
                Log.d(TAG, "onOptionsItemSelected: click");
                Intent intent = new Intent(this, DeviceList.class);
                startActivityForResult(intent, Constants.REQUEST_CONNECT);
                return true;
            }

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void init() {
        mLpmBnt.setEnabled(false);
        mShowKeyboardIV.setEnabled(false);
        mPpmBnt.setEnabled(false);
        mIsConnected = false;
    }

    private void activeApp() {
        mLpmBnt.setEnabled(true);
        mShowKeyboardIV.setEnabled(true);
        mPpmBnt.setEnabled(true);
        mIsConnected = true;
    }

    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.CONNECT_STATUS: {
                    String s = msg.getData().getString(Constants.MESSAGE_STATUS);
                    if (s.equals(Constants.CONNECTED))
                        activeApp();

                    Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
                }
            }
        }
    };


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mIsConnected) {
//            Log.d(TAG, "onKeyDown: " + keyCode + " event " + event.getUnicodeChar());
            int key = event.getUnicodeChar();
//            Log.d(TAG, " key " + key);
            clearArray();
            array[5] = (byte) key;
            if (key != 0) {
                bluetoothThread.write(array);
            } else if (keyCode == 67) {
                array[5] = (byte) 8;
                bluetoothThread.write(array); // BACK_SPACE 8
            }
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mIsConnected) {
            if (event.getPointerCount() > 1) { // do scrolla
                y = (int) event.getY() - mPrevY;
                mPrevY = (int) event.getY();
                if(y > 2){
                    clearArray();
                    array[6] = (byte) 1;
                    bluetoothThread.write(array);
                }else if(y < -2) {
                    clearArray();
                    array[6] = (byte) -1;
                    bluetoothThread.write(array);
                }
//                Log.d(TAG, "onTouchEvent: MULTI y: " + y);
            } else { // do myszki
                x = (int) event.getX() - mPrevX;
                y = (int) event.getY() - mPrevY;
                mPrevX = (int) event.getX();
                mPrevY = (int) event.getY();
                if (!mFirstTouch) { // jak jest pierwsze dotkniecie to nie wysyla wiadomosci
                        if (abs(x) > 2 || abs(y) > 2) { // jak x,y sa wieksze od 2 to wysyla
                            clearArray();
                            array[0] = (byte) x;
                            array[1] = (byte) y;
                            bluetoothThread.write(array);
                        }
//                        Log.d(TAG, "onTouchEvent: "+ array[0] + " " + array[1] + " size "+array.length);
                } else
                    mFirstTouch = false;
//                  Log.d(TAG, "onTouchEvent: X" + x + " Y " + y);

            }
            if (event.getAction() == ACTION_UP) {
                mFirstTouch = true;
//            Log.d(TAG, "onTouchEvent: UPUPUP");
            }
        }
        return true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop:");
        if (mIsConnected) {
            byte[] ms = {1};
            bluetoothThread.write(ms);
        }

        if (bluetoothThread != null) {
            bluetoothThread.cancel();
            init();
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.showKeyboardBnt: {
                Log.d(TAG, "onClick: Show keyboard");
                InputMethodManager imm = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
            }
            break;
            case R.id.lpmButton: {
                Log.d(TAG, "onClick: LPM");
                if (mPress) {
                    mLpmBnt.getBackground().setColorFilter(ContextCompat.getColor(this, R.color.colorButton), PorterDuff.Mode.MULTIPLY);
                    sendButton(1, false, true); // zwalniamy lewy przycisk
                    mPress = false;
                } else {
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
            mLpmBnt.getBackground().setColorFilter(ContextCompat.getColor(this, R.color.colorAccent), PorterDuff.Mode.MULTIPLY);
            sendButton(1, true, false);
            Log.d(TAG, "onLongClick: xxx");
            mPress = true;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == Constants.REQUEST_CONNECT) {
            if (resultCode == Activity.RESULT_OK) {
                String mac = data.getExtras().getString(Constants.DEVICE_ADDRESS);
                Log.d(TAG, "onActivityResult: funguje?");
                bluetoothThread = new BluetoothThread(mac, bluetoothAdapter, mHandler);
                bluetoothThread.start();
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                Log.d(TAG, "onActivityResult: CANELED");
            }
        }
    }


    private void sendButton(int bnt, boolean press, boolean release) {
        clearArray();
        array[2] = (byte) bnt;
        array[3] = (byte) (press ? 1 : 0);
        array[4] = (byte) (release ? 1 : 0);

        bluetoothThread.write(array);
    }

    private void clearArray() {
        Arrays.fill(array, (byte) 0);
    }
}
