package com.example.arjay.erahacks;

import android.Manifest;
import com.google.android.gms.location.LocationCallback;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.bumptech.glide.Glide;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class RescuerMapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, RoutingListener {

    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LocationRequest mLocationRequest;
    private SupportMapFragment mapFragment;
    private Button mLogout,mSettings, mrescueStatus;
    private FusedLocationProviderClient mFusedLocationClient;

    private Boolean isLoggingOut = false;

    private String rescueeId = "", MyCity, mins;

    private LatLng pickupLatLng;

    private LinearLayout mRescueeInfo;
    private ImageView mRescueeProfileImage;
    private TextView mRescueeName,mRescueePhone,mRescueeAddress,mDesc,mGetDep,mGetServ;

    private RequestQueue requestQueue;

    private int status = 0;

    private Button mDescription;
    private LinearLayout mDescLayout, mLabelLayout, mRescuerDescLayout;
    private EditText mDescBox, mRescueeDescText;

    private String timeOfRequest;


    private Switch mOnlineSwitch;

    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.primary_dark_material_light};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rescuer_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        polylines = new ArrayList<>();
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mLogout = (Button)findViewById(R.id.logout);
        mSettings = (Button)findViewById(R.id.settings);

        mRescueeInfo = (LinearLayout)findViewById(R.id.rescueeInfo);
        mRescueeProfileImage = (ImageView) findViewById(R.id.rescueePofileImage);
        mRescueeName = (TextView) findViewById(R.id.rescueeName);
        mRescueePhone = (TextView) findViewById(R.id.rescueePhone);
        mrescueStatus = (Button)findViewById(R.id.rescueStatus);
        mRescueeAddress = (TextView) findViewById(R.id.rescueeAddress);
        mDesc = (TextView) findViewById(R.id.description);
        mLabelLayout = findViewById(R.id.labelLayout);

        mRescuerDescLayout = findViewById(R.id.rescuerDescLayout);
        mRescueeDescText = findViewById(R.id.descText);

        mGetDep = findViewById(R.id.tvDepartmentOutput);
        mGetServ = findViewById(R.id.tvServiceOutput);


        mDescription =findViewById(R.id.btnDescription);
        mDescBox =findViewById(R.id.descriptionBox);
        mDescLayout =findViewById(R.id.descLayout);
        mDescLayout.bringToFront();
        mLabelLayout.bringToFront();
        mRescuerDescLayout.bringToFront();


        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getRescueeDescription();
                    }
                });
            }
        },0,1000);


        mOnlineSwitch = (Switch)findViewById(R.id.onlineSwitch);
        mOnlineSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked){
                    connectRescuer();
                    mOnlineSwitch.setText("online");
                }
                else{
                    disconnectDriver();
                    mOnlineSwitch.setText("offline");
                }
            }
        });

        mrescueStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (status){
                    case 1:
                        mDescLayout.setVisibility(View.GONE);
                        mRescuerDescLayout.setVisibility(View.VISIBLE);
                        status = 2;
                        erasePolylines();
                        sendNotification();
                        mrescueStatus.setText("COMPLETE MISSION");
                        break;
                    case 2:
                        mRescuerDescLayout.setVisibility(View.VISIBLE);
                        String checker = mRescueeDescText.getText().toString();
                        if (checker.equals("")){
                            status = 1;
                            return;
                        }
                        else {
                            recordRescue();
                            endRescue();
                            mDescLayout.setVisibility(View.GONE);
                            break;
                        }
                }
            }
        });

        mDescription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDescLayout.setVisibility(View.GONE);
                getDescription();
                mDescBox.setText("");
            }
        });

        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isLoggingOut = true;
                disconnectDriver();
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(RescuerMapActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });

        mSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RescuerMapActivity.this, RescuerSettingsActivity.class);
                startActivity(intent);
                return;
            }
        });



        getAssignedRescuee();
        label();

    }


    private void label(){
        String user = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (user != null){
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users").child("Rescuer").child(user);
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if (dataSnapshot.exists() && dataSnapshot.getChildrenCount()> 0){
                        if (map.get("service")!=null){
                            String serviceLabel;
                            serviceLabel =map.get("service").toString();
                            mGetServ.setText(serviceLabel);
                        }
                        if (map.get("department name")!=null){
                            String serviceLabel;
                            serviceLabel =map.get("department name").toString();
                            mGetDep.setText(serviceLabel);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

    }

    private void getDescription(){
        String desc;
        desc = mDescBox.getText().toString();
        DatabaseReference descRef = FirebaseDatabase.getInstance().getReference().child("rescueeRequest").child(rescueeId);

        if(desc.equals("")){
            desc = "no description yet";
            HashMap map = new HashMap();
            map.put("rescuerMessage", desc);
            descRef.updateChildren(map);
        }

        else{
            HashMap map = new HashMap();
            map.put("rescuerMessage", desc);
            descRef.updateChildren(map);
        }
    }
    private void getTimeOfRequest(){

        timeOfRequest = getCurrentTimestamp().toString();

    }

    private void getAssignedRescuee(){
        String rescuerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference assignedRescueeRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Rescuer").child(rescuerId).child("rescueeRequest").child("rescueeRideId");
        assignedRescueeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    status = 1;
                    rescueeId = dataSnapshot.getValue().toString();
                    getAssignedRescueePickUpLocation();
                    getAssignedRescueeInfo();
                    getTimeOfRequest();
                    mDescLayout.setVisibility(View.VISIBLE);
                    showNoti();
                    Intent i = new Intent(RescuerMapActivity.this,Alarm.class);
                    startActivity(i);
                }
                else{
                    endRescue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }



    private void endRescue(){
        mrescueStatus.setText("CONFIRM ARRIVAL");
        erasePolylines();

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference rescuerRef =FirebaseDatabase.getInstance().getReference().child("Users").child("Rescuer").child(userId).child("rescueeRequest");
        rescuerRef.removeValue();


        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("rescueeRequest");
        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(rescueeId);
        rescueeId = "";

        if(pickupMarker != null){
            pickupMarker.remove();
            mMap.clear();
        }
        if(assignedRescueePickUpLocationRefListener != null){
            assignedRescueePickUpLocationRef.removeEventListener(assignedRescueePickUpLocationRefListener);
        }

        mRescueeInfo.setVisibility(View.GONE);
        mRescueeName.setText("");
        mRescueePhone.setText("");
        mRescueeProfileImage.setImageResource(R.drawable.defaultpic);
    }

    private void recordRescue() {

        String rescueeReport = mRescueeDescText.getText().toString();

        String rescueeDesc = mDesc.getText().toString();

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference rescuerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Rescuer").child(userId).child("history");
        DatabaseReference rescueeRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Rescuee").child(rescueeId).child("history");
        DatabaseReference historyRef = FirebaseDatabase.getInstance().getReference().child("history");
        String requestId = historyRef.push().getKey();
        rescuerRef.child(requestId).setValue(true);
        rescueeRef.child(requestId).setValue(true);

        HashMap map = new HashMap();

        map.put("driver", userId);
        map.put("customer", rescueeId);
        map.put("rating", 0);
        map.put("timestamp", getCurrentTimestamp());
        map.put("descriptionPic", "none");
        map.put("address", MyCity);
        map.put("description", rescueeDesc);
        map.put("location/from/lat", pickupLatLng.latitude);
        map.put("location/from/lng", pickupLatLng.longitude);
        map.put("Rescuer's report", rescueeReport);
        map.put("Time of request", timeOfRequest);

        mRescueeDescText.setText("");
        mRescuerDescLayout.setVisibility(View.GONE);

        historyRef.child(requestId).updateChildren(map);
    }







    private Long getCurrentTimestamp() {
        Long timestamp = System.currentTimeMillis() / 1000;
        return timestamp;
    }
    private void getRescueeDescription(){
        DatabaseReference descRef = FirebaseDatabase.getInstance().getReference().child("rescueeRequest").child(rescueeId);
        descRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if (map.get("description") != null) {
                        mDesc.setText(map.get("description").toString());
                    }
                    else{
                        mDesc.setText("Rescuee's report will be updated here.");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void sendNotification(){
        DatabaseReference sendNotif = FirebaseDatabase.getInstance().getReference().child("rescueeRequest").child(rescueeId);

        HashMap map = new HashMap();
        map.put("arrivalUpdate","1");

        sendNotif.updateChildren(map);



    }

    private void getAssignedRescueeInfo(){
        mRescueeInfo.setVisibility(View.VISIBLE);
        DatabaseReference mRescueeDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Rescuee").child(rescueeId);
        mRescueeDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    Map<String, Object> map = (Map<String, Object>)dataSnapshot.getValue();
                    if(map.get("name") != null){
                        mRescueeName.setText(map.get("name").toString());
                    }
                    if(map.get("phone") != null){
                        mRescueePhone.setText(map.get("phone").toString());
                    }
                    if(map.get("profileImageUrl") != null){
                        Glide.with(getApplication()).load(map.get("profileImageUrl").toString()).into(mRescueeProfileImage);
                    }

                    getRescueeAddress();
                }
            }
            private DatabaseReference addressRef;
            private ValueEventListener addressRefListener;

            public void getRescueeAddress(){
                addressRef = FirebaseDatabase.getInstance().getReference().child("rescueeRequest").child(rescueeId).child("l");
                addressRefListener = assignedRescueePickUpLocationRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            List<Object> map = (List<Object>) dataSnapshot.getValue();
                            double locationLat = 0;
                            double locationLng = 0;
                            if(map.get(0) != null){
                                locationLat = Double.parseDouble(map.get(0).toString());
                            }
                            if(map.get(1) != null){
                                locationLng = Double.parseDouble(map.get(1).toString());
                            }
                            MyCity = "";
                            Geocoder geocoder = new Geocoder(RescuerMapActivity.this, Locale.getDefault());
                            try {
                                List<Address> addressess = geocoder.getFromLocation(locationLat, locationLng, 1);
                                MyCity = addressess.get(0).getAddressLine(0);
                                mRescueeAddress.setText(MyCity);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    Marker pickupMarker;
    private DatabaseReference assignedRescueePickUpLocationRef;
    private ValueEventListener assignedRescueePickUpLocationRefListener;
    private void getAssignedRescueePickUpLocation(){
        assignedRescueePickUpLocationRef =FirebaseDatabase.getInstance().getReference().child("rescueeRequest").child(rescueeId).child("l");
        assignedRescueePickUpLocationRefListener = assignedRescueePickUpLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && !rescueeId.equals("")){
                    if(dataSnapshot.exists()){
                        List<Object> map = (List<Object>) dataSnapshot.getValue();
                        double locationLat = 0;
                        double locationLng = 0;
                        if(map.get(0) != null){
                            locationLat = Double.parseDouble(map.get(0).toString());
                        }
                        if(map.get(1) != null){
                            locationLng = Double.parseDouble(map.get(1).toString());
                        }
                        pickupLatLng = new LatLng(locationLat,locationLng);
                        pickupMarker = mMap.addMarker(new MarkerOptions().position(pickupLatLng).title("Pick Up Location"));



                        getRouteToMarker(pickupLatLng);

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void getRouteToMarker(LatLng pickupLatLng) {
        if(pickupLatLng != null && mLastLocation != null){

            Routing routing = new Routing.Builder()
                    .travelMode(AbstractRouting.TravelMode.DRIVING)
                    .withListener(this)
                    .alternativeRoutes(false)
                    .waypoints(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), pickupLatLng)
                    .build();
            routing.execute();
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if(android.os.Build.VERSION.SDK_INT>= Build.VERSION_CODES.M){
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            }else{
                checkLocationPermission();
            }
        }
    }

    LocationCallback mLocationCallback = new LocationCallback(){
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for(Location location : locationResult.getLocations()){
                if(getApplicationContext() != null) {

                    mLastLocation = location;
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(11));

                    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference refAvailable = FirebaseDatabase.getInstance().getReference("rescuersAvailable");
                    DatabaseReference refWorking = FirebaseDatabase.getInstance().getReference("rescuersWorking");

                    GeoFire geoFireAvailable = new GeoFire(refAvailable);
                    GeoFire geoFireWorking = new GeoFire(refWorking);


                    switch (rescueeId){
                        case "":
                            geoFireWorking.removeLocation(userId);
                            geoFireAvailable.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()));
                            break;

                        default:
                            geoFireAvailable.removeLocation(userId);
                            geoFireWorking.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()));
                            break;
                    }

                }
            }
        }
    };

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)){
                new AlertDialog.Builder(this)
                        .setTitle("Give Permission")
                        .setMessage("Please give permission to access your location")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(RescuerMapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                            }
                        })
                        .create()
                        .show();
            }
            else{
                ActivityCompat.requestPermissions(RescuerMapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 1:{
                if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                        mMap.setMyLocationEnabled(true);
                    }
                }
                else{
                    Toast.makeText(getApplicationContext(), "Please provide the permission",Toast.LENGTH_SHORT).show();
                }
            }

            break;
        }
    }




    private void connectRescuer(){
        checkLocationPermission();
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
        mMap.setMyLocationEnabled(true);
    }



    private void disconnectDriver(){
        if (mFusedLocationClient != null){
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("rescuersAvailable");

        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(userId);
    }




    @Override
    public void onRoutingFailure(RouteException e) {
        if(e != null) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {
        if(polylines.size()>0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i <route.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);



            final int duration = route.get(i).getDurationValue() / 60;
            mins = String.valueOf(duration)+" mins";

            Toast.makeText(RescuerMapActivity.this,"Duration "+mins,Toast.LENGTH_LONG).show();



        }
    }




    @Override
    public void onRoutingCancelled() {

    }
    private void erasePolylines(){
        for(Polyline line : polylines){
            line.remove();
        }
        polylines.clear();
    }

    public void showNoti(){
        Intent i = new Intent(RescuerMapActivity.this,Alarm.class);
        PendingIntent pi = PendingIntent.getActivity(RescuerMapActivity.this, 0 , i , 0);
        Uri ref = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        String details = mRescueeAddress.getText().toString();

        Notification no = new Notification.Builder(this)
                .setSmallIcon(android.R.drawable.sym_action_email)
                .setContentTitle("Notification")
                .setContentText(details)
                .setAutoCancel(false)
                .setContentIntent(pi)
                .setSound(ref)
                .build();

        ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).notify(0, no);



    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }
}

