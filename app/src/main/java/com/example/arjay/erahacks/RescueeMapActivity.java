package com.example.arjay.erahacks;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import com.google.android.gms.location.LocationCallback;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import co.ceryle.radiorealbutton.RadioRealButton;
import co.ceryle.radiorealbutton.RadioRealButtonGroup;

public class RescueeMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    Location mLastLocation;
    LocationRequest mLocationRequest;
    private SupportMapFragment mapFragment;


    private FusedLocationProviderClient mFusedLocationClient;

    private Button mLogout,mRequest,mSettings,mHistory, mDescription, mRefButton;
    private EditText mDescBox;
    private LinearLayout mDescLayout;

    private LatLng pickupLocation;

    private String serviceTag;

    private Boolean isLoggingOut = false;

    private Boolean requestBol = false;

    private Marker pickupMarker;

    private String requestService;

    private int notifCounter;

    private LinearLayout mRescuerInfo,mLabelLayout;
    private ImageView mRescuerProfileImage;
    private TextView mRescuerName,mRescuerPhone,mGetServ;


    private RadioRealButtonGroup mRadioGroup;
    private RadioRealButton mPolice,mAmbulance,mFire;
    private String mService;

    private PlaceAutocompleteFragment autocompleteFragment;

    private String City;
    private TextView mDesc;
    private Boolean notified;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rescuee_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mLogout = findViewById(R.id.logout);
        mRequest = findViewById(R.id.request);
        mSettings = findViewById(R.id.settings);
        mHistory = findViewById(R.id.history);
        mDescription = findViewById(R.id.btnDescription);
        mDescBox = findViewById(R.id.descriptionBox);
        mDescLayout = findViewById(R.id.descLayout);
        mDesc = findViewById(R.id.description);
        mGetServ = findViewById(R.id.tvServiceOutput);
        mLabelLayout = findViewById(R.id.labelLayout);
        mDescLayout.bringToFront();
        mLabelLayout.bringToFront();
        mAuth = FirebaseAuth.getInstance();



        autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.getView().setVisibility(View.GONE);





        checkLocationPermission();

        mRadioGroup = findViewById(R.id.radioGroup);
        mPolice = findViewById(R.id.police);
        mAmbulance = findViewById(R.id.ambulance);
        mFire = findViewById(R.id.fire);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mRescuerInfo = findViewById(R.id.rescuerInfo);
        mRescuerProfileImage = findViewById(R.id.rescuerPofileImage);
        mRescuerName = findViewById(R.id.rescuerName);
        mRescuerPhone = findViewById(R.id.rescuerPhone);
        mRefButton = findViewById(R.id.refreshButton);

        notified = false;

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
        },0,5000);


        if (!notified){
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            sendNotification();
                            return;


                        }
                    });
                    if (notified){
                        cancel();
                    }
                }
            },0,5000);
        }





        mSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RescueeMapActivity.this, RescueeSettingsActivity.class);
                startActivity(intent);
                return;
            }
        });

        mRefButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(ContextCompat.checkSelfPermission(RescueeMapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                    mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                    mMap.setMyLocationEnabled(true);
                }
                mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                mMap.setMyLocationEnabled(true);
            }
        });


        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isLoggingOut = true;
                disconnectDriver();
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(RescueeMapActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return;
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

        mRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    autocompleteFragment.setText("");


                    if (pickupMarker != null){
                        pickupMarker.remove();
                    }
                    if (requestBol) {
                        endRescue();
                        mDescLayout.setVisibility(View.GONE);
                    } else {

                        mService = mPolice.getText().toString();

                        if(mPolice.isChecked())
                        {
                            mService = mPolice.getText().toString();
                        }
                        if(mAmbulance.isChecked())
                        {
                            mService = mAmbulance.getText().toString();
                        }
                        if(mFire.isChecked())
                        {
                            mService = mFire.getText().toString();
                        }

                        if (mService == null){
                            return;
                        }
                        if (pickupLocation == null) {
                            notifCounter = 1;


                            requestService = mService;
                            requestBol = true;
                            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                            final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("rescueeRequest");
                            GeoFire geoFire = new GeoFire(ref);
                            geoFire.setLocation(userId, new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()));

                            pickupLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());

                            Geocoder geocoder = new Geocoder(RescueeMapActivity.this, Locale.getDefault());
                            try {
                                List<Address> addressess = geocoder.getFromLocation(pickupLocation.latitude, pickupLocation.longitude, 1);
                                City = addressess.get(0).getLocality();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }




                            mDescLayout.setVisibility(View.VISIBLE);

                            pickupMarker = mMap.addMarker(new MarkerOptions().position(pickupLocation).title("Pick Up Here"));

                            mRequest.setText("Getting your Rescuer..");
                            getClosestRescuer();




                        }
                    }
            }
        });


        mHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RescueeMapActivity.this, HistoryActivity.class);
                intent.putExtra("rescueeOrRescuer", "Rescuee");
                startActivity(intent);
                return;
            }
        });





        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.

                if (pickupMarker != null){
                    pickupMarker.remove();
                }

                if(mPolice.isChecked())
                {
                    mService = mPolice.getText().toString();
                }
                if(mAmbulance.isChecked())
                {
                    mService = mAmbulance.getText().toString();
                }
                if(mFire.isChecked())
                {
                    mService = mFire.getText().toString();
                }


                if (mService == null){
                    Toast.makeText(RescueeMapActivity.this,"Please select a service",Toast.LENGTH_LONG).show();
                    return;
                }


                requestService = mService;
                requestBol = true;
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("rescueeRequest");
                GeoFire geoFire = new GeoFire(ref);
                geoFire.setLocation(userId, new GeoLocation(place.getLatLng().latitude, place.getLatLng().longitude));


                pickupLocation = new LatLng(place.getLatLng().latitude, place.getLatLng().longitude);

                Geocoder geocoder = new Geocoder(RescueeMapActivity.this, Locale.getDefault());
                try {
                    List<Address> addressess = geocoder.getFromLocation(place.getLatLng().latitude, place.getLatLng().longitude, 1);
                    City = addressess.get(0).getLocality();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (City != null){
                    if (City.equalsIgnoreCase("malabon")) {
                        mDescLayout.setVisibility(View.VISIBLE);
                        pickupMarker = mMap.addMarker(new MarkerOptions().position(pickupLocation).title("Pick Up Here"));

                        mRequest.setText("Getting your Rescuer..");
                        getClosestRescuer();
                    }
                    else{
                        endRescue();
                        Toast.makeText(RescueeMapActivity.this, "These app is exclusive for Malabon City only", Toast.LENGTH_LONG).show();
                        autocompleteFragment.setText("");
                    }
                }

            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
            }
        });

        label();
    }

    private void label(){
        if (FirebaseAuth.getInstance().getCurrentUser().getUid() != null){
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users").child("Rescuee").child(userId);
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if (dataSnapshot.exists() && dataSnapshot.getChildrenCount()> 0){
                        if (map.get("name")!=null){
                            String serviceLabel;
                            serviceLabel =map.get("name").toString();
                            mGetServ.setText(serviceLabel);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }


    }




    private int radius = 1;
    private Boolean rescuerFound = false;
    private String rescuerFoundID;
    private GeoQuery geoQuery;

    private void getClosestRescuer(){
        DatabaseReference rescuerLocation = FirebaseDatabase.getInstance().getReference().child("rescuersAvailable");

        if (pickupLocation != null){
            GeoFire geoFire = new GeoFire(rescuerLocation);
            geoQuery = geoFire.queryAtLocation(new GeoLocation(pickupLocation.latitude, pickupLocation.longitude), radius);
            geoQuery.removeAllListeners();
            geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                @Override
                public void onKeyEntered(String key, GeoLocation location) {
                    if (!rescuerFound && requestBol) {
                        DatabaseReference mRescueeDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Rescuer").child(key);
                        mRescueeDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                                    Map<String, Object> rescuerMap = (Map<String, Object>)dataSnapshot.getValue();
                                    if(rescuerFound){
                                        return;
                                    }

                                    if(rescuerMap.get("service").equals(requestService)){
                                        rescuerFound = true;
                                        rescuerFoundID = dataSnapshot.getKey();

                                        DatabaseReference rescuerRef =FirebaseDatabase.getInstance().getReference().child("Users").child("Rescuer").child(rescuerFoundID).child("rescueeRequest");
                                        String rescueeId = FirebaseAuth.getInstance().getCurrentUser().getUid();


                                        HashMap map = new HashMap();
                                        map.put("rescueeRideId", rescueeId);
                                        rescuerRef.updateChildren(map);


                                        mRequest.setEnabled(false);
                                        getDriverLocation();
                                        getRescuerInfo();
                                        getHasRescueEnded();
                                        mRequest.setText("Looking for Rescuer Location..");

                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                }

                @Override
                public void onKeyExited(String key) {

                }

                @Override
                public void onKeyMoved(String key, GeoLocation location) {

                }

                @Override
                public void onGeoQueryReady() {
                    if (!rescuerFound){
                        radius++;
                        getClosestRescuer();
                    }
                }

                @Override
                public void onGeoQueryError(DatabaseError error) {

                }
            });
        }

        else{
            Toast.makeText(this, "No online rescuers. Please try again later.", Toast.LENGTH_SHORT).show();
        }


    }

    private void sendNotification(){
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference sendNotif = FirebaseDatabase.getInstance().getReference().child("rescueeRequest").child(userId);
        sendNotif.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();



                    if (map.get("arrivalUpdate") != null){
                        Intent i = new Intent(RescueeMapActivity.this, DistanceNotifier.class);
                        startActivity(i);
                        notified = true;
                    }


                }
                else{
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }



    private Marker mRescuerMarker;
    private DatabaseReference rescuerLocationRef;
    private ValueEventListener rescuerLocationRefListener;

    private void getDriverLocation(){
        rescuerLocationRef =FirebaseDatabase.getInstance().getReference().child("rescuersWorking").child(rescuerFoundID).child("l");
        rescuerLocationRefListener = rescuerLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && requestBol){
                    List<Object> map = (List<Object>)dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;
                    if(map.get(0) != null){
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }
                    if(map.get(1) != null){
                        locationLng = Double.parseDouble(map.get(1).toString());
                    }
                    LatLng rescuerLatLng = new LatLng(locationLat,locationLng);
                    if(mRescuerMarker != null){
                        mRescuerMarker.remove();
                    }
                    Location loc1 = new Location("");
                    loc1.setLatitude(pickupLocation.latitude);
                    loc1.setLongitude(pickupLocation.longitude);

                    Location loc2 = new Location("");
                    loc2.setLatitude(rescuerLatLng.latitude);
                    loc2.setLongitude(rescuerLatLng.longitude);

                    float distance = loc1.distanceTo(loc2);

                    if (distance<100){
                        mRequest.setText("Rescuer's Here");


                    }else{
                        distance = distance/1000;


                        int intDistance = Math.round(distance);

                        mRequest.setText("Rescuer found please stay calm: "+String.valueOf(intDistance)+" km ");
                    }
                    mRescuerMarker = mMap.addMarker(new MarkerOptions().position(rescuerLatLng).title("Your Rescuer"));
                    mRescuerMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.icon_ambulance_free));

                    if(mPolice.isChecked())
                    {
                        mRescuerMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.policecar));
                    }
                    if(mAmbulance.isChecked())
                    {
                        mRescuerMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.icon_ambulance_free));
                    }
                    if(mFire.isChecked())
                    {
                        mRescuerMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.firefighter));
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getRescuerInfo(){
        mRescuerInfo.setVisibility(View.VISIBLE);
        DatabaseReference mRescueeDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Rescuer").child(rescuerFoundID);
        mRescueeDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    Map<String, Object> map = (Map<String, Object>)dataSnapshot.getValue();
                    if(map.get("name") != null){
                        mRescuerName.setText(map.get("name").toString());
                    }
                    if(map.get("phone") != null){
                        mRescuerPhone.setText(map.get("phone").toString());
                    }
                    if(map.get("profileImageUrl") != null){
                        Glide.with(getApplication()).load(map.get("profileImageUrl").toString()).into(mRescuerProfileImage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private DatabaseReference rescueHasEndedRef;
    private ValueEventListener rescueHasEndedRefListener;
    private void getHasRescueEnded(){
        rescueHasEndedRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Rescuer").child(rescuerFoundID).child("rescueeRequest").child("rescueeRideId");
        rescueHasEndedRefListener = rescueHasEndedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){

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
        notified = false;
        mRequest.setEnabled(true);
        pickupLocation = null;
        requestBol = false;
        mDescLayout.setVisibility(View.GONE);
        autocompleteFragment.setText("");


        if(rescuerLocationRefListener!= null && rescueHasEndedRefListener!= null) {
            rescuerLocationRef.removeEventListener(rescuerLocationRefListener);
            rescueHasEndedRef.removeEventListener(rescueHasEndedRefListener);
        }
        if(geoQuery != null){
            geoQuery.removeAllListeners();
        }

        if(rescuerFoundID != null){
            DatabaseReference rescuerRef =FirebaseDatabase.getInstance().getReference().child("Users").child("Rescuer").child(rescuerFoundID).child("rescueeRequest");
            rescuerRef.removeValue();

            rescuerFoundID = null;
        }
        rescuerFound = false;
        radius = 1;

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("rescueeRequest");
        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(userId);

        if(pickupMarker != null){
            pickupMarker.remove();
        }
        if(mRescuerMarker != null){
            mRescuerMarker.remove();
        }
        mRequest.setText("CALL EMERGENCY");
        mRescuerInfo.setVisibility(View.GONE);
        mRescuerName.setText("");
        mRescuerPhone.setText("");
        mRescuerProfileImage.setImageResource(R.drawable.defaultpic);



    }

    private void getRescueeDescription(){

        if (FirebaseAuth.getInstance().getCurrentUser().getUid() != null){
            String userId =FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference descRef = FirebaseDatabase.getInstance().getReference().child("rescueeRequest").child(userId);
            descRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0) {
                        Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                        if (map.get("rescuerMessage") != null) {
                            mDesc.setText(map.get("rescuerMessage").toString());
                        }
                        else{
                            mDesc.setText("No Message");
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
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference descRef = FirebaseDatabase.getInstance().getReference().child("rescueeRequest").child(userId);

        if(desc.equals("")){
            desc = "no description yet";
            HashMap map = new HashMap();
            map.put("description", desc);
            descRef.updateChildren(map);
        }

        else{
            HashMap map = new HashMap();
            map.put("description", desc);
            descRef.updateChildren(map);
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){

            }
            else{
                checkLocationPermission();
            }
        }
    }



    LocationCallback mLocationCallback = new LocationCallback(){
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                if (getApplicationContext() != null) {
                    mLastLocation = location;

                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(11));

                    if(!getRescuersAroundStarted){
                        getRescuersAround();
                    }

                }
            }
        }
    };

    private void checkLocationPermission() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)){
                new AlertDialog.Builder(this)
                        .setTitle("Give Permission")
                        .setMessage("Please give permission to access your location")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(RescueeMapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                            }
                        })
                        .create()
                        .show();
            }
            else{
                ActivityCompat.requestPermissions(RescueeMapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
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


    public void disconnectDriver() {
        if(mFusedLocationClient != null){
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("rescuersAvailable");
        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(userId);
    }





    boolean getRescuersAroundStarted = false;
    List<Marker> markers = new ArrayList<Marker>();


    private void getRescuersAround() {


        getRescuersAroundStarted = true;
        DatabaseReference rescuersLocation = FirebaseDatabase.getInstance().getReference().child("rescuersAvailable");

        final GeoFire geoFire = new GeoFire(rescuersLocation);
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()), 100000);

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                for (Marker markerIt : markers) {
                    if (markerIt.getTag() != null) {
                        if (markerIt.getTag().equals(key)) {
                        }
                    }
                }

                LatLng rescuersLocation = new LatLng(location.latitude, location.longitude);

                Marker mRescuerMarker = mMap.addMarker(new MarkerOptions().position(rescuersLocation).title(key));
                mRescuerMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.siren));
                mRescuerMarker.setTag(key);
                markers.add(mRescuerMarker);
            }

            @Override
            public void onKeyExited(String key) {
                for (Marker markerIt : markers) {

                    if (markerIt.getTag() != null) {
                        if (markerIt.getTag().equals(key)) {
                            markerIt.remove();
                        }
                    }
                }
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                for (Marker markerIt : markers) {
                    if (markerIt.getTag() != null) {
                        if (markerIt.getTag().equals(key)) {
                            markerIt.setPosition(new LatLng(location.latitude, location.longitude));
                        }
                    }

                }

            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(firebaseAuthListener);
    }



}
