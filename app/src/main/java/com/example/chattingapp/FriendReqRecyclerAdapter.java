package com.example.chattingapp;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendReqRecyclerAdapter extends FirebaseRecyclerAdapter <FriendReq, FriendReqRecyclerAdapter.RequestsViewHolder>  {

    Context mContext;
    public FriendReqRecyclerAdapter(@NonNull FirebaseRecyclerOptions options , Context mContext) {
        super(options);
        this.mContext = mContext;
    }


    @NonNull
    @Override
    public RequestsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.requests_layout,parent,false);

        return new FriendReqRecyclerAdapter.RequestsViewHolder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull final RequestsViewHolder holder, int position, @NonNull FriendReq model) {
        final String uid = getRef(position).getKey();

        final String currentUseruid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        final DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
        mRootRef.keepSynced(true);
        mRootRef.child("users").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String image = snapshot.child("thumb_image").getValue().toString();
                final String display_name = snapshot.child("name").getValue().toString();
                String status = snapshot.child("status").getValue().toString();
                final String imageURL = snapshot.child("image").getValue().toString();

                holder.setDisplay_name(display_name);
                holder.setUser_status(status);
                holder.setProfile(image);

                holder.mainView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(mContext,ProfileActivity.class);
                        intent.putExtra("user_id",uid);
                        intent.putExtra("display_name",display_name);
                        intent.putExtra("imageUrl",imageURL);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mContext.startActivity(intent);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    class RequestsViewHolder extends RecyclerView.ViewHolder {

        TextView user_status,display_name;
        CircleImageView profile;

        View mainView;

        public RequestsViewHolder(@NonNull View itemView) {
            super(itemView);

            mainView = itemView;
            // display_name = mainView.findViewById(R.id.req_user_display_name);

        }

        public void setDisplay_name(String name)
        {
            display_name = mainView.findViewById(R.id.req_user_display_name);
            display_name.setText(name);
        }

        public void setProfile(String url) {
            profile = mainView.findViewById(R.id.req_user_profile_thumb);
            Picasso.get().load(url).placeholder(R.drawable.profile).into(profile);
        }
        public void setUser_status(String status)
        {
            user_status = mainView.findViewById(R.id.req_user_status);
            user_status.setText(status);
        }

    }

}
