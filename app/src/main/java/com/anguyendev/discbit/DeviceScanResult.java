package com.anguyendev.discbit;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanResult;

public class DeviceScanResult {

    private ScanResult mScanResult;

    public DeviceScanResult(ScanResult scanResult) {
        mScanResult = scanResult;
    }

    @Override
    public int hashCode() {
        return mScanResult.getDevice().getAddress().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof DeviceScanResult) {
            return getDeviceAddress().equals(((DeviceScanResult) o).getDeviceAddress());
        } else {
            return false;
        }
    }

    public String getDeviceName() {
        return mScanResult.getDevice().getName();
    }

    public String getDeviceAddress() {
        return mScanResult.getDevice().getAddress();
    }

    public String getRssi() {
        return String.valueOf(mScanResult.getRssi());
    }

    public BluetoothDevice getBluetoothDevice() {
        return mScanResult.getDevice();
    }
}
