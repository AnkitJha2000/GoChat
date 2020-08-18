package com.example.chattingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.text.DateFormat;
import java.util.Date;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private Toolbar mtoolbar;
    private FirebaseAuth mAuth;
    private ViewPager mViewPager;
    private SectionsPagerAdapter sectionsPagerAdapter;

    private DatabaseReference mUserRef;

    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // firebase
        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser() != null )
        {
            String uid = mAuth.getCurrentUser().getUid();

            mUserRef = FirebaseDatabase.getInstance().getReference().child("users").child(uid);
        }

        // hooks
        mtoolbar = findViewById(R.id.main_page_toolbar);
        mViewPager = findViewById(R.id.main_tabPager);
        tabLayout = findViewById(R.id.main_page_tab_layout);


        // tabs
        sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager.setAdapter(sectionsPagerAdapter);

        tabLayout.setupWithViewPager(mViewPager);


        setSupportActionBar(mtoolbar);
        getSupportActionBar().setTitle("Go Chat");

    }
    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null)
        {
            Intent intent = new Intent(MainActivity.this,StartScreen.class);
            startActivity(intent);
            finish();
        }
         if(currentUser != null)
         {
             mUserRef.child("online").setValue("true");
         }
    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu , menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId() == R.id.logout)
        {
            mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(MainActivity.this,StartScreen.class);
            startActivity(intent);
            finish();
        }
        if(item.getItemId() == R.id.accountSettings)
        {
            Intent intent = new Intent(MainActivity.this,SettingsActivity.class);
            startActivity(intent);
        }
        if (item.getItemId() == R.id.allusers)
        {
            Intent intent = new Intent(MainActivity.this,AllUsers.class);
            startActivity(intent);
        }
        return true;
    }


}