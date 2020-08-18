package com.example.chattingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

import static com.google.android.gms.common.internal.safeparcel.SafeParcelable.NULL;

public class ChangeStatusActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    Button statusUpdatebtn,statusUpdateCancel;
    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    final String uid = currentUser.getUid();
    private FirebaseAuth mAuth;
    private DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    TextInputLayout statusUpdate;

    @Override
    public void onStart() {
        super.onStart();
        mAuth = FirebaseAuth.getInstance();
        // String uid = mAuth.getCurrentUser().getUid();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null)
        {
            mRootRef.child("users").child(currentUser.getUid()).child("online").setValue("true");
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth = FirebaseAuth.getInstance();
        // String uid = mAuth.getCurrentUser().getUid();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            mRootRef.child("users").child(currentUser.getUid()).child("online").setValue(ServerValue.TIMESTAMP);
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_status);

        // app bar
        mToolbar = findViewById(R.id.app_bar_status);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account Status Update");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // hooks
        statusUpdate = findViewById(R.id.statusUpdate);
        statusUpdatebtn = findViewById(R.id.statusUpdatebtn);
        statusUpdateCancel = findViewById(R.id.statusUpdateCancel);

        // uid
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        final String uid = currentUser.getUid();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(uid);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String status_current = snapshot.child("status").getValue().toString();
                statusUpdate.getEditText().setText(status_current);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        statusUpdateCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChangeStatusActivity.this,SettingsActivity.class);
                startActivity(intent);
                finish();
            }
        });

        statusUpdatebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!validateStatus())
                    return;

                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("status");

                databaseReference.setValue(Objects.requireNonNull(statusUpdate.getEditText().getText().toString()));
                Toast.makeText(ChangeStatusActivity.this, "Status Updated", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ChangeStatusActivity.this,SettingsActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(ChangeStatusActivity.this,SettingsActivity.class);
        startActivity(intent);
        finish();
    }

    private boolean validateStatus() {
        String val = Objects.requireNonNull(Objects.requireNonNull(statusUpdate.getEditText()).getText().toString());
        if(val.isEmpty())
        {
            statusUpdate.setError("Field can't be empty");
            return false;
        }
        else
        {
            statusUpdate.setError(NULL);
            statusUpdate.setErrorEnabled(false);
            return true;
        }
    }

}