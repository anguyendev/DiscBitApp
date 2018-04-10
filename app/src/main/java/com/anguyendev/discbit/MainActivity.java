package com.anguyendev.discbit;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.widget.Button;

public class MainActivity extends AppCompatActivity{
    private Button mSearchButton;
    private RecyclerView mDeviceList;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSearchButton = findViewById(R.id.search_button);
        mDeviceList = findViewById(R.id.device_list_recycler_view);
    }


}
