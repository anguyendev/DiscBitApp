package com.anguyendev.discbit;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ConnectActivity extends Activity implements BleManager.BleManagerListener,
        BleUtils.ResetBluetoothAdapterListener{

    private static final float DATA_RECEIVED_INTERVAL = 0.05f; // every 50 ms
    private static final float DATA_TO_DISPLAY = 15f; // 15 seconds
    private static final int Y_AXIS_MAX = 900;
    private static final int Y_AXIS_MIN = -900;

    private enum SensorType {
        NONE,
        ACCELEROMETER,
        GYROSCOPE,
        MAGNETOMETER
    }

    private SensorType mSelectedSensor;
    private BluetoothDevice mBluetoothDevice;
    private BleManager mBleManager;
    private View mConnectingLayout;
    private View mConnectedLayout;
    private LineChart mLineChart;
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
    private RadioGroup mRadioGroup;

    private List<Entry> mAccelerometerEntryListX = new ArrayList<>();
    private List<Entry> mAccelerometerEntryListY = new ArrayList<>();
    private List<Entry> mAccelerometerEntryListZ = new ArrayList<>();
    private LineDataSet mAccelerometerDataSetX;
    private LineDataSet mAccelerometerDataSetY;
    private LineDataSet mAccelerometerDataSetZ;
    private List<ILineDataSet> mAccelerometerSetList = new ArrayList<>();

    private List<Entry> mGyroscopeEntryListX = new ArrayList<>();
    private List<Entry> mGyroscopeEntryListY = new ArrayList<>();
    private List<Entry> mGyroscopeEntryListZ = new ArrayList<>();
    private LineDataSet mGyroscopeDataSetX;
    private LineDataSet mGyroscopeDataSetY;
    private LineDataSet mGyroscopeDataSetZ;
    private List<ILineDataSet> mGyroscopeSetList = new ArrayList<>();


    private List<Entry> mMagnetometerEntryListX = new ArrayList<>();
    private List<Entry> mMagnetometerEntryListY = new ArrayList<>();
    private List<Entry> mMagnetometerEntryListZ = new ArrayList<>();
    private LineDataSet mMagnetometerDataSetX;
    private LineDataSet mMagnetometerDataSetY;
    private LineDataSet mMagnetometerDataSetZ;
    private List<ILineDataSet> mMagnetometerSetList = new ArrayList<>();


    private XAxis xAxis;
    private YAxis leftAxis;

    private float mTimeCount = 0;

    private List<DiscData> mDiscDataList = new ArrayList<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        mConnectingLayout = findViewById(R.id.connecting_layout);
        mConnectedLayout = findViewById(R.id.connected_layout);

        mLineChart = findViewById(R.id.lineChart);
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

        mRadioGroup = findViewById(R.id.radioGroup);
        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId)
                {
                    case R.id.rb_accelerometer:
                        if (mSelectedSensor != SensorType.ACCELEROMETER) {
                            mSelectedSensor = SensorType.ACCELEROMETER;
                            resetAccelerometerData();
                        }
                        break;

                    case R.id.rb_gyroscrope:
                        if (mSelectedSensor != SensorType.GYROSCOPE) {
                            mSelectedSensor = SensorType.GYROSCOPE;
                            resetGyroscopeData();
                        }
                        break;

                    case R.id.rb_magnetometer:
                        if (mSelectedSensor != SensorType.MAGNETOMETER) {
                            mSelectedSensor = SensorType.MAGNETOMETER;
                            resetMagnetometerData();
                        }
                        break;
                }
            }
        });

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
                mConnectedLayout.setVisibility(View.VISIBLE);
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
    public void onDataAvailable(final JSONObject jsonObject) {
        Log.d(getString(R.string.app_name), "onDataAvailableJson(): " + jsonObject.toString());
        DiscData discData = new DiscData(jsonObject);
        mDiscDataList.add(discData);

        switch (mSelectedSensor) {
            case ACCELEROMETER:
                mAccelerometerDataSetX.addEntry(new Entry(mTimeCount, (float)discData.getAx()));
                mAccelerometerDataSetY.addEntry(new Entry(mTimeCount, (float)discData.getAy()));
                mAccelerometerDataSetZ.addEntry(new Entry(mTimeCount, (float)discData.getAz()));
                updateAxisMinMax();
                break;

            case GYROSCOPE:
                mGyroscopeDataSetX.addEntry(new Entry(mTimeCount, (float)discData.getGx()));
                mGyroscopeDataSetY.addEntry(new Entry(mTimeCount, (float)discData.getGy()));
                mGyroscopeDataSetZ.addEntry(new Entry(mTimeCount, (float)discData.getGz()));
                updateAxisMinMax();
                break;

            case MAGNETOMETER:
                mMagnetometerDataSetX.addEntry(new Entry(mTimeCount, (float)discData.getMx()));
                mMagnetometerDataSetY.addEntry(new Entry(mTimeCount, (float)discData.getMy()));
                mMagnetometerDataSetZ.addEntry(new Entry(mTimeCount, (float)discData.getMz()));
                updateAxisMinMax();
                break;
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
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
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onReadRemoteRssi(int rssi) {
        Log.d(getString(R.string.app_name), "onReadRemoteRssi");
    }

    private void setUpAxisX(){
        xAxis = mLineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM_INSIDE);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setDrawAxisLine(true);
        xAxis.setDrawGridLines(false);

        xAxis.setGranularityEnabled(true);
        xAxis.setGranularity(DATA_RECEIVED_INTERVAL);
        xAxis.setAxisMinimum(0);
        xAxis.setAxisMaximum(DATA_TO_DISPLAY);
    }

    private void setUpAxisY(){
        leftAxis = mLineChart.getAxisLeft();
        leftAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setDrawAxisLine(true);
        leftAxis.setDrawGridLines(false);

        leftAxis.setGranularityEnabled(true);
        leftAxis.setGranularity(1f);
        leftAxis.setAxisMaximum(Y_AXIS_MAX);
        leftAxis.setAxisMaximum(Y_AXIS_MIN);

        YAxis rightAxis = mLineChart.getAxisRight();
        rightAxis.setEnabled(false);
    }

    private void setUpGraph(){
        mLineChart.setTouchEnabled(true);

        // enable scaling and dragging
        mLineChart.setDragEnabled(true);
        mLineChart.setScaleEnabled(true);

        // if disabled, scaling can be done on x- and y-axis separately
        mLineChart.setPinchZoom(true);


        mLineChart.setDrawGridBackground(false);
        mLineChart.setHighlightPerDragEnabled(true);

        mLineChart.setAutoScaleMinMaxEnabled(true);

        Utils.init(this);


        setUpAxisX();
        setUpAxisY();

        setUpAccelerometerData();
        mLineChart.setData(new LineData(mAccelerometerSetList));

    }

    private void updateAxisMinMax() {
        if (mTimeCount > DATA_TO_DISPLAY) {
            xAxis.setAxisMinimum(xAxis.getAxisMinimum() + DATA_RECEIVED_INTERVAL);
            xAxis.setAxisMaximum(xAxis.getAxisMaximum() + DATA_RECEIVED_INTERVAL);
        }
        mTimeCount += DATA_RECEIVED_INTERVAL;
        mLineChart.invalidate();
        mLineChart.notifyDataSetChanged();
    }

    private void setUpAccelerometerData()
    {
        mAccelerometerEntryListX.clear();
        mAccelerometerEntryListX.add(new Entry(0, 0));
        mAccelerometerDataSetX = new LineDataSet(mAccelerometerEntryListX, "X");
        mAccelerometerDataSetX.setColor(Color.RED);
        mAccelerometerDataSetX.setCircleColor(Color.RED);
        mAccelerometerDataSetX.setDrawCircleHole(false);
        mAccelerometerDataSetX.setDrawValues(false);
        mAccelerometerSetList.add(mAccelerometerDataSetX);

        mAccelerometerEntryListY.clear();
        mAccelerometerEntryListY.add(new Entry(0, 0));
        mAccelerometerDataSetY = new LineDataSet(mAccelerometerEntryListY, "Y");
        mAccelerometerDataSetY.setColor(Color.GREEN);
        mAccelerometerDataSetY.setCircleColor(Color.GREEN);
        mAccelerometerDataSetY.setDrawCircleHole(false);
        mAccelerometerDataSetY.setDrawValues(false);
        mAccelerometerSetList.add(mAccelerometerDataSetY);

        mAccelerometerEntryListZ.clear();
        mAccelerometerEntryListZ.add(new Entry(0, 0));
        mAccelerometerDataSetZ = new LineDataSet(mAccelerometerEntryListZ, "Z");
        mAccelerometerDataSetZ.setColor(Color.BLUE);
        mAccelerometerDataSetZ.setCircleColor(Color.BLUE);
        mAccelerometerDataSetZ.setDrawCircleHole(false);
        mAccelerometerDataSetZ.setDrawValues(false);
        mAccelerometerSetList.add(mAccelerometerDataSetZ);
    }

    private void setUpGyroscopeData()
    {
        mGyroscopeEntryListX.clear();
        mGyroscopeEntryListX.add(new Entry(0, 0));
        mGyroscopeDataSetX = new LineDataSet(mGyroscopeEntryListX, "X");
        mGyroscopeDataSetX.setColor(Color.RED);
        mGyroscopeDataSetX.setCircleColor(Color.RED);
        mGyroscopeDataSetX.setDrawCircleHole(false);
        mGyroscopeDataSetX.setDrawValues(false);
        mGyroscopeSetList.add(mGyroscopeDataSetX);

        mGyroscopeEntryListY.clear();
        mGyroscopeEntryListY.add(new Entry(0, 0));
        mGyroscopeDataSetY = new LineDataSet(mGyroscopeEntryListY, "Y");
        mGyroscopeDataSetY.setColor(Color.GREEN);
        mGyroscopeDataSetY.setCircleColor(Color.GREEN);
        mGyroscopeDataSetY.setDrawCircleHole(false);
        mGyroscopeDataSetY.setDrawValues(false);
        mGyroscopeSetList.add(mGyroscopeDataSetY);

        mGyroscopeEntryListZ.clear();
        mGyroscopeEntryListZ.add(new Entry(0, 0));
        mGyroscopeDataSetZ = new LineDataSet(mGyroscopeEntryListZ, "Z");
        mGyroscopeDataSetZ.setColor(Color.BLUE);
        mGyroscopeDataSetZ.setCircleColor(Color.BLUE);
        mGyroscopeDataSetZ.setDrawCircleHole(false);
        mGyroscopeDataSetZ.setDrawValues(false);
        mGyroscopeSetList.add(mGyroscopeDataSetZ);
    }

    private void setUpMagnetometerData()
    {
        mMagnetometerEntryListX.add(new Entry(0, 0));
        mMagnetometerDataSetX = new LineDataSet(mMagnetometerEntryListX, "X");
        mMagnetometerDataSetX.setColor(Color.RED);
        mMagnetometerDataSetX.setCircleColor(Color.RED);
        mMagnetometerDataSetX.setDrawCircleHole(false);
        mMagnetometerDataSetX.setDrawValues(false);
        mMagnetometerSetList.add(mMagnetometerDataSetX);

        mMagnetometerEntryListY.add(new Entry(0, 0));
        mMagnetometerDataSetY = new LineDataSet(mMagnetometerEntryListY, "Y");
        mMagnetometerDataSetY.setColor(Color.GREEN);
        mMagnetometerDataSetY.setCircleColor(Color.GREEN);
        mMagnetometerDataSetY.setDrawCircleHole(false);
        mMagnetometerDataSetY.setDrawValues(false);
        mMagnetometerSetList.add(mMagnetometerDataSetY);

        mMagnetometerEntryListZ.add(new Entry(0, 0));
        mMagnetometerDataSetZ = new LineDataSet(mMagnetometerEntryListZ, "Z");
        mMagnetometerDataSetZ.setColor(Color.BLUE);
        mMagnetometerDataSetZ.setCircleColor(Color.BLUE);
        mMagnetometerDataSetZ.setDrawCircleHole(false);
        mMagnetometerDataSetZ.setDrawValues(false);
        mMagnetometerSetList.add(mMagnetometerDataSetZ);
    }

    private void resetAccelerometerData() {
        mAccelerometerSetList.clear();
        mLineChart.clear();
        mLineChart.invalidate();
        mTimeCount = 0;
        setUpAccelerometerData();
        mLineChart.setData(new LineData(mAccelerometerSetList));
    }

    private void resetGyroscopeData() {
        mGyroscopeSetList.clear();
        mLineChart.clear();
        mLineChart.invalidate();
        mTimeCount = 0;
        setUpGyroscopeData();
        mLineChart.setData(new LineData(mGyroscopeSetList));
    }

    private void resetMagnetometerData() {
        mMagnetometerSetList.clear();
        mLineChart.clear();
        mLineChart.invalidate();
        mTimeCount = 0;
        setUpMagnetometerData();
        mLineChart.setData(new LineData(mMagnetometerSetList));
    }
}
