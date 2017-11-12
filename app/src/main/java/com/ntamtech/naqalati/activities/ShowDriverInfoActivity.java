package com.ntamtech.naqalati.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ntamtech.naqalati.R;
import com.ntamtech.naqalati.helper.Utils;
import com.ntamtech.naqalati.model.CarType;
import com.ntamtech.naqalati.helper.Constant;
import com.ntamtech.naqalati.model.Driver;
import com.ntamtech.naqalati.model.FirebaseRoot;

import de.hdodenhof.circleimageview.CircleImageView;

public class ShowDriverInfoActivity extends AppCompatActivity {


    String driverId;
    private boolean haveRequest=false;
    private LinearLayout containerInfo;
    private CircleImageView profileImage;
    private TextView tvDriverName;
    private TextView tvDriverPhone;
    private TextView carType;
    private TextView carNumber;
    private LinearLayout containerSubInfo;
    private ProgressBar progressSubInfo;
    private TextView tvWaiting;
    private ProgressBar progressInfo;
    private Button btnRequestDriver;
    private ImageView imgClose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_show_driver_info);
        findViewById();
        onClick();
        getDriverId();
        initObjects();
        loadData();
    }

    private void loadData() {
        FirebaseDatabase.getInstance().getReference(FirebaseRoot.DB_DRIVER)
                .child(driverId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Driver driver = dataSnapshot.getValue(Driver.class);
                if(driver!=null){
                    tvDriverName.setText(driver.getUserName());
                    tvDriverPhone.setText(driver.getUserPhone());
                    carType.append(getCarTypeString(driver.getCarType()));
                    carNumber.append(driver.getCarNumber());
                    if(driver.getUserAvatar()!=null && !driver.getUserAvatar().isEmpty())
                        Utils.showImage(ShowDriverInfoActivity.this,driver.getUserAvatar(),profileImage);
                    if(driver.getCurrentRequest().isEmpty()){
                        // this driver free to request it
                        btnRequestDriver.setText(getString(R.string.request_driver));
                        containerInfo.setVisibility(View.VISIBLE);
                        progressInfo.setVisibility(View.INVISIBLE);
                        haveRequest=false;
                        imgClose.setVisibility(View.VISIBLE);
                    }else{
                        // this driver busy so show info request
                        btnRequestDriver.setText(getString(R.string.cancel_request));
                        haveRequest=true;
                        imgClose.setVisibility(View.INVISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Utils.ContactSuppot(ShowDriverInfoActivity.this);
            }
        });
    }

    private void onClick() {
        btnRequestDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(haveRequest){
                    // cancel request
                    imgClose.setVisibility(View.VISIBLE);
                    haveRequest=false;
                    btnRequestDriver.setText(getString(R.string.request_driver));
                }else {
                    // make request
                    imgClose.setVisibility(View.INVISIBLE);
                    haveRequest=true;
                    btnRequestDriver.setText(getString(R.string.cancel_request));
                }
            }
        });
        imgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void getDriverId() {
        driverId = getIntent().getStringExtra(Constant.SHOW_DRIVER_INFO);
        if(driverId==null || driverId.isEmpty()){
            Utils.ContactSuppot(this);
            finish();
        }
    }

    private void initObjects() {
    }

    private void findViewById() {
        containerInfo = findViewById(R.id.container_info);
        profileImage = findViewById(R.id.profile_image);
        tvDriverName = findViewById(R.id.tv_driver_name);
        tvDriverPhone = findViewById(R.id.tv_driver_phone);
        carType = findViewById(R.id.car_type);
        carNumber = findViewById(R.id.car_number);
        btnRequestDriver = findViewById(R.id.btn_request_driver);
        containerSubInfo = findViewById(R.id.container_sub_info);
        progressSubInfo = findViewById(R.id.progress_sub_info);
        tvWaiting = findViewById(R.id.tv_waiting);
        progressInfo = findViewById(R.id.progress_info);
        imgClose = findViewById(R.id.img_close);
    }
    private String getCarTypeString(CarType carType){
        String result="";
        switch (carType){
            case FULL: result="نقل" ;break;
            case MEDIUM: result="نص نقل" ;break;
            case SMALL: result="ربع نقل" ;break;
            default: result="نقل" ;
        }
        return result;
    }

    @Override
    public void onBackPressed() {
    }
    private void createRequest(){

    }
}
