package com.example.arjay.erahacks;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class HistorySingleActivity extends AppCompatActivity implements OnMapReadyCallback,RoutingListener{

    private String rideId, currentUserId, customerId, driverId, userDriverOrCustomer;

    private TextView rideDate,userName,userPhone,description,pickupAddress,mRescueename,mRescueePhone, timeDifference, tvRequestTimedate, mRescuerReport;

    private ImageView userImage,descriptionPic,mRescueePic;

    private DatabaseReference historyRideInfoDb;

    private LatLng pickupLatLng;

    private String mDescImageUrl;
    private FirebaseAuth mAuth;
    private DatabaseReference mHistoryDatabase;
    private Uri resultUri;
    private String userID;

    private Button mSave;
    private EditText descEdit;
    private String timeOfRequest, timeOfArrival;




    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_history_single);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);




        mMapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);

        rideId = getIntent().getExtras().getString("rideId");

        rideDate = (TextView)findViewById(R.id.rideDate);
        userName = (TextView)findViewById(R.id.username);
        userPhone = (TextView)findViewById(R.id.userphone);
        descEdit = (EditText) findViewById(R.id.desc);
        mRescueename = findViewById(R.id.rescueeName);
        mRescueePhone = findViewById(R.id.phoneOfRescuee);
        mRescueePic = findViewById(R.id.rescueePic);
        timeDifference = findViewById(R.id.timeDifference);
        tvRequestTimedate = findViewById(R.id.tvRequestTimeDate);
        mRescuerReport = findViewById(R.id.rescuerReport);

        pickupAddress = (TextView)findViewById(R.id.pickupAddress);
        descriptionPic = (ImageView)findViewById(R.id.descriptionPic);

        userImage = (ImageView)findViewById(R.id.userImage);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        mHistoryDatabase = FirebaseDatabase.getInstance().getReference().child("history").child(rideId);
        mSave = (Button)findViewById(R.id.save);

        historyRideInfoDb = FirebaseDatabase.getInstance().getReference().child("history").child(rideId);
        getRideInformation();

        getDescInfo();

        descriptionPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 1);
            }
        });

        mSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveUserInformation();

            }
        });

        getUserInfo();




    }

    private void getRideInformation() {
        historyRideInfoDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot child : dataSnapshot.getChildren()){
                        if(child.getKey().equals("customer")){
                            customerId = child.getValue().toString();
                            if(!customerId.equals(currentUserId)){
                                userDriverOrCustomer = "Rescuer";
                                getUserInformation("Rescuee", customerId);
                            }
                        }
                        if(child.getKey().equals("driver")){
                            driverId = child.getValue().toString();

                            if(!driverId.equals(currentUserId)){
                                userDriverOrCustomer = "Rescuee";
                                getUserInformation("Rescuer", driverId);
                            }
                        }
                        if(child.getKey().equals("timestamp")){
                            rideDate.setText("Time of arrival: "+getDate(Long.valueOf(child.getValue().toString())));
                            timeOfArrival = child.getValue().toString();
                        }
                        if(child.getKey().equals("description")){
                            descEdit.setText(child.getValue().toString());
                        }
                        if(child.getKey().equals("address")){
                            pickupAddress.setText(child.getValue().toString());
                        }
                        if(child.getKey().equals("location")){

                            pickupLatLng = new LatLng(Double.valueOf(child.child("from").child("lat").getValue().toString()),Double.valueOf(child.child("from").child("lng").getValue().toString()));
                            LatLngBounds.Builder builder = new LatLngBounds.Builder();
                            builder.include(pickupLatLng);
                            LatLngBounds bounds = builder.build();
                            int width = getResources().getDisplayMetrics().widthPixels;
                            int height = getResources().getDisplayMetrics().heightPixels;
                            int padding = (int) (width*0.12);

                            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);
                            mMap.animateCamera(cameraUpdate);
                            mMap.addMarker(new MarkerOptions().position(pickupLatLng).title("pick up location"));

                        }
                        if (child.getKey().equals("Time of request")){
                            timeOfRequest = child.getValue().toString();
                            tvRequestTimedate.setText("Time of request: "+getDate(Long.valueOf(child.getValue().toString())));
                        }
                        if (child.getKey().equals("Rescuer's report")){
                            mRescuerReport.setText(child.getValue().toString());
                        }

                    }

                    getTimeDifference();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getUserInformation(String otherUserRescuerOrRescuee, String otherUserId) {
        DatabaseReference mOtherUserDB = FirebaseDatabase.getInstance().getReference().child("Users").child(otherUserRescuerOrRescuee).child(otherUserId);
        mOtherUserDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if(map.get("name") != null){
                        userName.setText(map.get("name").toString());
                    }
                    if(map.get("phone") != null){
                        userPhone.setText(map.get("phone").toString());
                    }
                    if(map.get("profileImageUrl") != null){
                        Glide.with(getApplication()).load(map.get("profileImageUrl").toString()).into(userImage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getDescInfo(){
        mHistoryDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    Map<String, Object> map = (Map<String, Object>)dataSnapshot.getValue();

                    if(map.get("descriptionPic") != null){
                        mDescImageUrl = map.get("descriptionPic").toString();
                        Glide.with(getApplication()).load(mDescImageUrl).into(descriptionPic);
                    }
                    if(map.get("descriptionPic").equals("none")){
                        descriptionPic.setBackgroundResource(R.drawable.upload_pic);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getTimeDifference(){
        Integer request = Integer.parseInt(timeOfRequest);
        Integer arrival = Integer.parseInt(timeOfArrival);
        Integer result = arrival - request;

        Integer fResult = result / 60;

        timeDifference.setText(timeDifference.getText().toString()+fResult+" minutes");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("history").child(rideId);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    map.put("response", timeDifference.getText().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void getUserInfo(){
        String userid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users").child("Rescuee").child(userid);

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    Map<String,Object> map = (Map<String,Object>)dataSnapshot.getValue();

                    if(map.get("name") != null){
                        mRescueename.setText(map.get("name").toString());
                    }
                    if(map.get("phone") != null){
                        mRescueePhone.setText(map.get("phone").toString());
                    }
                    if(map.get("profileImageUrl") != null){
                        String url = map.get("profileImageUrl").toString();
                        Glide.with(getApplication()).load(url).into(mRescueePic);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }



    private void saveUserInformation() {

        final DatabaseReference desc = FirebaseDatabase.getInstance().getReference().child("history").child(rideId);
        desc.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Map map = new HashMap();
                String updateDesc = descEdit.getText().toString();
                String response = timeDifference.getText().toString();
                if (updateDesc.isEmpty()){
                    Toast.makeText(HistorySingleActivity.this,"Please fill out empty field",Toast.LENGTH_LONG).show();
                }
                else{
                    map.put("description",updateDesc);
                    map.put("response",response);
                    desc.updateChildren(map);
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if(resultUri != null){
            final StorageReference filePath = FirebaseStorage.getInstance().getReference().child("history").child(rideId);
            Bitmap bitmap = null;

            try {
                bitmap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(), resultUri);
            } catch (IOException e) {
                e.printStackTrace();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);

            byte[] data = baos.toByteArray();
            final UploadTask uploadTask = filePath.putBytes(data);

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Map newImage = new HashMap();
                            newImage.put("descriptionPic", uri.toString());
                            mHistoryDatabase.updateChildren(newImage);

                            Toast.makeText(HistorySingleActivity.this,"Case Filed",Toast.LENGTH_LONG).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            finish();
                            return;
                        }
                    });
                }
            });
        }else {
            finish();
            return;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == Activity.RESULT_OK){
            final Uri imageUri = data.getData();
            resultUri = imageUri;

            descriptionPic.setImageURI(resultUri);
        }
    }

    private String getDate(Long timestamp){
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp*1000);
        String date = DateFormat.format("dd-MM-yyyy hh:mm", cal).toString();


        return date;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    @Override
    public void onRoutingFailure(RouteException e) {

    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> arrayList, int i) {



    }

    @Override
    public void onBackPressed() {
        finish();
        return;
    }

    @Override
    public void onRoutingCancelled() {

    }
}
