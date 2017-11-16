package com.ntamtech.naqalati.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.ntamtech.naqalati.R;

public class OptionActivity extends AppCompatActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_option);
        findViewById(R.id.btn_request_now).setOnClickListener(this);
        findViewById(R.id.btn_special_request).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_request_now:
                startActivity(new Intent(OptionActivity.this, HomeActivity.class));
                break;
            case R.id.btn_special_request:
                startActivity(new Intent(OptionActivity.this, SpecialRequestActivity.class));
                break;
        }
    }
}
