package com.ntamtech.naqalati.activities;

import android.location.Address;
import android.location.Geocoder;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ntamtech.naqalati.R;
import com.ntamtech.naqalati.helper.SharedPref;
import com.ntamtech.naqalati.helper.Utils;
import com.ntamtech.naqalati.model.CarType;
import com.ntamtech.naqalati.helper.Constant;
import com.ntamtech.naqalati.model.Driver;
import com.ntamtech.naqalati.model.FirebaseRoot;
import com.ntamtech.naqalati.model.Point;
import com.ntamtech.naqalati.model.RequestInfo;
import com.ntamtech.naqalati.model.RequestStatus;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class ShowDriverInfoActivity extends AppCompatActivity {


    String driverId;
    String userId;
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
    private ValueEventListener requestStatueListener;
    private PlaceAutocompleteFragment fragmentStartPoint;
    private PlaceAutocompleteFragment fragmentEndPoint;
    private RequestInfo requestInfo ;
    private CheckBox chSelectMyLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_show_driver_info);
        findViewById();
        initObjects();
        onClick();
        getDriverId();
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
                        containerSubInfo.setVisibility(View.INVISIBLE);
                    }else{
                        // this driver busy so show info request
                        btnRequestDriver.setText(getString(R.string.cancel_request));
                        haveRequest=true;
                        imgClose.setVisibility(View.INVISIBLE);
                        containerSubInfo.setVisibility(View.VISIBLE);
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
                    deleteRequest();
                    imgClose.setVisibility(View.VISIBLE);
                    haveRequest=false;
                    btnRequestDriver.setText(getString(R.string.request_driver));
                    containerSubInfo.setVisibility(View.INVISIBLE);
                }else {
                    // make request
                    if(requestInfo.getStartPoint()==null){
                        Toast.makeText(ShowDriverInfoActivity.this, getString(R.string.start_point), Toast.LENGTH_SHORT).show();
                        return;
                    }else if(requestInfo.getEndPoint()==null){
                        Toast.makeText(ShowDriverInfoActivity.this, getString(R.string.end_point), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    createRequest();
                    imgClose.setVisibility(View.INVISIBLE);
                    haveRequest=true;
                    btnRequestDriver.setText(getString(R.string.cancel_request));
                    containerSubInfo.setVisibility(View.VISIBLE);
                }
            }
        });
        imgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        fragmentStartPoint.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Point point = new Point();
                point.setLat(place.getLatLng().latitude);
                point.setLng(place.getLatLng().longitude);
                point.setLocationString(place.getAddress().toString());
                requestInfo.setStartPoint(point);
            }

            @Override
            public void onError(Status status) {

            }
        });
        fragmentEndPoint.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Point point = new Point();
                point.setLat(place.getLatLng().latitude);
                point.setLng(place.getLatLng().longitude);
                point.setLocationString(place.getAddress().toString());
                requestInfo.setEndPoint(point);
            }

            @Override
            public void onError(Status status) {
            }
        });
        chSelectMyLocation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    fragmentStartPoint.setText("");
                    Point point = new Point();
                    Double lat = Double.parseDouble(SharedPref.getUserLat(ShowDriverInfoActivity.this));
                    Double lng = Double.parseDouble(SharedPref.getUserLng(ShowDriverInfoActivity.this));
                    point.setLat(lat);
                    point.setLng(lng);
                    Geocoder geocoder = new Geocoder(ShowDriverInfoActivity.this);
                    List<Address> addresses = null;
                    try {
                        addresses = geocoder.getFromLocation(lat, lng, 1);
                        point.setLocationString(createFullAddress(addresses.get(0)));
                    } catch (IOException e) {
                        point.setLocationString("");
                    }
                    requestInfo.setStartPoint(point);
                }else {
                    requestInfo.setStartPoint(null);
                }
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
        if(FirebaseAuth.getInstance().getCurrentUser()==null){
            Utils.ContactSuppot(this);
            finish();
        }
        userId=FirebaseAuth.getInstance().getCurrentUser().getUid();
        requestInfo = new RequestInfo();
        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                .setCountry("EG")
                .build();

        fragmentStartPoint.setFilter(typeFilter);
        fragmentEndPoint.setFilter(typeFilter);
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
        fragmentStartPoint = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.fragment_start_point);
        fragmentEndPoint = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.fragment_end_point);
        chSelectMyLocation=findViewById(R.id.ch_select_myLocation);
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
        // request id = driver id - user id
        String requestId = driverId+"-"+ userId;
        // update in my data to make request status = waiting
        FirebaseDatabase.getInstance().getReference(FirebaseRoot.DB_USER)
                .child(userId).child(FirebaseRoot.DB_REQUEST_STATUS).setValue(RequestStatus.WAITING);
        // create request info object to save
        requestInfo.setUserInfo(userId, SharedPref.getUserName(this),SharedPref.getPhone(this),SharedPref.getUserImage(this));
        // save request info in driver (pending requests root)
        FirebaseDatabase.getInstance().getReference(FirebaseRoot.DB_DRIVER)
                .child(driverId).child(FirebaseRoot.DB_PENDING_REQUEST).child(requestId)
                .setValue(requestInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    initRequestStatueListener();
                }
            }
        });
    }
    private void initRequestStatueListener(){
        FirebaseDatabase.getInstance().getReference(FirebaseRoot.DB_USER)
                .child(userId).child(FirebaseRoot.DB_REQUEST_STATUS).addValueEventListener(getRequestStatueListener());
    }
    private void removeRequestStatueListener(){
        if(requestStatueListener==null)
            return;
        FirebaseDatabase.getInstance().getReference(FirebaseRoot.DB_USER)
                .child(userId).child(FirebaseRoot.DB_REQUEST_STATUS).removeEventListener(requestStatueListener);
    }

    private ValueEventListener getRequestStatueListener(){
        requestStatueListener =new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot!=null){
                    RequestStatus requestStatus = dataSnapshot.getValue(RequestStatus.class);
                    if(requestStatus==RequestStatus.ACCEPT){
                        Toast.makeText(ShowDriverInfoActivity.this, R.string.request_accept, Toast.LENGTH_LONG).show();
                        finish();
                    }else if (requestStatus==RequestStatus.REFUSE) {
                        Toast.makeText(ShowDriverInfoActivity.this, R.string.request_refuse, Toast.LENGTH_LONG).show();
                        finish();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        return requestStatueListener;
    }

    @Override
    protected void onStop() {
        super.onStop();
        deleteRequest();
        finish();
    }
    private void deleteRequest(){
        String requestId = driverId+"-"+ userId;
        FirebaseDatabase.getInstance().getReference(FirebaseRoot.DB_DRIVER)
                .child(driverId).child(FirebaseRoot.DB_PENDING_REQUEST).child(requestId).removeValue();
        FirebaseDatabase.getInstance().getReference(FirebaseRoot.DB_USER)
                .child(userId).child(FirebaseRoot.DB_REQUEST_STATUS).setValue(RequestStatus.NO_REQUEST);
        removeRequestStatueListener();
    }
    private String createFullAddress(Address objAddress){
        String address = objAddress.getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
        String city = objAddress.getLocality();
        String state = objAddress.getAdminArea();
        String fullAddress = address+" "+state +" "+ city;
        return fullAddress.replace("null","");
    }
}
