package com.example.chattingapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class ChattingActivity extends AppCompatActivity {
    TextView display_name,last_seen;
    String userID;
    private FirebaseAuth mAuth;
    CircleImageView profile_thumb;
    private DatabaseReference mRootRef;
    private RecyclerView  mMessagesList;
    private Button add_file_btn,send_message_btn;
    private EditText mChatMessageView;
    private Button backtoMain;
    private String mCurrentuserID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager mLinearLayout;
    private MessageAdapter mAdapter;

    private StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
    private static final int TOTAL_ITEMS_TO_LOAD = 10;
    private int mCurrentPage = 1;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private int itempos = 0;
    private String mLastKey = "";
    private String mPrevKey = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatting);

        // firebase -----------------///////////////////////////--------------------------
        mRootRef = FirebaseDatabase.getInstance().getReference();

        // hooks ------------------------//////////////////////////--------------------------------
        display_name = findViewById(R.id.user_chat_display_name);
        profile_thumb = findViewById(R.id.user_chat_profile);
        backtoMain = findViewById(R.id.backtoMainChat);
        last_seen =findViewById(R.id.last_seen);
        add_file_btn = findViewById(R.id.addfile);
        send_message_btn = findViewById(R.id.send_btn);
        mChatMessageView = findViewById(R.id.msg_text);
        mMessagesList = findViewById(R.id.message_recycler);
        mSwipeRefreshLayout = findViewById(R.id.message_swipe_layout);

        // toolbar set --------------//////////////////////////////------------------------------------
        userID = getIntent().getStringExtra("user_id");
        String username = getIntent().getStringExtra("display_name");
        String imageURL =  getIntent().getStringExtra("imageUrl");

        display_name.setText(username);
        Picasso.get().load(imageURL).placeholder(R.drawable.profile).into(profile_thumb);


        // chat recycler adapter//////////////////////////////////////-----------------------------------

        mAdapter = new MessageAdapter(messagesList , ChattingActivity.this);

        mLinearLayout = new LinearLayoutManager(this);

        mMessagesList.setHasFixedSize(true);
        mMessagesList.setLayoutManager(mLinearLayout);
        mMessagesList.setAdapter(mAdapter);

        loadMessages();

        // back to Main Chat -------------////////////////////////////////-------------------------------

        backtoMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChattingActivity.this,MainActivity.class);
                startActivity(intent);
                finish();
            }
        });



        // toolbar last seen ////////////////////////////////////////////////////////////////
        mRootRef.child("users").child(userID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String online = snapshot.child("online").getValue().toString();

                if(online.equals("true"))
                {
                    last_seen.setText("online");
                }
                else
                {
                    GetTimeAgo getTimeAgo = new GetTimeAgo();
                    long lastTime = Long.parseLong(online);
                    String lastSeen = getTimeAgo.getTimeAgo(lastTime,getApplicationContext());
                    last_seen.setText("Last Seen "+lastSeen);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        mRootRef.child("chat").child(mCurrentuserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.hasChild(userID))
                {

                    Map chatAddMap = new HashMap();
                    chatAddMap.put("seen",false);
                    chatAddMap.put("timestamp",ServerValue.TIMESTAMP);

                    Map chatUserMap = new HashMap();
                    chatUserMap.put("chat/" + mCurrentuserID + "/" + userID, chatAddMap);
                    chatUserMap.put("chat/"+ userID + "/" + mCurrentuserID, chatAddMap);

                    mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                            if(error != null) {
                                Log.d("chat-log", error.getMessage());
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // recyclerView set up for showing and receiving messages;

        mMessagesList.setHasFixedSize(true);
        mMessagesList.setLayoutManager(new LinearLayoutManager(this));

        // send Button //////////////////////////////////////////////////////////////////////////////////

        send_message_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                sendMessage();

            }
        });
        // swipe to refresh_layout ---------////////////////--------------///////////////----------------

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                mCurrentPage++;
                itempos = 0;
                loadMoreMessages();
            }
        });
        // add photo button -----------------------////////////////////////////////////////////////////

        add_file_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON).setAspectRatio(1,1)
                        .setMinCropWindowSize(350,350)
                        .start(ChattingActivity.this);
            }
        });

    }

    private void loadMoreMessages() {
        DatabaseReference messageRef = mRootRef.child("messages").child(mCurrentuserID).child(userID);
        Query messageQuery = messageRef.orderByKey().endAt(mLastKey).limitToLast(10);
        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {


                Messages message = dataSnapshot.getValue(Messages.class);
                String messageKey = dataSnapshot.getKey();

                if(!mPrevKey.equals(messageKey)){

                    messagesList.add(itempos++, message);

                } else {

                    mPrevKey = mLastKey;

                }


                if(itempos == 1) {

                    mLastKey = messageKey;

                }


                Log.d("TOTALKEYS", "Last Key : " + mLastKey + " | Prev Key : " + mPrevKey + " | Message Key : " + messageKey);

                mAdapter.notifyDataSetChanged();

                mSwipeRefreshLayout.setRefreshing(false);

                mLinearLayout.scrollToPositionWithOffset(10, 0);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        mSwipeRefreshLayout.setRefreshing(false);
    }

    private void loadMessages() {

        DatabaseReference messageRef = mRootRef.child("messages").child(mCurrentuserID).child(userID);

        Query messageQuery = messageRef.limitToLast(mCurrentPage * TOTAL_ITEMS_TO_LOAD);


        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Messages message = dataSnapshot.getValue(Messages.class);

                itempos++;

                if(itempos == 1){

                    String messageKey = dataSnapshot.getKey();

                    mLastKey = messageKey;
                    mPrevKey = messageKey;

                }

                messagesList.add(message);
                mAdapter.notifyDataSetChanged();

                mMessagesList.scrollToPosition(messagesList.size() - 1);

                mSwipeRefreshLayout.setRefreshing(false);

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void sendMessage() {

        String message = mChatMessageView.getText().toString();
        if(!TextUtils.isEmpty(message)) {
            String currentUserRef = "messages/" + mCurrentuserID + "/" + userID;
            String chatUserRef = "messages/" + userID + "/" + mCurrentuserID;

            DatabaseReference user_message_push = mRootRef.child("messages").child(mCurrentuserID).child(userID).push();

            String pushID = user_message_push.getKey();

            Map msgMap = new HashMap();
            msgMap.put("message" ,message);
            msgMap.put("seen" ,false);
            msgMap.put("type" ,"text");
            msgMap.put("time" ,ServerValue.TIMESTAMP);
            msgMap.put("from",mCurrentuserID);

            Map messageUserMap = new HashMap();
            messageUserMap.put(currentUserRef + "/" + pushID,msgMap);
            messageUserMap.put(chatUserRef + "/" + pushID,msgMap);

            mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                    if(error != null)
                    {
                        Log.d("chat_log",error.getMessage());
                    }
                }
            });

        }
        mChatMessageView.setText(null);
    }

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

    // image sending ---------------------------///////////////////////////////////////////////////
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK)
            {

                Uri resultUri = result.getUri();

                final String current_user_ref = "messages/" + mCurrentuserID + "/" + userID;
                final String chat_user_ref = "messages/" + userID + "/" + mCurrentuserID;

                DatabaseReference user_message_image = mRootRef.child("messages").child(mCurrentuserID).child(userID).push();

                final String push_id = user_message_image.getKey();
                File thumb_filePath = new File(resultUri.getPath());
                Bitmap thumb_bitmap = null;
                try {
                    thumb_bitmap = new Compressor(this)
                            .setMaxWidth(200)
                            .setMaxHeight(200)
                            .setQuality(75)
                            .compressToBitmap(thumb_filePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                final byte[] thumb_byte = baos.toByteArray();


                final StorageReference filepath = mStorageRef.child("messageImages").child( push_id + ".jpg");
                // continue from here //////////////////////////////////////////////
                UploadTask uploadTask = filepath.putBytes(thumb_byte);

                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {

                                String downloadUrl = uri.toString();
                                // System.out.println(downloadUrl + " THE URL IS THIS ANKIT");
                                Map msgMap = new HashMap();
                                msgMap.put("message" ,downloadUrl);
                                msgMap.put("seen" ,false);
                                msgMap.put("type" ,"image");
                                msgMap.put("time" ,ServerValue.TIMESTAMP);
                                msgMap.put("from",mCurrentuserID);

                                Map messageUserMap = new HashMap();
                                messageUserMap.put(current_user_ref + "/" + push_id,msgMap);
                                messageUserMap.put(chat_user_ref + "/" + push_id,msgMap);

                                mChatMessageView.setText("");

                                mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                        if(error != null)
                                        {
                                            Log.d("chat_log",error.getMessage());
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