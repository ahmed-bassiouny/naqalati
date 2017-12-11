package com.bassiouny.naqalati.activities;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.bassiouny.naqalati.R;
import com.bassiouny.naqalati.helper.LocationManager;
import com.bassiouny.naqalati.helper.SharedPref;
import com.bassiouny.naqalati.helper.Utils;
import com.bassiouny.naqalati.helper.Constant;
import com.bassiouny.naqalati.model.Driver;
import com.bassiouny.naqalati.model.FirebaseRoot;
import com.bassiouny.naqalati.model.RequestInfo;
import com.bassiouny.naqalati.model.RequestStatus;
import com.bassiouny.naqalati.model.User;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeActivity extends AppCompatActivity implements LocationListener
        , OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    SupportMapFragment mapFragment;
    LocationManager locationManager;

    ImageView edit;
    ProgressBar progress; // this progress to load all data first time
    // local variable
    private final int requestLocationPermission = 123;
    private final int showDriver = 124;
    private double currentLat = 0.0;
    private double currentLng = 0.0;
    private boolean zoomOnMap = true; // to make zoom first time on map
    // have request if true this mean i maked request so marker don't clickable
    private boolean haveRequest = true;
    private GoogleMap googleMap;
    private Marker userMarker;
    private Marker myDriverMarker;
    private String myId = "";
    private String currentRequest = "";
    private Timer timerDrivers;
    ValueEventListener currentRequestListener;
    ValueEventListener listenerOnMyDriver;
    private RelativeLayout container;
    private CircleImageView profileImage;
    private TextView tvDriverName;
    private TextView tvDriverPhone;
    private TextView tvDriverCarNumber;
    private TextView tvTime;
    Button btnArrived, btnCancel;
    private String myDriverId="";
    private String carTypeFilter="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        findViewById();
        initObjects();
        onClick();
        getInfoFromDB();
    }

    private void getInfoFromDB() {
        FirebaseDatabase.getInstance().getReference(FirebaseRoot.DB_USER).child(myId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    // save data in shared pref
                    SharedPref.setInfoUser(HomeActivity.this, user.getUserName(), user.getUserPhone(),
                            user.getUserAvatar());
                    if (user.getCurrentRequest().isEmpty()) {
                        haveRequest = false;
                        startTime();
                    } else {
                        // TODO download request
                        stopTimer();
                        haveRequest = true;
                        currentRequest = user.getCurrentRequest();
                        addListenerOnCurrentRequest();
                    }
                } else {
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
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this,EditProfileActivity.class));
                finish();
            }
        });
        btnArrived.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDatabase.getInstance().getReference(FirebaseRoot.DB_REQUESTS)
                        .child(currentRequest).child(FirebaseRoot.DB_REQUEST_STATUS_IN_REQUESTS)
                        .setValue(RequestStatus.COMPLETE);
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDatabase.getInstance().getReference(FirebaseRoot.DB_REQUESTS)
                        .child(currentRequest).child(FirebaseRoot.DB_REQUEST_STATUS_IN_REQUESTS)
                        .setValue(RequestStatus.CANCEL_FROM_USER);
            }
        });
    }

    private void initObjects() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager = new LocationManager(this, this);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, requestLocationPermission);
        }
        mapFragment.getMapAsync(this);
        setMyId();
        carTypeFilter=getIntent().getStringExtra(Constant.CAR_TYPE_FILTER);
        if(carTypeFilter==null)
            carTypeFilter="";
    }

    private void findViewById() {
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        edit = findViewById(R.id.edit);
        progress = findViewById(R.id.progress);
        container = findViewById(R.id.container);
        profileImage = findViewById(R.id.profile_image);
        tvDriverName = findViewById(R.id.tv_driver_name);
        tvDriverPhone = findViewById(R.id.tv_driver_phone);
        tvDriverCarNumber = findViewById(R.id.tv_driver_car_number);
        tvTime = findViewById(R.id.tv_time);
        btnArrived = findViewById(R.id.btn_arrived);
        btnCancel = findViewById(R.id.btn_cancel);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == requestLocationPermission && grantResults[0] == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, requestLocationPermission);
        } else if (requestCode == requestLocationPermission && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            locationManager = new LocationManager(this, this);
        }
    }

    public void showSettingsAlert() {
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
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        this.googleMap.setOnMarkerClickListener(this);
    }

    private void setLocation() {
        if (googleMap == null)
            return;
        addMeOnMap();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!Utils.isGpsEnable(this)) {
            showSettingsAlert();
        } else {
            if (locationManager != null)
                locationManager.addListener();
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        stopTimer();
        removeListenerOnCurrentRequest();
        removeMyDriverCar();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationManager.removeListener(this);
    }

    private void setMyId() {
        if (myId.isEmpty() && FirebaseAuth.getInstance().getCurrentUser() != null) {
            myId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            Utils.ContactSuppot(this);
        }
    }

    private void startTime() {
        if (timerDrivers == null) {
            timerDrivers = new Timer();
        }
        timerDrivers.scheduleAtFixedRate(getAllDriverByTimer(), 1000, 10000);
    }

    private void stopTimer() {
        if (timerDrivers != null) {
            timerDrivers.cancel();
            timerDrivers = null;
        }
    }

    private TimerTask getAllDriverByTimer() {
        return new TimerTask() {

            @Override
            public void run() {
                getAllDriver();
            }
        };
    }

    private void getAllDriver() {
        FirebaseDatabase.getInstance().getReference(FirebaseRoot.DB_DRIVER).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    googleMap.clear();
                    setLocation();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Driver driver = snapshot.getValue(Driver.class);
                        if (driver.getCurrentRequest().isEmpty()&&driver.getCarType().equals(carTypeFilter))
                            addDriverOnMap(snapshot.getKey(), driver.getLat(), driver.getLng());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void addDriverOnMap(String driverId, Double lat, Double lng) {
        if (lat > 0 && lng > 0) {
            LatLng person = new LatLng(lat, lng);
            MarkerOptions markerOptions = new MarkerOptions().position(person);
            markerOptions.snippet(driverId);
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.car_marker));
            googleMap.addMarker(markerOptions);
        }
    }

    private void addMeOnMap() {
        if (userMarker != null)
            userMarker.remove();
        if (currentLng == 0.0 || currentLat == 0.0)
            return;
        LatLng person = new LatLng(currentLat, currentLng);
        MarkerOptions markerOptions = new MarkerOptions().position(person);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.person_marker));
        userMarker = googleMap.addMarker(markerOptions);
        if (zoomOnMap) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(person));
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(markerOptions.getPosition(), 15), 1000, null);
            zoomOnMap = false;
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (!haveRequest && marker.getSnippet() != null) {
            // send driver id to show info activity
            Intent intent = new Intent(HomeActivity.this, ShowDriverInfoActivity.class);
            intent.putExtra(Constant.SHOW_DRIVER_INFO, marker.getSnippet());
            startActivityForResult(intent, showDriver);
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == showDriver) {
            // this mean i make request and driver accept it
            if (googleMap != null)
                googleMap.clear();
            addMeOnMap();
            getInfoFromDB();
        }
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
        currentLat = location.getLatitude();
        currentLng = location.getLongitude();
        setLocation();
        SharedPref.setLocationUser(HomeActivity.this, currentLat, currentLng);
    }

    private void addListenerOnCurrentRequest() {
        FirebaseDatabase.getInstance().getReference(FirebaseRoot.DB_REQUESTS)
                .child(currentRequest).addValueEventListener(getListenerOnCurrentRequest());
    }

    private void removeListenerOnCurrentRequest() {
        if (currentRequestListener != null)
            FirebaseDatabase.getInstance().getReference(FirebaseRoot.DB_REQUESTS)
                    .child(currentRequest).removeEventListener(currentRequestListener);
    }

    private ValueEventListener getListenerOnCurrentRequest() {
        currentRequestListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                RequestInfo requestInfo = dataSnapshot.getValue(RequestInfo.class);
                if(requestInfo==null)
                    return;
                myDriverId=requestInfo.getDriverId();
                addMyDriverCar();
                if (requestInfo.getRequestStatus() == RequestStatus.DRIVER_GO_TO_START_POINT) {
                    progress.setVisibility(View.VISIBLE);
                    Toast.makeText(HomeActivity.this, R.string.waiting, Toast.LENGTH_SHORT).show();
                    container.setVisibility(View.VISIBLE);
                    setDriverInfo(requestInfo);
                    btnArrived.setVisibility(View.INVISIBLE);
                    googleMap.clear();
                    setLocation();
                    final LatLng driverLocation = new LatLng(requestInfo.getDriverLat(), requestInfo.getDriverLng());
                    final LatLng startPointLocation = new LatLng(requestInfo.getStartPoint().getLat(), requestInfo.getStartPoint().getLng());
                    Routing routing = new Routing.Builder()
                            .travelMode(Routing.TravelMode.DRIVING)
                            .waypoints(driverLocation, startPointLocation)
                            .withListener(new RoutingListener() {
                                @Override
                                public void onRoutingFailure(RouteException e) {

                                }

                                @Override
                                public void onRoutingStart() {

                                }

                                @Override
                                public void onRoutingSuccess(ArrayList<Route> arrayList, int shortestRouteIndex) {
                                    ArrayList polylines = new ArrayList<>();
                                    //add route(s) to the map.
                                    for (int i = 0; i < arrayList.size(); i++) {
                                        PolylineOptions polyOptions = new PolylineOptions();
                                        polyOptions.color(Color.BLUE);
                                        polyOptions.width(10 + i * 3);
                                        polyOptions.addAll(arrayList.get(i).getPoints());
                                        Polyline polyline = googleMap.addPolyline(polyOptions);
                                        polylines.add(polyline);
                                    }

                                    // Start marker
                                    MarkerOptions options = new MarkerOptions();
                                    options.position(driverLocation);
                                    googleMap.addMarker(options);

                                    // End marker
                                    options = new MarkerOptions();
                                    options.position(startPointLocation);
                                    googleMap.addMarker(options);
                                    tvTime.setText(convertTimeToArabic(arrayList.get(arrayList.size() - 1).getDurationText()));
                                    progress.setVisibility(View.GONE);
                                }

                                @Override
                                public void onRoutingCancelled() {

                                }
                            })
                            .build();
                    routing.execute();
                } else if (requestInfo.getRequestStatus() == RequestStatus.DRIVER_GO_TO_END_POINT) {
                    progress.setVisibility(View.VISIBLE);
                    Toast.makeText(HomeActivity.this, R.string.waiting, Toast.LENGTH_SHORT).show();
                    setDriverInfo(requestInfo);
                    container.setVisibility(View.VISIBLE);
                    btnArrived.setVisibility(View.VISIBLE);
                    googleMap.clear();
                    setLocation();
                    final LatLng startPointLocation = new LatLng(requestInfo.getStartPoint().getLat(), requestInfo.getStartPoint().getLng());
                    final LatLng endPointLocation = new LatLng(requestInfo.getEndPoint().getLat(), requestInfo.getEndPoint().getLng());
                    Routing routing = new Routing.Builder()
                            .travelMode(Routing.TravelMode.DRIVING)
                            .waypoints(startPointLocation, endPointLocation)
                            .withListener(new RoutingListener() {
                                @Override
                                public void onRoutingFailure(RouteException e) {

                                }

                                @Override
                                public void onRoutingStart() {
                                }

                                @Override
                                public void onRoutingSuccess(ArrayList<Route> arrayList, int shortestRouteIndex) {
                                    ArrayList polylines = new ArrayList<>();
                                    //add route(s) to the map.
                                    for (int i = 0; i < arrayList.size(); i++) {
                                        PolylineOptions polyOptions = new PolylineOptions();
                                        polyOptions.color(Color.BLUE);
                                        polyOptions.width(10 + i * 3);
                                        polyOptions.addAll(arrayList.get(i).getPoints());
                                        Polyline polyline = googleMap.addPolyline(polyOptions);
                                        polylines.add(polyline);
                                    }

                                    // Start marker
                                    MarkerOptions options = new MarkerOptions();
                                    options.position(startPointLocation);
                                    googleMap.addMarker(options);

                                    // End marker
                                    options = new MarkerOptions();
                                    options.position(endPointLocation);
                                    googleMap.addMarker(options);
                                    tvTime.setText(convertTimeToArabic(arrayList.get(arrayList.size() - 1).getDurationText()));
                                    progress.setVisibility(View.GONE);
                                }

                                @Override
                                public void onRoutingCancelled() {

                                }
                            })
                            .build();
                    routing.execute();
                } else if (requestInfo.getRequestStatus() == RequestStatus.CANCEL_FROM_DRIVER) {
                    Toast.makeText(HomeActivity.this, R.string.driver_cancel, Toast.LENGTH_SHORT).show();
                    googleMap.clear();
                    haveRequest = false;
                    container.setVisibility(View.GONE);
                    progress.setVisibility(View.GONE);
                    removeListenerOnCurrentRequest();
                    removeCurrentRequest();
                    removeMyDriverCar();
                    setLocation();
                    startTime();
                } else if (requestInfo.getRequestStatus() == RequestStatus.CANCEL_FROM_USER) {
                    Toast.makeText(HomeActivity.this, R.string.user_cancel, Toast.LENGTH_SHORT).show();
                    googleMap.clear();
                    haveRequest = false;
                    container.setVisibility(View.GONE);
                    progress.setVisibility(View.GONE);
                    removeListenerOnCurrentRequest();
                    removeCurrentRequest();
                    removeMyDriverCar();
                    setLocation();
                    startTime();
                } else if (requestInfo.getRequestStatus() == RequestStatus.COMPLETE) {
                    Toast.makeText(HomeActivity.this, R.string.complete, Toast.LENGTH_SHORT).show();
                    googleMap.clear();
                    haveRequest = false;
                    container.setVisibility(View.GONE);
                    progress.setVisibility(View.GONE);
                    removeListenerOnCurrentRequest();
                    removeCurrentRequest();
                    removeMyDriverCar();
                    setLocation();
                    startTime();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progress.setVisibility(View.GONE);
            }
        };
        return currentRequestListener;
    }

    private void setDriverInfo(RequestInfo userInfo) {
        tvDriverName.setText(userInfo.getDriverName());
        tvDriverPhone.setText(userInfo.getDriverPhone());
        tvDriverCarNumber.setText(userInfo.getCarNumber());
        if (!userInfo.getDriverImage().isEmpty())
            Utils.showImage(this, userInfo.getDriverImage(), profileImage);
    }

    private String convertTimeToArabic(String time) {
        return time.replace("hours", "ساعة").replace("mins", "دقيقة");
    }

    public void removeCurrentRequest() {
        FirebaseDatabase.getInstance().getReference(FirebaseRoot.DB_USER)
                .child(myId).child(FirebaseRoot.DB_CURRENT_REQUEST).setValue("");
        currentRequest = "";
    }

    private ValueEventListener getListenerOnMyDriver() {
        if (listenerOnMyDriver == null)
            listenerOnMyDriver = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Driver driver =dataSnapshot.getValue(Driver.class);
                    if(driver==null)
                        return;
                    if(myDriverMarker!=null)
                        myDriverMarker.remove();
                    LatLng person = new LatLng(driver.getLat(), driver.getLng());
                    MarkerOptions markerOptions = new MarkerOptions().position(person);
                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.car_marker));
                    myDriverMarker = googleMap.addMarker(markerOptions);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
        return listenerOnMyDriver;
    }
    private void addMyDriverCar(){
        if(listenerOnMyDriver==null)
        FirebaseDatabase.getInstance().getReference(FirebaseRoot.DB_DRIVER)
                .child(myDriverId).addValueEventListener(getListenerOnMyDriver());
    }
    private void removeMyDriverCar(){
        if(listenerOnMyDriver!=null) {
            FirebaseDatabase.getInstance().getReference(FirebaseRoot.DB_DRIVER)
                    .child(myDriverId).removeEventListener(listenerOnMyDriver);
            listenerOnMyDriver=null;
        }
        if(myDriverMarker!=null) {
            myDriverMarker.remove();
            myDriverMarker=null;
        }
    }

}
