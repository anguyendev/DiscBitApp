package com.anguyendev.discbit;

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

    public ScanResult getScanResult() {
        return mScanResult;
    }

    public String getDeviceName() {
        return mScanResult.getDevice().getName();
    }

    public String getDeviceAddress() {
        return mScanResult.getDevice().getAddress();
    }

    public String getDeviceRssi() {
        return String.valueOf(mScanResult.getRssi());
    }
}
