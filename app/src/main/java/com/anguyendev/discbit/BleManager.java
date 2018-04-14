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

import java.lang.reflect.Method;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BleManager implements BleGattExecutor.BleExecutorListener {
    // Log
    private final static String TAG = BleManager.class.getSimpleName();

    // Enumerations
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

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

    private BluetoothGattCharacteristic disManuf;
    private BluetoothGattCharacteristic disModel;
    private BluetoothGattCharacteristic disHWRev;
    private BluetoothGattCharacteristic disSWRev;
    private boolean disAvailable;

    private Queue<BluetoothGattCharacteristic> readQueue;

    // Singleton
    private static BleManager mInstance = null;

    // Data
    private final BleGattExecutor mExecutor = BleGattExecutor.createExecutor(this);
    private BluetoothAdapter mAdapter;
    private BluetoothGatt mGatt;

    private BluetoothDevice mDevice;
    private String mDeviceAddress;
    private int mConnectionState = STATE_DISCONNECTED;

    private BleManagerListener mBleListener;

    private BluetoothGattCharacteristic tx;
    private BluetoothGattCharacteristic rx;

    public static BleManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new BleManager(context);
        }
        return mInstance;
    }

    public int getState() {
        return mConnectionState;
    }

    public BluetoothDevice getConnectedDevice() {
        return mDevice;
    }

    public String getConnectedDeviceAddress() {
        return mDeviceAddress;
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

        readQueue = new ConcurrentLinkedQueue<BluetoothGattCharacteristic>();
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
                    mConnectionState = STATE_CONNECTING;
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
        mConnectionState = STATE_CONNECTING;
        if (mBleListener != null) {
            mBleListener.onConnecting();
        }

        final boolean gattAutoconnect = sharedPreferences.getBoolean("pref_gattautoconnect", false);
        mGatt = mDevice.connectGatt(context, gattAutoconnect, mExecutor);

        return true;
    }

    public void clearExecutor() {
        if (mExecutor != null) {
            mExecutor.clear();
        }
    }

    /**
     * Call to private Android method 'refresh'
     * This method does actually clear the cache from a bluetooth device. But the problem is that we don't have access to it. But in java we have reflection, so we can access this method.
     * http://stackoverflow.com/questions/22596951/how-to-programmatically-force-bluetooth-low-energy-service-discovery-on-android
     */
    public boolean refreshDeviceCache() {
        try {
            BluetoothGatt localBluetoothGatt = mGatt;
            Method localMethod = localBluetoothGatt.getClass().getMethod("refresh");
            if (localMethod != null) {
                boolean result = (Boolean) localMethod.invoke(localBluetoothGatt);
                if (result) {
                    Log.d(TAG, "Bluetooth refresh cache");
                }
                return result;
            }
        } catch (Exception localException) {
            Log.e(TAG, "An exception occurred while refreshing device");
        }
        return false;
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


    public boolean readRssi() {
        if (mGatt != null) {
            return mGatt.readRemoteRssi();  // if true: Caller should wait for onReadRssi callback
        } else {
            return false;           // Rsii read is not available
        }
    }

    public void readCharacteristic(BluetoothGattService service, String characteristicUUID) {
        readService(service, characteristicUUID, null);
    }

    public void readDescriptor(BluetoothGattService service, String characteristicUUID, String descriptorUUID) {
        readService(service, characteristicUUID, descriptorUUID);
    }

    private void readService(BluetoothGattService service, String characteristicUUID, String descriptorUUID) {
        if (service != null) {
            if (mAdapter == null || mGatt == null) {
                Log.w(TAG, "readService: BluetoothAdapter not initialized");
                return;
            }

            mExecutor.read(service, characteristicUUID, descriptorUUID);
            mExecutor.execute(mGatt);
        }
    }

    public void writeService(BluetoothGattService service, String uuid, byte[] value) {
        if (service != null) {
            if (mAdapter == null || mGatt == null) {
                Log.w(TAG, "writeService: BluetoothAdapter not initialized");
                return;
            }

            mExecutor.write(service, uuid, value);
            mExecutor.execute(mGatt);
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

    public void enableIndication(BluetoothGattService service, String uuid, boolean enabled) {
        if (service != null) {

            if (mAdapter == null || mGatt == null) {
                Log.w(TAG, "enableNotification: BluetoothAdapter not initialized");
                return;
            }

            mExecutor.enableIndication(service, uuid, enabled);
            mExecutor.execute(mGatt);
        }
    }

    // Properties
    private int getCharacteristicProperties(BluetoothGattService service, String characteristicUUIDString) {
        final UUID characteristicUuid = UUID.fromString(characteristicUUIDString);
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(characteristicUuid);
        int properties = 0;
        if (characteristic != null) {
            properties = characteristic.getProperties();
        }

        return properties;
    }

    public boolean isCharacteristicReadable(BluetoothGattService service, String characteristicUUIDString) {
        final int properties = getCharacteristicProperties(service, characteristicUUIDString);
        final boolean isReadable = (properties & BluetoothGattCharacteristic.PROPERTY_READ) != 0;
        return isReadable;
    }

    public boolean isCharacteristicNotifiable(BluetoothGattService service, String characteristicUUIDString) {
        final int properties = getCharacteristicProperties(service, characteristicUUIDString);
        final boolean isNotifiable = (properties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0;
        return isNotifiable;
    }

    // Permissions
    private int getDescriptorPermissions(BluetoothGattService service, String characteristicUUIDString, String descriptorUUIDString) {
        final UUID characteristicUuid = UUID.fromString(characteristicUUIDString);
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(characteristicUuid);

        int permissions = 0;
        if (characteristic != null) {
            final UUID descriptorUuid = UUID.fromString(descriptorUUIDString);
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(descriptorUuid);
            if (descriptor != null) {
                permissions = descriptor.getPermissions();
            }
        }

        return permissions;
    }

    public boolean isDescriptorReadable(BluetoothGattService service, String characteristicUUIDString, String descriptorUUIDString) {
        final int permissions = getDescriptorPermissions(service, characteristicUUIDString, descriptorUUIDString);
        final boolean isReadable = (permissions & BluetoothGattCharacteristic.PERMISSION_READ) != 0;
        return isReadable;
    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mGatt != null) {
            return mGatt.getServices();
        } else {
            return null;
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

    public BluetoothGattService getGattService(String uuid, int instanceId) {
        if (mGatt != null) {
            List<BluetoothGattService> services = getSupportedGattServices();
            boolean found = false;
            int i = 0;
            while (i < services.size() && !found) {
                BluetoothGattService service = services.get(i);
                if (service.getUuid().toString().equalsIgnoreCase(uuid) && service.getInstanceId() == instanceId) {
                    found = true;
                } else {
                    i++;
                }
            }

            if (found) {
                return services.get(i);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

        // Log.d(TAG, "onConnectionStateChange status: "+status+ " newState: "+newState);

        if (newState == BluetoothProfile.STATE_CONNECTED) {
            mConnectionState = STATE_CONNECTED;

            if (mBleListener != null) {
                mBleListener.onConnected();
            }

            // Attempts to discover services after successful connection.
            gatt.discoverServices();

        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            mConnectionState = STATE_DISCONNECTED;

            if (mBleListener != null) {
                mBleListener.onDisconnected();
            }
        } else if (newState == BluetoothProfile.STATE_CONNECTING) {
            mConnectionState = STATE_CONNECTING;

            if (mBleListener != null) {
                mBleListener.onConnecting();
            }
        }
    }

    // region BleExecutorListener
    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        // if (status == BluetoothGatt.GATT_SUCCESS) {
        // Call listener
        if (mBleListener != null) {
            mBleListener.onServicesDiscovered();
        }

        if (status != BluetoothGatt.GATT_SUCCESS) {
            Log.d(TAG, "onServicesDiscovered status: " + status);
        } else {

            // Save reference to each UART characteristic.
            tx = gatt.getService(UART_UUID).getCharacteristic(TX_UUID);
            rx = gatt.getService(UART_UUID).getCharacteristic(RX_UUID);

            // Save reference to each DIS characteristic.
            disManuf = gatt.getService(DIS_UUID).getCharacteristic(DIS_MANUF_UUID);
            disModel = gatt.getService(DIS_UUID).getCharacteristic(DIS_MODEL_UUID);
            disHWRev = gatt.getService(DIS_UUID).getCharacteristic(DIS_HWREV_UUID);
            disSWRev = gatt.getService(DIS_UUID).getCharacteristic(DIS_SWREV_UUID);

            // Add device information characteristics to the read queue
            // These need to be queued because we have to wait for the response to the first
            // read request before a second one can be processed (which makes you wonder why they
            // implemented this with async logic to begin with???)
            readQueue.offer(disManuf);
            readQueue.offer(disModel);
            readQueue.offer(disHWRev);
            readQueue.offer(disSWRev);

            // Request a dummy read to get the device information queue going
            gatt.readCharacteristic(disManuf);

            // Setup notifications on RX characteristic changes (i.e. data received).
            // First call setCharacteristicNotification to enable notification.
            if (!gatt.setCharacteristicNotification(rx, true)) {
                // Stop if the characteristic notification setup failed.
                Log.d("DiscBit", "connectFailure - characteristic notification setup failed");
                return;
            }
            // Next update the RX characteristic's client descriptor to enable notifications.
            BluetoothGattDescriptor desc = rx.getDescriptor(CLIENT_UUID);
            if (desc == null) {
                // Stop if the RX characteristic has no client descriptor.
                Log.d("DiscBit", "connectFailure - RX characteristic has no client");
                return;
            }
            desc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            if (!gatt.writeDescriptor(desc)) {
                // Stop if the client descriptor could not be written.
                Log.d("DiscBit", "connectFailure - client descriptor could not be written");
                return;
            }
            // Notify of connection completion.
            Log.d("DiscBit", "notifyOnConnected");
        }
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        // if (status == BluetoothGatt.GATT_SUCCESS) {
        if (mBleListener != null) {
            mBleListener.onDataAvailable(characteristic);
        }
        // }

        if (status != BluetoothGatt.GATT_SUCCESS) {
            Log.d(TAG, "onCharacteristicRead status: " + status);
        }
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        if (mBleListener != null) {
            mBleListener.onDataAvailable(characteristic);
        }
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

        void onReadRemoteRssi(int rssi);
    }
}
