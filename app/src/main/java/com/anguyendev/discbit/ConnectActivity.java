package com.anguyendev.discbit;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ConnectActivity extends Activity implements BleManager.BleManagerListener, BleUtils.ResetBluetoothAdapterListener{

    private BluetoothDevice mBluetoothDevice;
    private BleManager mBleManager;
    private View mConnectingLayout;

    private TextView AxText;
    private TextView AyText;
    private TextView AzText;
    private TextView GxText;
    private TextView GyText;
    private TextView GzText;
    private TextView MxText;
    private TextView MyText;
    private TextView MzText;
    private TextView YawText;
    private TextView PitchText;
    private TextView RollText;
    private TextView LogTextView;

    private List<DiscData> mDiscDataList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        mConnectingLayout = findViewById(R.id.connecting_layout);

        AxText = findViewById(R.id.ax_text);
        AyText = findViewById(R.id.ay_text);
        AzText = findViewById(R.id.az_text);
        GxText = findViewById(R.id.gx_text);
        GyText = findViewById(R.id.gy_text);
        GzText = findViewById(R.id.gz_text);
        MxText = findViewById(R.id.mx_text);
        MyText = findViewById(R.id.my_text);
        MzText = findViewById(R.id.mz_text);
        YawText = findViewById(R.id.yaw_text);
        PitchText = findViewById(R.id.pitch_text);
        RollText = findViewById(R.id.roll_text);
        LogTextView = findViewById(R.id.logTextView);

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
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnectingLayout.setVisibility(View.GONE);
                //mDiscDataTable.setVisibility(View.VISIBLE);
            }
        });

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
        mBleManager.enableNotification(mBleManager.getGattService(mBleManager.UART_UUID),
                mBleManager.RX_UUID, true);
    }

    @Override
    public void onDataAvailable(BluetoothGattCharacteristic characteristic) {
        Log.d(getString(R.string.app_name), "onDataAvailableChar");
    }

    @Override
    public void onDataAvailable(BluetoothGattDescriptor descriptor) {
        Log.d(getString(R.string.app_name), "onDataAvailableDesc");
    }

    @Override
    public void onDataAvailable(JSONObject jsonObject) {
        Log.d(getString(R.string.app_name), "onDataAvailableJson(): " + jsonObject.toString());
        //DiscData discData = new DiscData(jsonObject);
        //mDiscDataList.add(discData);
//        AxText.setText(String.valueOf(discData.getAx()));
//        AyText.setText(String.valueOf(discData.getAy()));
//        AzText.setText(String.valueOf(discData.getAz()));
//        GxText.setText(String.valueOf(discData.getGx()));
//        GyText.setText(String.valueOf(discData.getGy()));
//        GzText.setText(String.valueOf(discData.getGz()));
//        MxText.setText(String.valueOf(discData.getMx()));
//        MyText.setText(String.valueOf(discData.getMy()));
//        MzText.setText(String.valueOf(discData.getMz()));
//        YawText.setText(String.valueOf(discData.getYaw()));
//        PitchText.setText(String.valueOf(discData.getPitch()));
//        RollText.setText(String.valueOf(discData.getRoll()));
        try {
            AxText.setText(String.valueOf(jsonObject.getDouble("ax")));
            AyText.setText(String.valueOf(jsonObject.getDouble("ay")));
            AzText.setText(String.valueOf(jsonObject.getDouble("az")));
            GxText.setText(String.valueOf(jsonObject.getDouble("gx")));
            GyText.setText(String.valueOf(jsonObject.getDouble("gy")));
            GzText.setText(String.valueOf(jsonObject.getDouble("gz")));
            MxText.setText(String.valueOf(jsonObject.getDouble("mx")));
            MyText.setText(String.valueOf(jsonObject.getDouble("my")));
            MzText.setText(String.valueOf(jsonObject.getDouble("mz")));
            YawText.setText(String.valueOf(jsonObject.getDouble("yaw")));
            PitchText.setText(String.valueOf(jsonObject.getDouble("pitch")));
            RollText.setText(String.valueOf(jsonObject.getDouble("roll")));

            // Scrollview for debugging
            String text = LogTextView.getText() + "\n" + jsonObject.toString();
            if(text.length() > 1200) {
                text = text.substring(text.length() - 1200, text.length());
            }
            LogTextView.setText(text);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onReadRemoteRssi(int rssi) {
        Log.d(getString(R.string.app_name), "onReadRemoteRssi");
    }
}
