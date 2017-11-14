package com.ntamtech.naqalati.activities;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.awesomedialog.blennersilva.awesomedialoglibrary.AwesomeInfoDialog;
import com.awesomedialog.blennersilva.awesomedialoglibrary.interfaces.Closure;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ntamtech.naqalati.R;
import com.ntamtech.naqalati.helper.SharedPref;
import com.ntamtech.naqalati.helper.Utils;
import com.ntamtech.naqalati.helper.Constant;
import com.ntamtech.naqalati.model.Driver;
import com.ntamtech.naqalati.model.FirebaseRoot;
import com.ntamtech.naqalati.model.User;

import java.util.Timer;
import java.util.TimerTask;

public class HomeActivity extends AppCompatActivity implements LocationListener
        ,OnMapReadyCallback,GoogleMap.OnMarkerClickListener{

    SupportMapFragment mapFragment;
    LocationManager locationManager;
    ImageView signout;
    ProgressBar progress; // this progress to load all data first time
    // local variable
    private final int requestLocationPermission =123;
    private final int showDriver = 124;
    private double currentLat=0.0;
    private double currentLng=0.0;
    private boolean zoomOnMap =true; // to make zoom first time on map
    // have request if true this mean i maked request so marker don't clickable
    private boolean haveRequest=true;
    private GoogleMap googleMap;
    private Marker userMarker;
    private String myId="";
    private Timer timerDrivers;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        findViewById();
        initObjects();
        onClick();
        zoomOnMap=true;
        initLocationListener();
        getInfoFromDB();
    }

    private void getInfoFromDB() {
        FirebaseDatabase.getInstance().getReference(FirebaseRoot.DB_USER).child(myId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if(user!=null){
                    SharedPref.setLocationUser(HomeActivity.this,user.getLat(),user.getLng());
                    if(user.getCurrentRequest().isEmpty()){
                        haveRequest=false;
                        currentLat=user.getLat();
                        currentLng=user.getLng();
                        setLocation();
                        startTime();
                    }else {
                        // TODO download request
                        haveRequest=true;
                        Toast.makeText(HomeActivity.this, "i have request", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Utils.ContactSuppot(HomeActivity.this);
                }
                progress.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Utils.ContactSuppot(HomeActivity.this);
                progress.setVisibility(View.GONE);
            }
        });
    }

    private void onClick() {
        signout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AwesomeInfoDialog(HomeActivity.this)
                        .setTitle(R.string.app_name)
                        .setMessage(R.string.you_want_signout)
                        .setColoredCircle(R.color.dialogInfoBackgroundColor)
                        .setDialogIconAndColor(R.drawable.ic_dialog_info, R.color.white)
                        .setCancelable(true)
                        .setPositiveButtonText(getString(R.string.yes))
                        .setPositiveButtonbackgroundColor(R.color.red_logo)
                        .setPositiveButtonTextColor(R.color.white)
                        .setNegativeButtonText(getString(R.string.no))
                        .setNegativeButtonbackgroundColor(R.color.dialogInfoBackgroundColor)
                        .setNegativeButtonTextColor(R.color.white)
                        .setPositiveButtonClick(new Closure() {
                            @Override
                            public void exec() {
                                FirebaseAuth.getInstance().signOut();
                                startActivity(new Intent(HomeActivity.this,SigninActivity.class));
                                finish();
                            }
                        })
                        .setNegativeButtonClick(new Closure() {
                            @Override
                            public void exec() {
                            }
                        })
                        .show();
            }
        });
    }

    private void initLocationListener() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates( LocationManager.GPS_PROVIDER,5000,0, this);
            locationManager.requestLocationUpdates( LocationManager.NETWORK_PROVIDER,3000,0, this);
        }else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, requestLocationPermission);
        }
    }

    private void initObjects() {
        mapFragment.getMapAsync(this);
        locationManager=(LocationManager) getSystemService(Context.LOCATION_SERVICE);
        setMyId();
    }

    private void findViewById() {
        mapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
        signout=findViewById(R.id.signout);
        progress = findViewById(R.id.progress);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode== requestLocationPermission &&grantResults[0]==PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, requestLocationPermission);
        }
    }

    public void showSettingsAlert(){
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(getString(R.string.open_gps));
        alertDialog.setCancelable(false);

        alertDialog.setPositiveButton(getString(R.string.setting), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                    dialog.dismiss();
            }
        });
        alertDialog.show();
    }

    @Override
    public void onLocationChanged(Location location) {
        FirebaseDatabase.getInstance().getReference(FirebaseRoot.DB_USER)
                .child(myId)
                .child(FirebaseRoot.DB_LAT)
                .setValue(location.getLatitude());

        FirebaseDatabase.getInstance().getReference(FirebaseRoot.DB_USER)
                .child(myId)
                .child(FirebaseRoot.DB_LNG)
                .setValue(location.getLongitude());
        currentLat=location.getLatitude();
        currentLng=location.getLongitude();
        setLocation();
        SharedPref.setLocationUser(HomeActivity.this,currentLat,currentLng);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {
          if(provider.equals(LocationManager.GPS_PROVIDER)){
            showSettingsAlert();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap=googleMap;
        this.googleMap.setOnMarkerClickListener(this);
    }
    private void setLocation(){
        if(googleMap==null)
            return;
        addMeOnMap();
    }

    @Override
    protected void onStop() {
        super.onStop();
        locationManager.removeUpdates(this);
        stopTimer();
    }
    private void setMyId(){
        if(myId.isEmpty() && FirebaseAuth.getInstance().getCurrentUser()!=null){
            myId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }else {
            Utils.ContactSuppot(this);
        }
    }

    private void startTime() {
        if(timerDrivers == null) {
            timerDrivers = new Timer();
        }
        timerDrivers.scheduleAtFixedRate(getAllDriverByTimer(), 1000, 5000);
    }
    private void stopTimer(){
        if(timerDrivers!=null) {
            timerDrivers.cancel();
            timerDrivers=null;
        }
    }

    private TimerTask getAllDriverByTimer(){
        return new TimerTask() {

            @Override
            public void run() {
                getAllDriver();
            }
        };
    }
    private void getAllDriver(){
        FirebaseDatabase.getInstance().getReference(FirebaseRoot.DB_DRIVER).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot!=null){
                    for (DataSnapshot snapshot:dataSnapshot.getChildren()){
                        Driver driver = snapshot.getValue(Driver.class);
                        if(driver.getCurrentRequest().isEmpty())
                            addDriverOnMap(snapshot.getKey(),driver.getLat(),driver.getLng());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    private void addDriverOnMap(String driverId,Double lat,Double lng){
        if(lat>0 &&lng>0) {
            LatLng person = new LatLng(lat, lng);
            MarkerOptions markerOptions = new MarkerOptions().position(person);
            markerOptions.snippet(driverId);
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.car_marker));
            googleMap.addMarker(markerOptions);
        }
    }

    private void addMeOnMap() {
        if(userMarker!=null)
            userMarker.remove();
        if(currentLng==0.0 || currentLat==0.0)
            return;
        LatLng person = new LatLng(currentLat,currentLng);
        MarkerOptions markerOptions =new MarkerOptions().position(person);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.person_marker));
        userMarker= googleMap.addMarker(markerOptions);
        if(zoomOnMap) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(person));
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(markerOptions.getPosition(), 15), 1000, null);
            zoomOnMap=false;
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if(!haveRequest&&marker.getSnippet()!=null){
            // send driver id to show info activity
            Intent intent=new Intent(HomeActivity.this,ShowDriverInfoActivity.class);
            intent.putExtra(Constant.SHOW_DRIVER_INFO,marker.getSnippet());
            startActivityForResult(intent,showDriver);
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK && requestCode==showDriver){
            // this mean i make request and driver accept it
            if(googleMap!=null)
                googleMap.clear();
            addMeOnMap();
            getInfoFromDB();
        }
    }
}
