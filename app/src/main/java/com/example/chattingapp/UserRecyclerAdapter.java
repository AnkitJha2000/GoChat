package com.example.chattingapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.circularreveal.cardview.CircularRevealCardView;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class UserRecyclerAdapter extends FirebaseRecyclerAdapter<Users, UserRecyclerAdapter.UserViewHolder> {

    Context mContext;
    public UserRecyclerAdapter(@NonNull FirebaseRecyclerOptions<Users> options, Context context) {
        super(options);
        this.mContext = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull UserViewHolder holder, int position, @NonNull Users model) {

        holder.user_display_name.setText(model.getName());
        holder.user_status.setText(model.getStatus());

        Picasso.get().load(model.getThumb_image()).placeholder(R.drawable.profile).into(holder.user_profile_thumb);

        final String user_id = model.getUid();
        final String displayName = model.getName();
        final String status = model.getStatus();
        final String imageUrl = model.getImage();

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext,ProfileActivity.class);
                intent.putExtra("display name",displayName);
                intent.putExtra("user status",status);
                intent.putExtra("imageUrl",imageUrl);
                intent.putExtra("user_id",user_id);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
            }
        });

    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycle_layout_all_users, parent, false);

        return new UserViewHolder(view);
    }

    class UserViewHolder extends RecyclerView.ViewHolder{

        CircleImageView user_profile_thumb;
        TextView user_display_name,user_status;
        View mView;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            user_display_name = itemView.findViewById(R.id.user_display_name);
            user_profile_thumb = itemView.findViewById(R.id.user_profile_thumb);
            user_status = itemView.findViewById(R.id.user_status);

        }
    }

}
