package com.anguyendev.discbit;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import com.anguyendev.discbit.models.GraphDataPoint;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.LineGraphSeries;
import org.json.JSONObject;

public class ConnectActivity extends Activity implements BleManager.BleManagerListener,
        BleUtils.ResetBluetoothAdapterListener {

    private static final String TAG = ConnectActivity.class.getSimpleName();
    private static final float DATA_RECEIVED_INTERVAL = 0.05f; // every 50 ms

    private enum SensorType {
        NONE,
        ACCELEROMETER,
        GYROSCOPE,
        MAGNETOMETER
    }

    private SensorType mSelectedSensor;
    private BleManager mBleManager;

    // Views
    private GraphView mGraphView;
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

    // Graph data
    private LineGraphSeries<GraphDataPoint> mAccelerometerDataSetX = new LineGraphSeries<>();
    private LineGraphSeries<GraphDataPoint> mAccelerometerDataSetY = new LineGraphSeries<>();
    private LineGraphSeries<GraphDataPoint> mAccelerometerDataSetZ = new LineGraphSeries<>();

    private LineGraphSeries<GraphDataPoint> mGyroscopeDataSetX = new LineGraphSeries<>();
    private LineGraphSeries<GraphDataPoint> mGyroscopeDataSetY = new LineGraphSeries<>();
    private LineGraphSeries<GraphDataPoint> mGyroscopeDataSetZ = new LineGraphSeries<>();

    private LineGraphSeries<GraphDataPoint> mMagnetometerDataSetX = new LineGraphSeries<>();
    private LineGraphSeries<GraphDataPoint> mMagnetometerDataSetY = new LineGraphSeries<>();
    private LineGraphSeries<GraphDataPoint> mMagnetometerDataSetZ = new LineGraphSeries<>();

    private float mTimeCount = 0;
    private String deviceAddress;

    private boolean inspectionModeEnabled = false;
    private Handler graphHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        mBleManager = BleManager.getInstance(this);
        mBleManager.setBleListener(this);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            BluetoothDevice bluetoothDevice = extras.getParcelable("btdevice");
            if(bluetoothDevice != null) {
                deviceAddress = bluetoothDevice.getAddress();
            }
        }

        setUpGraph();

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

        Button inspectModeButton = findViewById(R.id.inspectionModeButton);
        inspectModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inspectionModeEnabled = !inspectionModeEnabled;
                mGraphView.getViewport().setScalable(inspectionModeEnabled);
                mGraphView.getViewport().setScalableY(inspectionModeEnabled);
            }
        });

        RadioGroup radioGroup = findViewById(R.id.radioGroup);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                onRadioButtonOptionChecked(checkedId);
            }
        });
        int checkedButtonId = radioGroup.getCheckedRadioButtonId();
        onRadioButtonOptionChecked(checkedButtonId);
    }

    @Override
    public void onStart() {
        super.onStart();
        if(mBleManager != null) {
            mBleManager.connect(this, deviceAddress);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if(mBleManager != null) {
            mBleManager.disconnect();
        }
    }

    private void setUpGraph(){
        mGraphView = findViewById(R.id.graphView);
        // Set the viewport bounds manually to improve look of graph/data
        mGraphView.getViewport().setXAxisBoundsManual(true);
        mGraphView.getViewport().setMinX(0);
        mGraphView.getViewport().setMaxX(2);
        mGraphView.getViewport().setScalable(false);
        mGraphView.getViewport().setScalableY(false);
        // Set the grid label options
        mGraphView.getGridLabelRenderer().setHorizontalAxisTitle("Seconds");
        mGraphView.getGridLabelRenderer().setVerticalLabelsAlign(Paint.Align.LEFT);

        int padding = getResources().getDimensionPixelSize(R.dimen.small_padding);
        mGraphView.getLegendRenderer().setVisible(true);
        mGraphView.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.BOTTOM);
        mGraphView.getLegendRenderer().setPadding(padding);
        mGraphView.getLegendRenderer().setSpacing(padding / 2);

        mAccelerometerDataSetX.setTitle("x");
        mAccelerometerDataSetX.setColor(Color.RED);
        mAccelerometerDataSetY.setTitle("y");
        mAccelerometerDataSetY.setColor(Color.GREEN);
        mAccelerometerDataSetZ.setTitle("z");
        mAccelerometerDataSetZ.setColor(Color.BLUE);

        mGyroscopeDataSetX.setTitle("x");
        mGyroscopeDataSetX.setColor(Color.RED);
        mGyroscopeDataSetY.setTitle("y");
        mGyroscopeDataSetY.setColor(Color.GREEN);
        mGyroscopeDataSetZ.setTitle("z");
        mGyroscopeDataSetZ.setColor(Color.BLUE);

        mMagnetometerDataSetX.setTitle("x");
        mMagnetometerDataSetX.setColor(Color.RED);
        mMagnetometerDataSetY.setTitle("y");
        mMagnetometerDataSetY.setColor(Color.GREEN);
        mMagnetometerDataSetZ.setTitle("z");
        mMagnetometerDataSetZ.setColor(Color.BLUE);
    }

    private void toggleLoadingViews(final boolean show) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                View connectingLayout = findViewById(R.id.connecting_layout);
                connectingLayout.setVisibility(show ? View.VISIBLE : View.GONE);

                View connectedLayout = findViewById(R.id.connected_layout);
                connectedLayout.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });
    }

    private void onRadioButtonOptionChecked(int checkedId) {
        switch (checkedId) {
            case R.id.rb_accelerometer:
                if (mSelectedSensor != SensorType.ACCELEROMETER) {
                    mSelectedSensor = SensorType.ACCELEROMETER;
                    resetAllGraphData();
                    initializeAccelerometerData();
                }
                break;

            case R.id.rb_gyroscrope:
                if (mSelectedSensor != SensorType.GYROSCOPE) {
                    mSelectedSensor = SensorType.GYROSCOPE;
                    resetAllGraphData();
                    initializeGyroscopeData();
                }
                break;

            case R.id.rb_magnetometer:
                if (mSelectedSensor != SensorType.MAGNETOMETER) {
                    mSelectedSensor = SensorType.MAGNETOMETER;
                    resetAllGraphData();
                    initializeMagnetometerData();
                }
                break;
        }
    }

    //<editor-fold desc="Initialization of graph series data">
    private void initializeAccelerometerData() {
        mGraphView.addSeries(mAccelerometerDataSetX);
        mGraphView.addSeries(mAccelerometerDataSetY);
        mGraphView.addSeries(mAccelerometerDataSetZ);
    }

    private void initializeGyroscopeData() {
        mGraphView.addSeries(mGyroscopeDataSetX);
        mGraphView.addSeries(mGyroscopeDataSetY);
        mGraphView.addSeries(mGyroscopeDataSetZ);
    }

    private void initializeMagnetometerData() {
        mGraphView.addSeries(mMagnetometerDataSetX);
        mGraphView.addSeries(mMagnetometerDataSetY);
        mGraphView.addSeries(mMagnetometerDataSetZ);
    }
    //</editor-fold>

    //<editor-fold desc="Add data point to graph series data">
    private void addAccelerometerData(DiscData data) {
        addDataPointToSeries(mAccelerometerDataSetX, new GraphDataPoint(mTimeCount, data.getAx()));
        addDataPointToSeries(mAccelerometerDataSetY, new GraphDataPoint(mTimeCount, data.getAy()));
        addDataPointToSeries(mAccelerometerDataSetZ, new GraphDataPoint(mTimeCount, data.getAz()));
    }

    private void addGyroscopeData(DiscData data) {
        addDataPointToSeries(mGyroscopeDataSetX, new GraphDataPoint(mTimeCount, data.getGx()));
        addDataPointToSeries(mGyroscopeDataSetY, new GraphDataPoint(mTimeCount, data.getGy()));
        addDataPointToSeries(mGyroscopeDataSetZ, new GraphDataPoint(mTimeCount, data.getGz()));
    }

    private void addMagnetometerData(DiscData data) {
        addDataPointToSeries(mMagnetometerDataSetX, new GraphDataPoint(mTimeCount, data.getMx()));
        addDataPointToSeries(mMagnetometerDataSetY, new GraphDataPoint(mTimeCount, data.getMy()));
        addDataPointToSeries(mMagnetometerDataSetZ, new GraphDataPoint(mTimeCount, data.getMz()));
    }

    private void addDataPointToSeries(final LineGraphSeries<GraphDataPoint> series,
                                      final GraphDataPoint point) {
        graphHandler.post(new Runnable() {
            @Override
            public void run() {
                series.appendData(point, !inspectionModeEnabled, 750);
            }
        });
    }
    //</editor-fold>

    //<editor-fold desc="Reset data for graph data series">
    private void resetAllGraphData() {
        resetAccelerometerData();
        resetGyroscopeData();
        resetMagnetometerData();
        mGraphView.removeAllSeries();
    }

    private void resetAccelerometerData() {
        mAccelerometerDataSetX.resetData(new GraphDataPoint[]{});
        mAccelerometerDataSetY.resetData(new GraphDataPoint[]{});
        mAccelerometerDataSetZ.resetData(new GraphDataPoint[]{});
        mTimeCount = 0;
    }

    private void resetGyroscopeData() {
        mGyroscopeDataSetX.resetData(new GraphDataPoint[]{});
        mGyroscopeDataSetY.resetData(new GraphDataPoint[]{});
        mGyroscopeDataSetZ.resetData(new GraphDataPoint[]{});
        mTimeCount = 0;
    }

    private void resetMagnetometerData() {
        mMagnetometerDataSetX.resetData(new GraphDataPoint[]{});
        mMagnetometerDataSetX.resetData(new GraphDataPoint[]{});
        mMagnetometerDataSetX.resetData(new GraphDataPoint[]{});
        mTimeCount = 0;
    }
    //</editor-fold>

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mBleManager.disconnect();
    }

    @Override
    public void resetBluetoothCompleted() {
        Log.d(TAG, "resetBluetoothCompleted");
    }

    @Override
    public void onConnected() {
        Log.d(TAG, "onConnected");
        toggleLoadingViews(false);
    }

    @Override
    public void onConnecting() {
        Log.d(TAG, "onConnecting");
        toggleLoadingViews(true);
    }

    @Override
    public void onDisconnected() {
        Log.d(TAG, "onDisconnected");
    }

    @Override
    public void onServicesDiscovered() {
        Log.d(TAG, "onServicesDiscovered");
        mBleManager.enableNotification(
                mBleManager.getGattService(BleManager.UART_UUID),
                BleManager.RX_UUID,
                true
        );
    }

    @Override
    public void onDataAvailable(BluetoothGattCharacteristic characteristic) {
        Log.d(TAG, "onDataAvailableChar");
    }

    @Override
    public void onDataAvailable(BluetoothGattDescriptor descriptor) {
        Log.d(TAG, "onDataAvailableDesc");
    }

    @Override
    public void onDataAvailable(final JSONObject jsonObject) {
        Log.d(TAG, "onDataAvailableJson()");
        final DiscData discData = new DiscData(jsonObject);
        switch (mSelectedSensor) {
            case ACCELEROMETER:
                addAccelerometerData(discData);
                break;
            case GYROSCOPE:
                addGyroscopeData(discData);
                break;
            case MAGNETOMETER:
                addMagnetometerData(discData);
                break;
        }
        mTimeCount += DATA_RECEIVED_INTERVAL;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AxText.setText(String.valueOf(discData.getAx()));
                AyText.setText(String.valueOf(discData.getAy()));
                AzText.setText(String.valueOf(discData.getAz()));
                GxText.setText(String.valueOf(discData.getGx()));
                GyText.setText(String.valueOf(discData.getGy()));
                GzText.setText(String.valueOf(discData.getGz()));
                MxText.setText(String.valueOf(discData.getMx()));
                MyText.setText(String.valueOf(discData.getMy()));
                MzText.setText(String.valueOf(discData.getMz()));
                YawText.setText(String.valueOf(discData.getYaw()));
                PitchText.setText(String.valueOf(discData.getPitch()));
                RollText.setText(String.valueOf(discData.getRoll()));
            }
        });
    }

    @Override
    public void onReadRemoteRssi(int rssi) {
        Log.d(TAG, "onReadRemoteRssi");
    }
}