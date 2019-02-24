package com.example.arjay.erahacks;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RescueeLoginActivity extends AppCompatActivity {

    private EditText mEmail,mPassword;
    private Button mLogin,mRegistration;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;


    private TextView mReg,mForgot;
    private Boolean emailAddChecker;

    ProgressDialog progressDialog;
    private Integer counter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_rescuee_login);

        counter = 0;
        mForgot = findViewById(R.id.forgotPassword);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        mAuth = FirebaseAuth.getInstance();

        mReg = findViewById(R.id.reg);

        mReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(RescueeLoginActivity.this, RescueeRegistration.class);
                startActivity(i);
                finish();
                return;
            }
        });
        mForgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(RescueeLoginActivity.this,ResetPassword.class);
                startActivity(i);
                finish();
                return;
            }
        });

        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if(user!=null && user.isEmailVerified()){
                    progressDialog.setMessage("Logging in. Please wait.");
                    progressDialog.show();
                    String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users").child("Rescuee").child(userID);
                    ref.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                                Intent intent = new Intent(RescueeLoginActivity.this, RescueeMapActivity.class);
                                startActivity(intent);
                                finish();
                                return;
                            } else {
                                progressDialog.dismiss();
                                FirebaseAuth.getInstance().signOut();
                                return;
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }
        };


        mEmail = (EditText)findViewById(R.id.email);
        mPassword = (EditText)findViewById(R.id.password);
        mLogin = (Button)findViewById(R.id.login);


        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.setMessage("Logging in. Please wait.");
                progressDialog.show();
                final String email = mEmail.getText().toString();
                final String password = mPassword.getText().toString();

                if (email.isEmpty() && password.isEmpty()){
                    Toast.makeText(RescueeLoginActivity.this,"Please provide log-in credentials", Toast.LENGTH_LONG).show();
                    progressDialog.dismiss();
                }
                else {

                    mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(RescueeLoginActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(!task.isSuccessful()){
                                Toast.makeText(RescueeLoginActivity.this, "Sign in error.", Toast.LENGTH_SHORT).show();
                                counter++;
                                if (counter > 3){
                                    mForgot.setVisibility(View.VISIBLE);
                                }
                                progressDialog.dismiss();
                            }
                            else{
                                verifyEmailAddress();
                            }
                        }
                    });

                }

            }
        });
    }

    private void verifyEmailAddress(){
        FirebaseUser user = mAuth.getCurrentUser();
        emailAddChecker = user.isEmailVerified();

        if (emailAddChecker){
            String userID = mAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users").child("Rescuee").child(userID);
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                        Intent intent = new Intent(RescueeLoginActivity.this, RescueeMapActivity.class);
                        progressDialog.dismiss();
                        startActivity(intent);
                        finish();
                        return;
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(RescueeLoginActivity.this,"User not found.",Toast.LENGTH_LONG).show();
                        return;
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        else{
            mAuth.signOut();
            Toast.makeText(RescueeLoginActivity.this,"Please verify your email address",Toast.LENGTH_LONG).show();
            Intent i = new Intent(RescueeLoginActivity.this, RescueeLoginActivity.class);
            startActivity(i);
            finish();
            return;

        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(firebaseAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(firebaseAuthListener);
    }
}
