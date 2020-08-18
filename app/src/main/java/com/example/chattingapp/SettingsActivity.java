package com.example.chattingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.icu.util.ICUUncheckedIOException;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {

    CircleImageView profile_image;
    TextView display_name,userStatus;
    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    String uid = currentUser.getUid();
    Button changeProfile,changeStatus,restoreDefaults;
    DatabaseReference mDatabaseReference;
    FirebaseAuth mAuth;
    private DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();;
    // firebase storage

    private StorageReference mStorageRef;

    ProgressDialog progressDialog;

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
        setContentView(R.layout.activity_settings);
        mStorageRef = FirebaseStorage.getInstance().getReference();
        // hooks
        profile_image = findViewById(R.id.profile_image);
        display_name = findViewById(R.id.display_name);
        userStatus = findViewById(R.id.userStatus);
        changeProfile = findViewById(R.id.changeProfile);
        changeStatus = findViewById(R.id.changeStatus);
        restoreDefaults = findViewById(R.id.restoreDefaults);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = currentUser.getUid();

        // firebase

        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(uid);
        mDatabaseReference.keepSynced(true);

        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //Toast.makeText(SettingsActivity.this, snapshot.toString(), Toast.LENGTH_SHORT).show();

                String name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                final String Image = dataSnapshot.child("image").getValue().toString();

                display_name.setText(name);
                userStatus.setText(status);

                // Picasso.get().load(Image).placeholder(R.drawable.profile).into(profile_image);
                Picasso.get().load(Image).networkPolicy(NetworkPolicy.OFFLINE)
                        .placeholder(R.drawable.profile).into(profile_image, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError(Exception e) {
                        Picasso.get().load(Image).placeholder(R.drawable.profile).into(profile_image);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // restore defaults
        restoreDefaults.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/gochat-2ef75.appspot.com/o/profile_images%2Fdefault.jpg?alt=media&token=2abaa849-e75f-47ce-93b4-dd2ecf89c8a8").placeholder(R.drawable.profile).into(profile_image);
                userStatus.setText("Hi there! I am using GO chat.");

                mDatabaseReference.child("image").setValue("https://firebasestorage.googleapis.com/v0/b/gochat-2ef75.appspot.com/o/profile_images%2Fdefault.jpg?alt=media&token=2abaa849-e75f-47ce-93b4-dd2ecf89c8a8");
                mDatabaseReference.child("thumb_image").setValue("https://firebasestorage.googleapis.com/v0/b/gochat-2ef75.appspot.com/o/profile_images%2Fdefault.jpg?alt=media&token=2abaa849-e75f-47ce-93b4-dd2ecf89c8a8");
                mDatabaseReference.child("status").setValue("Hi there! I am using GO chat.");

            }
        });


        // change status btn
        changeStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingsActivity.this,ChangeStatusActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // change image
        changeProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*
                Intent galleryIntent = new Intent();
                galleryIntent.setType("Image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(galleryIntent,"SELECT IMAGE"),GALLERY_PICK);
                */
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON).setAspectRatio(1,1)
                        .setMinCropWindowSize(500,500)
                        .start(SettingsActivity.this);
            }
        });

    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK)
            {
                progressDialog = new ProgressDialog(SettingsActivity.this);
                progressDialog.setTitle("Loading");
                progressDialog.setMessage("Please wait while Uploading your profile picture");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();

                Uri resultUri = result.getUri();

                File thumb_file = new File(resultUri.getPath());

                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                final String uid = currentUser.getUid();



                // // bitmap upload ////////////////////////////////////////////////////////////////
                try {
                    Bitmap thumb_bitmap = new Compressor(this)
                            .setMaxHeight(200)
                            .setMaxWidth(200)
                            .setQuality(75)
                            .compressToBitmap(thumb_file);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] thumb_byte = baos.toByteArray();
                    final StorageReference thumbpath = mStorageRef.child("thumbs").child(uid + ".jpg");
                    UploadTask uploadTask = thumbpath.putBytes(thumb_byte);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            progressDialog.dismiss();
                            Toast.makeText(SettingsActivity.this, "Failed to upload Thumb image", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                            // ...
                            thumbpath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String thumb_url = uri.toString();
                                    mDatabaseReference.child("thumb_image").setValue(thumb_url).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful())
                                            {
                                                Toast.makeText(SettingsActivity.this, "thumbnail successfully updated", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                            });
                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Failed to convert to bitmap", Toast.LENGTH_SHORT).show();
                }



                // profile image upload full sized //////////////////////////
                final StorageReference filepath = mStorageRef.child("profile_images").child(uid + ".jpg");
                // continue from here //////////////////////////////////////////////
                UploadTask uploadTask = filepath.putFile(resultUri);

                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String downloadUrl = uri.toString();
                                System.out.println(downloadUrl + " THE URL IS THIS ANKIT");
                                mDatabaseReference.child("image").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful())
                                        {
                                            progressDialog.dismiss();
                                            Toast.makeText(SettingsActivity.this, "profile picture updated", Toast.LENGTH_SHORT).show();
                                        }

                                    }
                                });
                            }
                        });
                    }
                });
            }
            else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

    }

}

/*

                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful())
                        {
                            String downloadUrl = filepath.getDownloadUrl().toString();
                            System.out.println(downloadUrl + " THE URL IS THIS ANKIT");
                            mDatabaseReference.child("image").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful())
                                    {
                                        progressDialog.dismiss();
                                        Toast.makeText(SettingsActivity.this, "profile picture updated", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                        else
                        {
                            progressDialog.dismiss();
                            Toast.makeText(SettingsActivity.this, "Error", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

 */