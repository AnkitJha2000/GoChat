package com.example.chattingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

import static com.google.android.gms.common.internal.safeparcel.SafeParcelable.NULL;


public class Sign_in extends AppCompatActivity {
    Button signin,backtosignup;
    TextInputLayout Useremail,Userpassword;
    private FirebaseAuth mAuth;
    private ProgressDialog mprogressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        // progreass dialog

        mprogressDialog = new ProgressDialog(this);

        // firebase

        mAuth = FirebaseAuth.getInstance();

        // hooks
        signin = findViewById(R.id.signin);
        backtosignup = findViewById(R.id.backtosignup);
        Useremail = findViewById(R.id.email);
        Userpassword = findViewById(R.id.password);

        // back to sign up
        backtosignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Sign_in.this,RegisterUser.class);
                startActivity(intent);
                finish();
            }
        });

        // sign in activity
        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!validatePassword() || !validateEmail())
                    return;

                mprogressDialog.setTitle("Login User");
                mprogressDialog.setMessage("User is being signed in!");
                mprogressDialog.setCanceledOnTouchOutside(false);
                mprogressDialog.show();

                String emailEntered = Objects.requireNonNull(Useremail.getEditText().getText().toString());
                String passwordEntered = Objects.requireNonNull(Userpassword.getEditText().getText().toString());

                loginUser(emailEntered,passwordEntered);

            }
        });


    }

    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("user is signed in", "signInWithEmail:success");
                            //FirebaseUser user = mAuth.getCurrentUser();

                            mprogressDialog.dismiss();

                            Intent intent = new Intent(Sign_in.this,MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("User not sign in", "signInWithEmail:failure", task.getException());
                            mprogressDialog.hide();
                            Toast.makeText(Sign_in.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });

    }

    private boolean validateEmail() {
        String val = Objects.requireNonNull(Objects.requireNonNull(Useremail.getEditText()).getText().toString());
        if(val.isEmpty())
        {
            Useremail.setError("Field can't be empty");
            return false;
        }
        else
        {
            Useremail.setError(NULL);
            Useremail.setErrorEnabled(false);
            return true;
        }
    }
    private boolean validatePassword() {
        String val = Objects.requireNonNull(Objects.requireNonNull(Userpassword.getEditText()).getText().toString());
        if(val.isEmpty())
        {
            Userpassword.setError("Field can't be empty");
            return false;
        }
        else
        {
            Userpassword.setError(NULL);
            Userpassword.setErrorEnabled(false);
            return true;
        }
    }
}