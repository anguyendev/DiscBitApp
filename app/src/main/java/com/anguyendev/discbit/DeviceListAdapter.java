package com.anguyendev.discbit;

import android.bluetooth.BluetoothDevice;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class DeviceListAdapter extends RecyclerView.Adapter<DeviceListAdapter.ViewHolder>{

    private ArrayList<DeviceScanResult> mScanResults;
    private DeviceListClickListener mDeviceListClickListener;

    public interface DeviceListClickListener
    {
        void onDeviceListItemSelected(BluetoothDevice bluetoothDevice);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView mDeviceName;
        private TextView mDeviceAddress;
        private TextView mDeviceRSSI;
        private ViewHolder(ConstraintLayout view, final DeviceListClickListener clickListener) {
            super(view);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickListener.onDeviceListItemSelected(
                            mScanResults.get(getAdapterPosition()).getBluetoothDevice());
                }
            });
            mDeviceName = view.findViewById(R.id.device_list_name_text);
            mDeviceAddress = view.findViewById(R.id.device_list_address_text);
            mDeviceRSSI = view.findViewById(R.id.device_list_rssi_text);
        }
    }

    public DeviceListAdapter(DeviceListClickListener deviceListClickListener) {
        mScanResults = new ArrayList<>();
        mDeviceListClickListener = deviceListClickListener;
    }

    @NonNull
    @Override
    public DeviceListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view
        ConstraintLayout v = (ConstraintLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.ble_device_list_item, parent, false);

        return new ViewHolder(v, mDeviceListClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceListAdapter.ViewHolder holder, int position) {
        if(mScanResults.get(position).getDeviceName() != null) {
            holder.mDeviceName.setText("(" + mScanResults.get(position).getDeviceName() + ")");
        } else {
            holder.mDeviceName.setText("");
        }
        holder.mDeviceAddress.setText(mScanResults.get(position).getDeviceAddress());
        holder.mDeviceRSSI.setText("RSSI: " + mScanResults.get(position).getRssi());
    }

    @Override
    public int getItemCount() {
        return mScanResults.size();
    }

    public void add(DeviceScanResult deviceScanResult) {
        if (!mScanResults.contains(deviceScanResult)) {
            mScanResults.add(deviceScanResult);
            notifyItemInserted(mScanResults.indexOf(deviceScanResult));
        } else {
            int deviceIndex = mScanResults.indexOf(deviceScanResult);
            mScanResults.set(deviceIndex, deviceScanResult);
            notifyItemChanged(deviceIndex);
        }
    }

    public boolean isEmpty() {
        return mScanResults.isEmpty();
    }

    public void clear() {
        mScanResults.clear();
        notifyDataSetChanged();
    }
}
