package com.anguyendev.discbit;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements DeviceListAdapter.DeviceListClickListener{

    //TODO ask for permission the proper way

    // Constants
    private static final String APP_TAG = "DiscBit";
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int SCAN_PERIOD = 10000;

    // Views
    private Button mScanButton;
    private RecyclerView mRecyclerView;
    private TextView mInfoText;

    // BLE objects
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mLEScanner;
    private ScanSettings mScanSettings;
    private ArrayList<ScanFilter> mFilters;

    private DeviceListAdapter mDeviceListAdapter;

    private Handler mHandler = new Handler();

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            DeviceScanResult deviceScanResult = new DeviceScanResult(result);
            mDeviceListAdapter.add(deviceScanResult);
            Log.d(APP_TAG, "BLE Device found: " + result.getDevice().getAddress());
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult sr : results) {
                //TODO handle batch result
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            //TODO show some error message
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mInfoText = findViewById(R.id.no_devices_text);

        // Set the recycler view adapter
        mRecyclerView = findViewById(R.id.device_list_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayout.VERTICAL));
        mDeviceListAdapter = new DeviceListAdapter(this);
        mRecyclerView.setAdapter(mDeviceListAdapter);

        // Get the bluetooth adapter
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        // TODO handle null bluetooth adapter

        // Set up BLE scanner
        mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
        mScanSettings = new ScanSettings.Builder().
                setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
        mFilters = new ArrayList<>();

        // Set a click listener on the search button
        mScanButton = findViewById(R.id.scan_button);
        mScanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDeviceListAdapter.clear();
                startScan();
            }
        });
    }

    @Override
    protected void onResume(){
        super.onResume();
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
            stopScan();
        }
    }

    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode != RESULT_OK) {
                updateView(false, false,
                        getString(R.string.bluetooth_disabled));
            } else {
                updateView(false, true,
                        getString(R.string.no_devices_text));
            }
        }
    }

    @Override
    public void onDeviceListItemSelected(BluetoothDevice bluetoothDevice) {
        Log.d(APP_TAG, "Device selected: " + bluetoothDevice.getAddress());
        stopScan();
    }


    private void startScan() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(APP_TAG, "Done scanning for BLE devices");
                stopScan();
            }}, SCAN_PERIOD);

        Log.d(APP_TAG, "Scanning for BLE devices");
        updateView(true, false);
        mLEScanner.startScan(mFilters, mScanSettings, mScanCallback);
    }

    private void stopScan() {
        mLEScanner.stopScan(mScanCallback);
        updateView(!mDeviceListAdapter.isEmpty(), true, getString(R.string.no_devices_text));
    }

    private void updateView(boolean deviceListVisible, boolean scanButtonActive, String infoText) {
        if (deviceListVisible) {
            mRecyclerView.setVisibility(View.VISIBLE);
            mInfoText.setVisibility(View.GONE);
        } else {
            mRecyclerView.setVisibility(View.GONE);
            mInfoText.setText(infoText);
            mInfoText.setVisibility(View.VISIBLE);
        }

        mScanButton.setEnabled(scanButtonActive);
        if (scanButtonActive) {
            mScanButton.setText(R.string.scan);
        } else {
            mScanButton.setText(R.string.scanning);
        }
    }

    private void updateView(boolean deviceListVisible, boolean scanButtonActive) {
        updateView(deviceListVisible, scanButtonActive, null);
    }
}
