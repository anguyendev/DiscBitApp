package com.anguyendev.discbit;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.os.Bundle;
import android.util.Log;

public class ConnectActivity extends Activity implements BleManager.BleManagerListener, BleUtils.ResetBluetoothAdapterListener{

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
