package com.anguyendev.discbit;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class DeviceListAdapter extends RecyclerView.Adapter<DeviceListAdapter.ViewHolder>{

    private ArrayList<DeviceScanResult> mScanResults;
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mDeviceName;
        public TextView mDeviceAddress;
        public TextView mDeviceRSSI;
        public ViewHolder(LinearLayout view) {
            super(view);
            mDeviceName = view.findViewById(R.id.device_list_name_text);
            mDeviceAddress = view.findViewById(R.id.device_list_address_text);
            mDeviceRSSI = view.findViewById(R.id.device_list_rssi_text);
        }
    }

    public DeviceListAdapter(ArrayList<DeviceScanResult> results) {
        mScanResults = results;
    }

    @NonNull
    @Override
    public DeviceListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view
        LinearLayout v = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.ble_device_list_item, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceListAdapter.ViewHolder holder, int position) {
        holder.mDeviceName.setText(mScanResults.get(position).getDeviceName());
        holder.mDeviceAddress.setText(mScanResults.get(position).getDeviceAddress());
        holder.mDeviceRSSI.setText(mScanResults.get(position).getDeviceRssi());
    }

    @Override
    public int getItemCount() {
        return mScanResults.size();
    }
}
