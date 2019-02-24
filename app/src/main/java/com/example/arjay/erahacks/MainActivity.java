package com.example.arjay.erahacks;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private Button mRescuer,mRescuee,mbtnAgreement;
    private LinearLayout mAgreementLayout;
    private TextView mtvAgreement;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);

        mRescuer = (Button)findViewById(R.id.rescuer);
        mRescuee = (Button)findViewById(R.id.rescuee);
        mbtnAgreement = findViewById(R.id.btnAgreement);
        mAgreementLayout = findViewById(R.id.agreementLayout);
        mtvAgreement = findViewById(R.id.tvAgreement);

        mtvAgreement.setText("Application's Policy"+"\n"+"\n"+"THIS APPLICATION IS NOT DEVELOPED FOR FUN. USERS WHO ARE CONVICTED IN USING THIS APPLICATION FOR JOKE PURPOSE WILL RECEIVE A SERIOUS PUNISHMENT FROM THE GOVERNMENT OF THE PHILIPPINES.");

        startService(new Intent(MainActivity.this, onAppKilled.class));

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        mbtnAgreement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAgreementLayout.setVisibility(View.GONE);
            }
        });




        mRescuer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.setMessage("Please wait.");
                progressDialog.show();
                Intent intent = new Intent(MainActivity.this, RescuerLoginActivity.class);
                startActivity(intent);
                progressDialog.dismiss();
                finish();
                return;
            }
        });

        mRescuee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.setMessage("Please wait.");
                progressDialog.show();
                Intent intent = new Intent(MainActivity.this, RescueeLoginActivity.class);
                startActivity(intent);
                progressDialog.dismiss();
                finish();
                return;
            }
        });

    }
}
