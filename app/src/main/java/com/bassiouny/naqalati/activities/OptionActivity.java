package com.bassiouny.naqalati.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.bassiouny.naqalati.R;
import com.bassiouny.naqalati.helper.Constant;

public class OptionActivity extends AppCompatActivity implements View.OnClickListener{

    Spinner spinner;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_option);
        spinner=findViewById(R.id.sp_car_type);
        ArrayAdapter mAdapter = ArrayAdapter.createFromResource(this, R.array.car_type_value,
                android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(mAdapter);
        findViewById(R.id.btn_request_now).setOnClickListener(this);
        findViewById(R.id.btn_special_request).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_request_now:
                Intent intent =new Intent(OptionActivity.this, HomeActivity.class);
                intent.putExtra(Constant.CAR_TYPE_FILTER,spinner.getSelectedItem().toString());
                startActivity(intent);
                break;
            case R.id.btn_special_request:
                startActivity(new Intent(OptionActivity.this, SpecialRequestActivity.class));
                break;
        }
    }
}
