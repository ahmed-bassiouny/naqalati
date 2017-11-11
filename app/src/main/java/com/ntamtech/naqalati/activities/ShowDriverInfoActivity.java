package com.ntamtech.naqalati.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.ntamtech.naqalati.R;

public class ShowDriverInfoActivity extends AppCompatActivity {

    String driverId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_driver_info);
        setFinishOnTouchOutside(false);
    }
}
