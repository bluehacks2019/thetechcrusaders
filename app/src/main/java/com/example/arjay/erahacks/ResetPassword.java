package com.example.arjay.erahacks;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPassword extends AppCompatActivity {

    private TextInputLayout mEmail;
    private Button mReset;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        mEmail = findViewById(R.id.email);
        mReset = findViewById(R.id.resetButton);
        mAuth = FirebaseAuth.getInstance();

        mReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = mEmail.getEditText().getText().toString().trim();

                if (email.isEmpty()){
                    mEmail.setError("Empty field.");
                }
                else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    mEmail.setError("Incorrect email address format.");
                }
                else{
                    mEmail.setError(null);
                    if(mEmail != null) {
                        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(ResetPassword.this, "Password reset. Please check your email.", Toast.LENGTH_LONG).show();
                                Intent i = new Intent(ResetPassword.this, MainActivity.class);
                                startActivity(i);
                                finish();
                                return;
                            }
                        });
                    }
                    else{
                        Toast.makeText(ResetPassword.this, "Email not found.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}
