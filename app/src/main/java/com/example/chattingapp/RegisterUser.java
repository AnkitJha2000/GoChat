package com.example.chattingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Objects;

import static com.google.android.gms.common.internal.safeparcel.SafeParcelable.NULL;

public class RegisterUser extends AppCompatActivity {
    TextInputLayout fullname,create_email,create_password;
    Button signupbtn,backtosignin;
    ProgressDialog progressDialog;
    FirebaseDatabase rootNode;
    DatabaseReference databaseReference;
    // firebase auth
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);
        // progress bar

        progressDialog = new ProgressDialog(this);

        // firebase auth
        mAuth = FirebaseAuth.getInstance();


        // hooks
        fullname = findViewById(R.id.fullname);
        create_email = findViewById(R.id.create_email);
        create_password = findViewById(R.id.create_password);
        signupbtn = findViewById(R.id.signupbtn);
        backtosignin = findViewById(R.id.backtosignin);


        // function
        backtosignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegisterUser.this,Sign_in.class);
                startActivity(intent);
                finish();
            }
        });


        // signup button
        signupbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!validateName() || !validatePassword() || !validateEmail())
                {
                    return;
                }
                String display_name = Objects.requireNonNull(fullname.getEditText().getText().toString());
                String email = Objects.requireNonNull(create_email.getEditText().getText().toString());
                String password = Objects.requireNonNull(create_password.getEditText().getText().toString());
                progressDialog.setTitle("Registering User");
                progressDialog.setMessage("Please wait while we create your account!");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();
                register_user(display_name,email,password);
            }
        });

    }

    private Boolean validateName() {
        String val = Objects.requireNonNull(fullname.getEditText()).getText().toString();

        if (val.isEmpty()) {
            fullname.setError("Field can't be Empty");
            return false;
        }
        else {
            fullname.setError(NULL);
            fullname.setErrorEnabled(false);
            return true;
        }
    }

    private Boolean validateEmail() {
        String val = Objects.requireNonNull(create_email.getEditText()).getText().toString();
        String noWhite = "^[a-zA-Z0-9_!#$%&’*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";
        if (val.isEmpty()) {
            create_email.setError("Field can't be Empty");
            return false;
        }
        else if(!val.matches(noWhite))
        {
            create_email.setError("Invalid E-Mail Address");
            return false;
        }
        else {
            create_email.setError(NULL);
            create_email.setErrorEnabled(false);
            return true;
        }
    }

    private Boolean validatePassword() {
        String val = Objects.requireNonNull(create_password.getEditText()).getText().toString();
        String noWhites = "((?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%]).{6,20})";
        if (val.isEmpty()) {
            create_password.setError("Field can't be Empty");
            return false;
        }

        else if(!val.matches(noWhites))
        {
            create_password.setError("Password is too weak");
            return false;
        }

        else {
            create_password.setError(NULL);
            create_password.setErrorEnabled(false);
            return true;
        }
    }

    private void register_user(final String display_name, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                            String uid = currentUser.getUid();

                            rootNode = FirebaseDatabase.getInstance();
                            databaseReference =  rootNode.getReference("users");

                            UserHelperClass userHelperClass = new UserHelperClass(display_name,
                                    "Hi there! I am using GO chat.",
                                    "https://firebasestorage.googleapis.com/v0/b/gochat-2ef75.appspot.com/o/profile_images%2Fdefault.jpg?alt=media&token=2abaa849-e75f-47ce-93b4-dd2ecf89c8a8",
                                    "https://firebasestorage.googleapis.com/v0/b/gochat-2ef75.appspot.com/o/profile_images%2Fdefault.jpg?alt=media&token=2abaa849-e75f-47ce-93b4-dd2ecf89c8a8",
                                    uid
                                    );
                            databaseReference.child(uid).setValue(userHelperClass);

                            progressDialog.dismiss();
                            Intent intent = new Intent(RegisterUser.this,MainActivity.class);
                            startActivity(intent);
                            finish();

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("User is not  registered", "createUserWithEmail:failure", task.getException());
                            progressDialog.hide();
                            Toast.makeText(RegisterUser.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}