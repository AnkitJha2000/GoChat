package com.example.chattingapp;

import android.accessibilityservice.GestureDescription;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

    public class FriendsRecyclerAdapter extends FirebaseRecyclerAdapter<Friends, FriendsRecyclerAdapter.FriendsViewHolder>{
        Context mContext;
        public FriendsRecyclerAdapter(@NonNull FirebaseRecyclerOptions<Friends> options , Context context) {
            super(options);
            this.mContext = context;
        }

        @Override
        protected void onBindViewHolder(@NonNull final FriendsViewHolder holder, int position, @NonNull Friends model) {
            holder.user_status.setText(model.getDate());
            final String userID = getRef(position).getKey();

            DatabaseReference mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("users");
            mUsersDatabase.keepSynced(true);
            mUsersDatabase.child(userID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    final String username = snapshot.child("name").getValue().toString();
                    final String image = snapshot.child("thumb_image").getValue().toString();
                    if(snapshot.hasChild("online"))
                    {
                        String online = snapshot.child("online").getValue().toString();
                        holder.setOnline_user(online);
                    }
                    holder.setDisplay_name(username);
                    holder.setProfile(image);

                    holder.mView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            CharSequence options[] = new CharSequence[]{"Open Profile","Send Message"};

                            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    // click event for each item
                                    if(i == 0){

                                        Intent intent = new Intent(mContext,ProfileActivity.class);
                                        intent.putExtra("user_id",userID);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        mContext.startActivity(intent);
                                    }
                                    if(i==1)
                                    {
                                        Intent intent = new Intent(mContext,ChattingActivity.class);
                                        intent.putExtra("user_id",userID);
                                        intent.putExtra("display_name",username);
                                        intent.putExtra("imageUrl",image);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        mContext.startActivity(intent);
                                    }
                                }
                            });
                            builder.show();
                        }
                    });

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }

        @NonNull
        @Override
        public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_layout_all_users,parent,false);

            return new FriendsViewHolder(view);
        }

        class FriendsViewHolder extends RecyclerView.ViewHolder{

            View mView ;
            TextView user_status,display_name;
            CircleImageView profile;
            ImageView online_user;

           public FriendsViewHolder(@NonNull View itemView) {
               super(itemView);

               mView = itemView;
               user_status = itemView.findViewById(R.id.user_status);
               display_name = itemView.findViewById(R.id.user_display_name);
               profile = itemView.findViewById(R.id.user_profile_thumb);
           }

           public void setDisplay_name(String name){
               display_name = mView.findViewById(R.id.user_display_name);
               display_name.setText(name);
           }
           public void setProfile(String imageurl){

               profile = itemView.findViewById(R.id.user_profile_thumb);
               Picasso.get().load(imageurl).into(profile);

           }

           public void setOnline_user(String online){
               online_user = itemView.findViewById(R.id.online_user);
               if(online.equals("true"))
               {
                   online_user.setVisibility(View.VISIBLE);
               }
               else
               {
                   online_user.setVisibility(View.INVISIBLE);
               }
           }


       }

    }
