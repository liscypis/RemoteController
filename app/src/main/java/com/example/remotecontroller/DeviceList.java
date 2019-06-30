package com.example.remotecontroller;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

import static android.content.ContentValues.TAG;

public class DeviceList extends Activity implements AdapterView.OnItemClickListener, View.OnClickListener {

    private static final String DEVICE_ADDRESS = "device_address";
    private static final int PERMISSION_REQUEST_CODE = 999;
    private BluetoothAdapter bluetoothAdapter;
    private ArrayAdapter<String> foundDevicesArrayAdapter;
    private Button mScanBnt;
    private TextView mFoundTV;
    private ProgressBar mFindDevPB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);

        ArrayAdapter<String> pairedDevicesArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        foundDevicesArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);

        ListView pairedListView = (ListView) findViewById(R.id.parriedLV);
        pairedListView.setAdapter(pairedDevicesArrayAdapter);
        pairedListView.setOnItemClickListener(this);

        ListView newDevicesListView = (ListView) findViewById(R.id.foundLV);
        newDevicesListView.setAdapter(foundDevicesArrayAdapter);
        newDevicesListView.setOnItemClickListener(this);

        mScanBnt = (Button) findViewById(R.id.findBnt);
        mScanBnt.setOnClickListener(this);
        mFoundTV = (TextView) findViewById(R.id.foundTV);
        mFindDevPB =(ProgressBar) findViewById(R.id.findDevPB);
        mFindDevPB.setVisibility(View.GONE);

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(receiver, filter);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                pairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            String string = "BRAK URZADZEN";
            pairedDevicesArrayAdapter.add(string);
        }

        // dodanie pozwolenia
        getLocationPermission();

        mFoundTV.setVisibility(View.GONE);
    }
    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "getLocationPermission: TRUE");
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQUEST_CODE);
            Log.d(TAG, "getLocationPermission: FALSE");
        }
    }


    /**
     * sprawdza czy uzytkownik dal pozwolenie do lokalizacji, jeśli nie to okno z zapytaniem znów się wyświetli.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    Log.i(TAG, "onRequestPermissionsResult: cos nie działa");
                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                            PERMISSION_REQUEST_CODE);
                    Log.d(TAG, "onRequestPermissionsResult: nie wybrał");
                }
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        bluetoothAdapter.cancelDiscovery();

        String info = ((TextView) view).getText().toString();
        String address = info.substring(info.length() - 17);

        Intent intent = new Intent();
        intent.putExtra(DEVICE_ADDRESS, address);
        // Set result and finish this Activity
        setResult(Activity.RESULT_OK, intent);
        finish();
    }


    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                Log.d(TAG, "discovery started");
                mFindDevPB.setVisibility(View.VISIBLE);
            }
            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Toast.makeText(getApplicationContext(), "Zkonczono skanowanie", Toast.LENGTH_SHORT).show();
                mFindDevPB.setVisibility(View.GONE);
                mFoundTV.setVisibility(View.VISIBLE);
                mFoundTV.append(": " + foundDevicesArrayAdapter.getCount());
            }
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    mFoundTV.setVisibility(View.VISIBLE);
                    Log.i("Device Name: ", "device " + device.getName());
                    Log.i("deviceHardwareAddress ", "hard" + device.getAddress());
                    foundDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                }

            }

        }
    };


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.findBnt) {
            findDevices();
        }
    }

    void findDevices() {
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        bluetoothAdapter.startDiscovery();
        mScanBnt.setEnabled(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

}
