package com.anguyendev.discbit;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.os.Bundle;
import android.util.Log;

import java.util.Queue;
import java.util.UUID;

public class ConnectActivity extends Activity implements BleManager.BleManagerListener, BleUtils.ResetBluetoothAdapterListener{

    // UUIDs for UART service and associated characteristics.
    public static UUID UART_UUID = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E");
    public static UUID TX_UUID   = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E");
    public static UUID RX_UUID   = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E");

    // UUID for the UART BTLE client characteristic which is necessary for notifications.
    public static UUID CLIENT_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    // UUIDs for the Device Information service and associated characeristics.
    public static UUID DIS_UUID       = UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb");
    public static UUID DIS_MANUF_UUID = UUID.fromString("00002a29-0000-1000-8000-00805f9b34fb");
    public static UUID DIS_MODEL_UUID = UUID.fromString("00002a24-0000-1000-8000-00805f9b34fb");
    public static UUID DIS_HWREV_UUID = UUID.fromString("00002a26-0000-1000-8000-00805f9b34fb");
    public static UUID DIS_SWREV_UUID = UUID.fromString("00002a28-0000-1000-8000-00805f9b34fb");

    private Queue<BluetoothGattCharacteristic> readQueue;

    private BluetoothDevice mBluetoothDevice;
    private BleManager mBleManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        mBleManager = BleManager.getInstance(this);
        mBleManager.setBleListener(this);

        Bundle extras = getIntent().getExtras();
        if (extras != null)
        {
            mBluetoothDevice = extras.getParcelable("btdevice");
            mBleManager.connect(this, mBluetoothDevice.getAddress());
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
    }

    @Override
    protected void onPause(){
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mBleManager.disconnect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void resetBluetoothCompleted() {
        Log.d(getString(R.string.app_name), "resetBluetoothCompleted");
    }

    @Override
    public void onConnected() {
        Log.d(getString(R.string.app_name), "onConnected");
    }

    @Override
    public void onConnecting() {
        Log.d(getString(R.string.app_name), "onConnecting");
    }

    @Override
    public void onDisconnected() {
        Log.d(getString(R.string.app_name), "onDisconnected");
    }

    @Override
    public void onServicesDiscovered() {
        Log.d(getString(R.string.app_name), "onServicesDiscovered");

    }

    @Override
    public void onDataAvailable(BluetoothGattCharacteristic characteristic) {
        Log.d(getString(R.string.app_name), "onDataAvailable");
    }

    @Override
    public void onDataAvailable(BluetoothGattDescriptor descriptor) {
        Log.d(getString(R.string.app_name), "onDataAvailable");
    }

    @Override
    public void onReadRemoteRssi(int rssi) {
        Log.d(getString(R.string.app_name), "onReadRemoteRssi");
    }
}
