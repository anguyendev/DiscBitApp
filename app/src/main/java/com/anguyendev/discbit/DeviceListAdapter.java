package com.anguyendev.discbit;

import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class DeviceListAdapter extends RecyclerView.Adapter<DeviceListAdapter.ViewHolder>{

    private ArrayList<DeviceScanResult> mScanResults;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView mDeviceName;
        private TextView mDeviceAddress;
        private TextView mDeviceRSSI;
        private ViewHolder(ConstraintLayout view) {
            super(view);
            mDeviceName = view.findViewById(R.id.device_list_name_text);
            mDeviceAddress = view.findViewById(R.id.device_list_address_text);
            mDeviceRSSI = view.findViewById(R.id.device_list_rssi_text);
        }
    }

    public DeviceListAdapter() {
        mScanResults = new ArrayList<>();
    }

    @NonNull
    @Override
    public DeviceListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view
        ConstraintLayout v = (ConstraintLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.ble_device_list_item, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceListAdapter.ViewHolder holder, int position) {
        if(mScanResults.get(position).getDeviceName() != null)
        {
            holder.mDeviceName.setText("(" + mScanResults.get(position).getDeviceName() + ")");
        }
        holder.mDeviceAddress.setText(mScanResults.get(position).getDeviceAddress());
        holder.mDeviceRSSI.setText("RSSI: " + mScanResults.get(position).getRssi());
    }

    @Override
    public int getItemCount() {
        return mScanResults.size();
    }

    public void add(DeviceScanResult deviceScanResult)
    {
        if (!mScanResults.contains(deviceScanResult)) {
            mScanResults.add(deviceScanResult);
            notifyItemInserted(mScanResults.indexOf(deviceScanResult));
        } else {
            int deviceIndex = mScanResults.indexOf(deviceScanResult);
            mScanResults.set(deviceIndex, deviceScanResult);
            notifyItemChanged(deviceIndex);
        }
    }

    public boolean isEmpty()
    {
        return mScanResults.isEmpty();
    }

    public void clear()
    {
        mScanResults.clear();
        notifyDataSetChanged();
    }
}
