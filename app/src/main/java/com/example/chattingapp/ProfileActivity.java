package com.example.chattingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

import javax.xml.transform.sax.SAXResult;

public class ProfileActivity extends AppCompatActivity {
    ImageView profile;
    TextView display_name_view,user_status,total_friends;
    Button friend_request,decline_request;
    ProgressDialog progressDialog;
    private DatabaseReference mUsersDatabase;
    private DatabaseReference mFriendRequestDatabase;
    private DatabaseReference mFriendDatabase;
    private FirebaseAuth mAuth;
    private DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();

    private int mCurrentState;
    private FirebaseUser mCurrentUser;

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
        setContentView(R.layout.activity_profile);
        // intent extras ///////////////////////////////////////////////////////////////////////////

        final String userID  =(getIntent().getStringExtra("user_id"));
        // System.out.println(userID + "???????????????????????????????????????????????????????????????????????????????????");

        // hooks ///////////////////////////////////////////////////////////////////////////////////
        profile = findViewById(R.id.user_profile_image_other);
        display_name_view = findViewById(R.id.user_display_name_other);
        user_status = findViewById(R.id.user_status_other);
        total_friends = findViewById(R.id.friends_number_other);
        friend_request = findViewById(R.id.send_friend_request);
        decline_request = findViewById(R.id.decline_friend_request);

        // friend request
        mCurrentState = 0;
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();


        // progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading Profile");
        progressDialog.setMessage("Please Wait while we are loading user's profile");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        //firebase only profile loading
        assert userID != null;
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(userID);
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("friends");



        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                String display_name = dataSnapshot.child("name").getValue().toString();
                String userStatus = dataSnapshot.child("status").getValue().toString();
                String imageUrl = dataSnapshot.child("image").getValue().toString();
                //-----------------------FRIEND REQUEST/LIST FEATURE -------------------//////

                mFriendRequestDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        friend_request.setEnabled(true);
                        if(dataSnapshot.hasChild(userID))
                        {
                            String request_type = dataSnapshot.child(userID).child("request_type").getValue().toString();
                            if(request_type.equals("received")){
                                mCurrentState = 2;
                                friend_request.setText("Accept Friend Request");

                                decline_request.setVisibility(View.VISIBLE);
                                decline_request.setEnabled(true);

                            }
                            else if(request_type.equals("sent"))
                            {
                                mCurrentState = 1;
                                friend_request.setText("Cancel Friend Request");

                                decline_request.setVisibility(View.INVISIBLE);
                                decline_request.setEnabled(false);

                            }
                            progressDialog.dismiss();
                        }
                        else {

                            mFriendDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(snapshot.hasChild(userID))
                                    {
                                        mCurrentState = 3;
                                        friend_request.setText("UnFriend this Person");
                                        decline_request.setVisibility(View.INVISIBLE);
                                        decline_request.setEnabled(false);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                        }
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressDialog.dismiss();
                    }
                });



                Picasso.get().load(imageUrl).into(profile);
                //System.out.println(display_name + "???????????????????????????????????????????????????????????????????????????????????");
                //System.out.println(imageUrl + "???????????????????????????????????????????????????????????????????????????????????");
                //System.out.println(userStatus + "???????????????????????????????????????????????????????????????????????????????????");

                display_name_view.setText(display_name);
                user_status.setText(userStatus);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // 0 for not friends
        // 1 for friend requests sent(btn = cancel request)
        // 2 for Accept friend Request
        // 3 for ALready friends (unfriend button)




        // send request btn
        mFriendRequestDatabase = FirebaseDatabase.getInstance().getReference().child("friend_req");

        friend_request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                friend_request.setEnabled(false);
                decline_request.setVisibility(View.INVISIBLE);
                decline_request.setEnabled(false);

                // ------------------------NOT FRIENDS STATE _------------_--_////////////////////////
                if(mCurrentState == 0)
                {
                    decline_request.setVisibility(View.INVISIBLE);
                    decline_request.setEnabled(false);
                    mFriendRequestDatabase.child(mCurrentUser.getUid()).child(userID).child("request_type").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                mFriendRequestDatabase.child(userID).child(mCurrentUser.getUid()).child("request_type").setValue("received").addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        mCurrentState = 1;
                                        friend_request.setText("Cancel Friend Request");

                                        decline_request.setVisibility(View.INVISIBLE);
                                        decline_request.setEnabled(false);

                                        Toast.makeText(ProfileActivity.this, "Friend Request Sent!!", Toast.LENGTH_SHORT).show();


                                    }
                                });
                            }
                            else
                            {
                                decline_request.setVisibility(View.INVISIBLE);
                                decline_request.setEnabled(false);
                                Toast.makeText(ProfileActivity.this, "Error! Something Went Wrong!!", Toast.LENGTH_SHORT).show();
                            }
                            friend_request.setEnabled(true);
                        }
                    });
                }
                //// -----------------------CANCEL FRIEND REQUESTS_______------_________-----------_______////////////
                if(mCurrentState == 1)
                {
                    friend_request.setEnabled(true);
                    mFriendRequestDatabase.child(mCurrentUser.getUid()).child(userID).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            mFriendRequestDatabase.child(userID).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    friend_request.setEnabled(true);
                                    mCurrentState = 0;
                                    friend_request.setText("Send Friend Request");
                                    decline_request.setVisibility(View.INVISIBLE);
                                    decline_request.setEnabled(false);
                                }
                            });

                        }
                    });
                }

                //// -----------------------ACCEPT FRIEND REQUESTS_______------_________-----------_______////////////

                if(mCurrentState == 2)
                {
                    friend_request.setEnabled(true);
                    final String date_since = DateFormat.getDateTimeInstance().format(new Date());
                    mFriendDatabase.child(mCurrentUser.getUid()).child(userID).child("date").setValue(date_since).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendDatabase.child(userID).child(mCurrentUser.getUid()).child("date").setValue(date_since).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    // REMOVING REQUESTS FROM DATABASE ____________------------------///////////////////
                                    mFriendRequestDatabase.child(mCurrentUser.getUid()).child(userID).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                            mFriendRequestDatabase.child(userID).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    friend_request.setEnabled(true);
                                                    mCurrentState = 3;
                                                    friend_request.setText("UnFriend this Person");
                                                    decline_request.setVisibility(View.INVISIBLE);
                                                    decline_request.setEnabled(false);
                                                }
                                            });
                                        }
                                    });
                                    ////////////////////////////////////////////////////////////////////////////////////
                                    Toast.makeText(ProfileActivity.this, "Request Accepted!", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                    mFriendDatabase.child(mCurrentUser.getUid()).child(userID).child("FriendID").setValue(userID);
                    mFriendDatabase.child(userID).child(mCurrentUser.getUid()).child("FriendID").setValue(mCurrentUser.getUid());

                }
                if(mCurrentState == 3)
                {
                    friend_request.setEnabled(true);
                    friend_request.setText("Send Friend Request");
                    mFriendDatabase.child(mCurrentUser.getUid()).child(userID).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendDatabase.child(userID).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    mCurrentState = 0;
                                    friend_request.setText("Send Friend Request");
                                    Toast.makeText(ProfileActivity.this, "Unfriend Successful !", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });

                }

            }
        });

        // decline request
        decline_request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                decline_request.setVisibility(View.INVISIBLE);
                decline_request.setEnabled(false);

                mFriendRequestDatabase.child(mCurrentUser.getUid()).child(userID).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mFriendRequestDatabase.child(userID).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                mCurrentState = 0;
                                friend_request.setText("Send Friend Request");
                                Toast.makeText(ProfileActivity.this, "Request cancelled successfully !!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }
        });

    }

}