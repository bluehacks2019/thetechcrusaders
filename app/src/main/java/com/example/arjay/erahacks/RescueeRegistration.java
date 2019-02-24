package com.example.arjay.erahacks;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RescueeRegistration extends AppCompatActivity {

    private Button mRegister, mGetPhoneVerification, mVerifyPhone;
    private TextView mBack;

    private ProgressDialog progressDialog;

    private TextInputLayout mEmail,mPassword,mName,mPhone, mVerificationCode;

    private FirebaseAuth mAuth;
    private String codeSent;
    private boolean codeVerified;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_rescuee_registration);

        codeVerified = false;

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);
        mName = findViewById(R.id.name);
        mPhone = findViewById(R.id.phone);
        mRegister = findViewById(R.id.regButton);
        mBack = findViewById(R.id.backButton);
        mGetPhoneVerification = findViewById(R.id.btnVerifyPhone);
        mVerificationCode = findViewById(R.id.phoneVerificationCode);
        mVerifyPhone = findViewById(R.id.verifyPhone);

        mAuth = FirebaseAuth.getInstance();

        FirebaseAuth.getInstance().signOut();

        mGetPhoneVerification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.setMessage("Sending vefification code.");
                progressDialog.show();
                sendVerificationCode();
            }
        });

        mVerifyPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.setMessage("Verifying phone number.");
                progressDialog.show();
                verifySignInCode();
            }
        });


        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent i = new Intent(RescueeRegistration.this, RescueeLoginActivity.class);
                startActivity(i);
                finish();
                return;
            }
        });

        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                progressDialog.setMessage("Registering user..");
                progressDialog.show();

                if (!validateEmail() | !validatePassword() | !validateName() | !validatePhone() | !codeVerified){
                    progressDialog.dismiss();
                    Toast.makeText(RescueeRegistration.this, "Sign up error.",Toast.LENGTH_SHORT).show();
                }
                else{
                    String email = mEmail.getEditText().getText().toString();
                    String password = mPassword.getEditText().getText().toString();

                    mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(RescueeRegistration.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if(task.isSuccessful()){
                                String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                DatabaseReference regRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Rescuee").child(userID);
                                String name = mName.getEditText().getText().toString();
                                String phone = mPhone.getEditText().getText().toString();
                                String email = mEmail.getEditText().getText().toString();

                                Map map = new HashMap();

                                map.put("name",name);
                                map.put("phone",phone);
                                map.put("email",email);
                                regRef.updateChildren(map);
                                mEmail.getEditText().setText("");
                                mPassword.getEditText().setText("");
                                mName.getEditText().setText("");
                                mPhone.getEditText().setText("");
                                mVerificationCode.getEditText().setText("");
                                mVerificationCode.getEditText().setEnabled(true);
                                sendEmailVerification();
                                progressDialog.dismiss();
                                codeVerified = false;
                            }
                            else {
                                mEmail.setError("Email is already in use");
                                progressDialog.dismiss();

                            }
                        }
                    });
                }

            }
        });


    }

    private void verifySignInCode() {
        String code = mVerificationCode.getEditText().getText().toString();
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(codeSent,code);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential){
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Toast.makeText(RescueeRegistration.this, "Phone Verified!", Toast.LENGTH_SHORT).show();
                    mVerificationCode.getEditText().setEnabled(false);
                    mPhone.getEditText().setEnabled(false);
                    codeVerified = true;
                    progressDialog.dismiss();
                }
                else{
                    Toast.makeText(RescueeRegistration.this, "Invalid Verification Code", Toast.LENGTH_SHORT).show();
                    codeVerified = false;
                    progressDialog.dismiss();
                }
            }
        });
    }


    private void sendVerificationCode() {
        if (validatePhone()){
            String phone = mPhone.getEditText().getText().toString();
            PhoneAuthProvider.getInstance().verifyPhoneNumber(phone,60, TimeUnit.SECONDS,RescueeRegistration.this,mCallbacks);
        }
        else{
            Toast.makeText(this, "Please enter a valid phone number.", Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
            return;
        }
    }

    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

        }

        @Override
        public void onVerificationFailed(FirebaseException e) {

        }

        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);

            codeSent = s;
            progressDialog.setMessage("Code sent!");
            progressDialog.dismiss();
        }
    };


    private void sendEmailVerification(){
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null){
            user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    if(task.isSuccessful()){
                        Toast.makeText(RescueeRegistration.this, "Registration successful, please check and verify your email address.",Toast.LENGTH_LONG).show();
                        mAuth.signOut();
                    }
                    else {
                        String error = task.getException().getMessage();
                        Toast.makeText(RescueeRegistration.this, "Error: "+error, Toast.LENGTH_SHORT).show();
                    }

                }
            });
        }
    }

    private boolean validateEmail(){
        String email = mEmail.getEditText().getText().toString().trim();

        if (email.isEmpty()){
            mEmail.setError("Empty field.");
            return false;
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            mEmail.setError("Incorrect email address format.");
            return false;
        }
        else{
            mEmail.setError(null);
            return true;
        }
    }

    private boolean validatePassword(){
        String password = mPassword.getEditText().getText().toString().trim();
        Matcher passwordCharMatcher = specialCharandSpace.matcher(password);
        Matcher passwordUpperCase = hasUpperCase.matcher(password);

        if (password.isEmpty()){
            mPassword.setError("Empty field.");
            return false;
        }
        else if (!passwordCharMatcher.matches()){
            mPassword.setError("Password contains special characters.");
            return false;
        }
        else if (password.length() <= 7){
            mPassword.setError("Password must contain atleast 8 characters.");
            return false;
        }
        else if (passwordUpperCase.matches()){
            mPassword.setError("Uppercase is not allowed.");
            return false;
        }
        else{
            mPassword.setError(null);
            return true;
        }
    }

    private Boolean validateName(){
        String name = mName.getEditText().getText().toString();

        if(name.isEmpty()){
            mName.setError("Empty field.");
            return false;
        }
        else if (hasSpecialCharacter.matcher(name).find()){
            mName.setError("Name contains special characters.");
            return false;
        }
        else if (name.length() <=7 ){
            mName.setError("Name must contain atleast 8 characters");
            return false;
        }
        else {
            mName.setError(null);
            return true;
        }
    }

    private  Boolean validatePhone(){
        String phone = mPhone.getEditText().getText().toString();

        if (phone.isEmpty()){
            mPhone.setError("Field is empty.");
            return false;
        }
        else if (phone.length() != 13){
            mPhone.setError("Invalid phone number format.");
            return false;
        }
        else if (hasSpecialCharacter.matcher(phone).find()){
            mPhone.setError("Phone must not contain special characters.");
            return false;
        }
        else if (phone.contains(" ")){
            mPhone.setError("Spaces not allowed.");
            return false;
        }
        else{
            mPhone.setError(null);
            return true;
        }
    }
    public static final Pattern EMAIL_ADDRESS = Pattern.compile("\"^[_A-Za-z0-9-]+(\\\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\\\.[A-Za-z0-9]+)*(\\\\.[A-Za-z]{2,})$\"");
    public static final Pattern specialCharandSpace = Pattern.compile("[a-zA-z0-9]*");
    public static final Pattern hasUpperCase = Pattern.compile("(.*[A-Z].*)");
    public static final Pattern hasSpecialCharacter = Pattern.compile("[$&,:;=\\\\?@#|/'<>^*()%!-]");

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseAuth.getInstance().signOut();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        Intent i = new Intent(RescueeRegistration.this, RescueeLoginActivity.class);
        startActivity(i);

    }
}
