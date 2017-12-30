package com.bassiouny.naqalati.activities;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.bassiouny.naqalati.R;
import com.bassiouny.naqalati.helper.Constant;
import com.bassiouny.naqalati.helper.SharedPref;
import com.bassiouny.naqalati.model.FirebaseRoot;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class OptionActivity extends AppCompatActivity implements View.OnClickListener {

    private Spinner spinner;
    private EditText etShacehNumberCar;
    private TextView tvVersion;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_option);
        spinner = findViewById(R.id.sp_car_type);
        etShacehNumberCar = findViewById(R.id.et_shaceh_number_car);
        ArrayAdapter mAdapter = ArrayAdapter.createFromResource(this, R.array.car_type_value,
                android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(mAdapter);
        findViewById(R.id.btn_request_now).setOnClickListener(this);
        findViewById(R.id.btn_special_request).setOnClickListener(this);
        checkIfTokenUpdated();
        findViewById(R.id.iv_menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(OptionActivity.this,MenuActivity.class));
            }
        });
        tvVersion = findViewById(R.id.tv_version);
        try {
            PackageInfo pInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
            tvVersion.setText("نسخه رقم "+pInfo.versionName );
        } catch (PackageManager.NameNotFoundException e) {
            Crashlytics.logException(e);
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_request_now:
                Intent intent = new Intent(OptionActivity.this, HomeActivity.class);
                intent.putExtra(Constant.CAR_TYPE_FILTER, spinner.getSelectedItem().toString());
                intent.putExtra(Constant.CAR_SHACEH_NUMBER_FILTER, etShacehNumberCar.getText().toString());
                startActivity(intent);
                break;
            case R.id.btn_special_request:
                startActivity(new Intent(OptionActivity.this, SpecialRequestActivity.class));
                break;
        }
    }

    private void checkIfTokenUpdated() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null)
            return;
        if (!SharedPref.updatedToken(this)) {
            // this case mean i don't update token user in firebase so i will make request to update it
            FirebaseDatabase.getInstance().getReference(FirebaseRoot.DB_USER)
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child(FirebaseRoot.DB_TOKEN).setValue(SharedPref.getToken(this))
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                SharedPref.setUpdatedToken(OptionActivity.this);
                            }
                        }
                    });
        }
    }
}
