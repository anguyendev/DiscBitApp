package com.anguyendev.discbit;

// Original source code: https://github.com/StevenRudenko/BleSensorTag. MIT License (Steven Rudenko)


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

public class BleManager implements BleGattExecutor.BleExecutorListener {
    // Log
    private final static String TAG = BleManager.class.getSimpleName();

    // UUIDs for UART service and associated characteristics.
    public static String UART_UUID = "6E400001-B5A3-F393-E0A9-E50E24DCCA9E";
    public static String TX_UUID   = "6E400002-B5A3-F393-E0A9-E50E24DCCA9E";
    public static String RX_UUID   = "6E400003-B5A3-F393-E0A9-E50E24DCCA9E";

    // Singleton
    private static BleManager mInstance = null;

    // Data
    private final BleGattExecutor mExecutor = BleGattExecutor.createExecutor(this);
    private BluetoothAdapter mAdapter;
    private BluetoothGatt mGatt;

    private BluetoothDevice mDevice;
    private String mDeviceAddress;

    private BleManagerListener mBleListener;

    public static BleManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new BleManager(context);
        }
        return mInstance;
    }

    public void setBleListener(BleManagerListener listener) {
        mBleListener = listener;
    }

    public BleManager(Context context) {
        // Init Adapter
        if (mAdapter == null) {
            mAdapter = BleUtils.getBluetoothAdapter(context);
        }

        if (mAdapter == null || !mAdapter.isEnabled()) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
        }
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     * @return Return true if the connection is initiated successfully. The connection result is reported asynchronously through the {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)} callback.
     */
    public boolean connect(Context context, String address) {
        if (mAdapter == null || address == null) {
            Log.w(TAG, "connect: BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Get preferences
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        final boolean reuseExistingConnection = sharedPreferences.getBoolean("pref_recycleconnection", false);

        if (reuseExistingConnection) {
            // Previously connected device.  Try to reconnect.
            if (mDeviceAddress != null && address.equalsIgnoreCase(mDeviceAddress) && mGatt != null) {
                Log.d(TAG, "Trying to use an existing BluetoothGatt for connection.");
                if (mGatt.connect()) {
                    if (mBleListener != null)
                        mBleListener.onConnecting();
                    return true;
                } else {
                    return false;
                }
            }
        } else {
            final boolean forceCloseBeforeNewConnection = sharedPreferences.getBoolean("pref_forcecloseconnection", true);

            if (forceCloseBeforeNewConnection) {
                close();
            }
        }

        mDevice = mAdapter.getRemoteDevice(address);
        if (mDevice == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }

        Log.d(TAG, "Trying to create a new connection.");
        mDeviceAddress = address;
        if (mBleListener != null) {
            mBleListener.onConnecting();
        }

        final boolean gattAutoconnect = sharedPreferences.getBoolean("pref_gattautoconnect", false);
        mGatt = mDevice.connectGatt(context, gattAutoconnect, mExecutor);

        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)} callback.
     */
    public void disconnect() {
        mDevice = null;

        if (mAdapter == null || mGatt == null) {
            Log.w(TAG, "disconnect: BluetoothAdapter not initialized");
            return;
        }

        // Disconnect
        mGatt.disconnect();
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are  released properly.
     */
    private void close() {
        if (mGatt != null) {
            mGatt.close();
            mGatt = null;
            mDeviceAddress = null;
            mDevice = null;
        }
    }

    public void enableNotification(BluetoothGattService service, String uuid, boolean enabled) {
        if (service != null) {

            if (mAdapter == null || mGatt == null) {
                Log.w(TAG, "enableNotification: BluetoothAdapter not initialized");
                return;
            }

            mExecutor.enableNotification(service, uuid, enabled);
            mExecutor.execute(mGatt);
        }
    }

    public BluetoothGattService getGattService(String uuid) {
        if (mGatt != null) {
            final UUID serviceUuid = UUID.fromString(uuid);
            return mGatt.getService(serviceUuid);
        } else {
            return null;
        }
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

        // Log.d(TAG, "onConnectionStateChange status: "+status+ " newState: "+newState);

        if (newState == BluetoothProfile.STATE_CONNECTED) {

            if (mBleListener != null) {
                mBleListener.onConnected();
            }

            // Attempts to discover services after successful connection.
            gatt.discoverServices();

        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {

            if (mBleListener != null) {
                mBleListener.onDisconnected();
            }
        } else if (newState == BluetoothProfile.STATE_CONNECTING) {

            if (mBleListener != null) {
                mBleListener.onConnecting();
            }
        }
    }

    // region BleExecutorListener
    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        // Call listener
        if (mBleListener != null)
            mBleListener.onServicesDiscovered();

        if (status != BluetoothGatt.GATT_SUCCESS) {
            Log.d(TAG, "onServicesDiscovered status: " + status);
        }
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        if (mBleListener != null) {
            mBleListener.onDataAvailable(characteristic);
        }

        if (status != BluetoothGatt.GATT_SUCCESS) {
            Log.d(TAG, "onCharacteristicRead status: " + status);
        }
    }

    private boolean readingJson;
    private byte[] mByteArray = new byte[1024];
    private int byteArrayIndex = 0;

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        if (mBleListener != null) {
            if (characteristic.getService().getUuid().toString().equalsIgnoreCase(UART_UUID)) {
                if (characteristic.getUuid().toString().equalsIgnoreCase(RX_UUID)) {
                    final byte[] bytes = characteristic.getValue();
                    for (byte b: bytes) {
                        if (b=='{') {
                            readingJson = true;
                        }
                        if (readingJson) {
                            mByteArray[byteArrayIndex++] = b;
                            if (b=='}') {
                                jsonObjectComplete();
                                readingJson = false;
                            }
                        }
                    }
                }
            }
        }
    }

    private void jsonObjectComplete() {
        try {
            String val = BleUtils.bytesToText(mByteArray, false);
            JSONObject jsonObject = new JSONObject(val);
            mBleListener.onDataAvailable(jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        byteArrayIndex = 0;
    }

    @Override
    public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        //   if (status == BluetoothGatt.GATT_SUCCESS) {
        if (mBleListener != null) {
            mBleListener.onDataAvailable(descriptor);
        }
        //   }

        if (status != BluetoothGatt.GATT_SUCCESS) {
            Log.d(TAG, "onDescriptorRead status: " + status);
        }
    }

    @Override
    public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
        if (mBleListener != null) {
            mBleListener.onReadRemoteRssi(rssi);
        }

        if (status != BluetoothGatt.GATT_SUCCESS) {
            Log.d(TAG, "onReadRemoteRssi status: " + status);
        }

    }
    //endregion

    public interface BleManagerListener {

        void onConnected();

        void onConnecting();

        void onDisconnected();

        void onServicesDiscovered();

        void onDataAvailable(BluetoothGattCharacteristic characteristic);

        void onDataAvailable(BluetoothGattDescriptor descriptor);

        void onDataAvailable(JSONObject jsonObject);

        void onReadRemoteRssi(int rssi);
    }
}
