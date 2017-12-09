package com.bassiouny.naqalati.activities;

import android.app.Activity;
import android.location.Address;
import android.location.Geocoder;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
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
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.bassiouny.naqalati.R;
import com.bassiouny.naqalati.helper.SharedPref;
import com.bassiouny.naqalati.helper.Utils;
import com.bassiouny.naqalati.helper.Constant;
import com.bassiouny.naqalati.model.Driver;
import com.bassiouny.naqalati.model.FirebaseRoot;
import com.bassiouny.naqalati.model.Point;
import com.bassiouny.naqalati.model.RequestInfo;
import com.bassiouny.naqalati.model.RequestStatus;

import java.io.IOException;
import java.util.List;

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
    private Driver driver;
    private TextView tvPrice;
    private Button btnAccept;
    private String priceRequest;
    private TextInputEditText etProductType,etProductSize;

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
                 driver = dataSnapshot.getValue(Driver.class);
                if(driver!=null){
                    tvDriverName.setText(driver.getUserName());
                    tvDriverPhone.setText(driver.getUserPhone());
                    carType.append(driver.getCarType());
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
                }else {
                    // make request
                    if(requestInfo.getStartPoint()==null){
                        Toast.makeText(ShowDriverInfoActivity.this, getString(R.string.start_point), Toast.LENGTH_SHORT).show();
                        return;
                    }else if(requestInfo.getEndPoint()==null){
                        Toast.makeText(ShowDriverInfoActivity.this, getString(R.string.end_point), Toast.LENGTH_SHORT).show();
                        return;
                    }else if(etProductType.getText().toString().trim().isEmpty()){
                        etProductType.setError("برجاء ادخال نوع البضاعة");
                        return;
                    }else if(etProductSize.getText().toString().trim().isEmpty()){
                        etProductSize.setError("برجاء ادخال الكمية");
                        return;
                    }
                    createRequest();
                    addListenerForRequest();
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
        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateRealRequest();
                setResult(Activity.RESULT_OK);
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
        tvPrice = findViewById(R.id.tv_price);
        btnAccept = findViewById(R.id.btn_accept);
        fragmentStartPoint = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.fragment_start_point);
        fragmentEndPoint = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.fragment_end_point);
        chSelectMyLocation=findViewById(R.id.ch_select_myLocation);
        etProductType = findViewById(R.id.et_product_type);
        etProductSize = findViewById(R.id.et_product_size);
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
        requestInfo.setUserInfo(userId, SharedPref.getUserName(this),SharedPref.getPhone(this),SharedPref.getUserImage(this)
        ,Double.parseDouble(SharedPref.getUserLat(this)),Double.parseDouble(SharedPref.getUserLng(this)));
        //set product type and size
        requestInfo.setProductType(etProductType.getText().toString());
        requestInfo.setProductSize(etProductSize.getText().toString());
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
        // request id = driver id - user id
        String requestId = driverId+"-"+ userId;
        FirebaseDatabase.getInstance().getReference(FirebaseRoot.DB_DRIVER)
                .child(driverId).child(FirebaseRoot.DB_PENDING_REQUEST).child(requestId)
                .child(FirebaseRoot.DB_PRICE).addValueEventListener(getRequestStatueListener());
    }
    private void removeRequestStatueListener(){
        if(requestStatueListener==null)
            return;
        String requestId = driverId+"-"+ userId;
        FirebaseDatabase.getInstance().getReference(FirebaseRoot.DB_DRIVER)
                .child(driverId).child(FirebaseRoot.DB_PENDING_REQUEST).child(requestId)
                .child(FirebaseRoot.DB_PRICE).removeEventListener(requestStatueListener);
    }

    private ValueEventListener getRequestStatueListener(){
        requestStatueListener =new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot!=null){
                    priceRequest = dataSnapshot.getValue(String.class);
                    if(priceRequest!=null&&Integer.parseInt(priceRequest)>0){
                        tvPrice.setText("السعر المطلوب : "+priceRequest);
                        tvPrice.setVisibility(View.VISIBLE);
                        btnAccept.setVisibility(View.VISIBLE);
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
        String address = objAddress.getAddressLine(0);
        String city = objAddress.getLocality();
        String state = objAddress.getAdminArea();
        String fullAddress = address+" "+state +" "+ city;
        return fullAddress.replace("null","");
    }
    private void CreateRealRequest(){
        // Complete required attribte in request info object
        requestInfo.setDriverName(driver.getUserName());
        requestInfo.setDriverPhone(driver.getUserPhone());
        requestInfo.setDriverImage(driver.getUserAvatar());
        requestInfo.setUserId(userId);
        requestInfo.setDriverId(driverId);
        requestInfo.setDriverLat(driver.getLat());
        requestInfo.setDriverLng(driver.getLng());
        requestInfo.setRequestStatus(RequestStatus.DRIVER_GO_TO_START_POINT);
        requestInfo.setCarNumber(driver.getCarNumber());
        requestInfo.setCarType(driver.getCarType());
        requestInfo.setPrice(priceRequest);
        requestInfo.setDate(Utils.getCurrentDate());
        // generate key for request
        String key =FirebaseDatabase.getInstance().getReference(FirebaseRoot.DB_REQUESTS).push().getKey();
        // update currentRequest in driver
        FirebaseDatabase.getInstance().getReference(FirebaseRoot.DB_DRIVER)
                .child(driverId).child(FirebaseRoot.DB_CURRENT_REQUEST)
                .setValue(key);
        // update currentRequest in driver
        FirebaseDatabase.getInstance().getReference(FirebaseRoot.DB_USER)
                .child(userId).child(FirebaseRoot.DB_CURRENT_REQUEST)
                .setValue(key);
        FirebaseDatabase.getInstance().getReference(FirebaseRoot.DB_REQUESTS).child(key)
                .setValue(requestInfo);
    }
    private void addListenerForRequest(){
        FirebaseDatabase.getInstance().getReference(FirebaseRoot.DB_DRIVER)
                .child(driverId).child(FirebaseRoot.DB_PENDING_REQUEST)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {
                        finish();
                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }
}
