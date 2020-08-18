package com.example.chattingapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter< MessageAdapter.MessageViewHolder> {
    private List<Messages> mMessagesList;
    private Context mContext ;

    public MessageAdapter(List<Messages> mMessagesList , Context mContext) {
        this.mMessagesList = mMessagesList;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_single_layout,parent,false);
        return new MessageViewHolder(view);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, int position) {
        String currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Messages messages = mMessagesList.get(position);
        String message_type = messages.getType();
        String chatUserID = messages.getFrom();
        long time = messages.getTime();
        GetTimeAgo getTimeAgo = new GetTimeAgo();
        String lastSeen = getTimeAgo.getTimeAgo(time,mContext);
        holder.message_time_layout.setText(lastSeen);

        if(messages.getFrom().equals(currentUserID))
        {
            holder.message_chat_layout.setBackgroundColor(Color.WHITE);

        } else {
            holder.message_chat_layout.setBackgroundResource(R.drawable.message_layout);
        }

        FirebaseDatabase.getInstance().getReference().child("users").child(chatUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String image = snapshot.child("thumb_image").getValue().toString();
                holder.setMessage_profile_layout(image);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if(message_type.equals("text"))
        {
            holder.message_chat_layout.setText(messages.getMessage());
            holder.user_message_image.setVisibility(View.GONE);
        }
        else
        {
            holder.message_chat_layout.setVisibility(View.INVISIBLE);
            Picasso.get().load(messages.getMessage()).placeholder(R.drawable.ic_baseline_account_box_24).into(holder.user_message_image);
        }

    }

    @Override
    public int getItemCount() {
        return mMessagesList.size();
    }

    class MessageViewHolder extends RecyclerView.ViewHolder{

        View mMainView;
        CircleImageView message_profile_layout;
        TextView message_chat_layout,message_time_layout;
        ImageView user_message_image;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            mMainView = itemView;
            message_profile_layout = mMainView.findViewById(R.id.message_profile_layout);
            message_chat_layout = mMainView.findViewById(R.id.message_chat_layout);
            message_time_layout = mMainView.findViewById(R.id.message_time_layout);
            user_message_image = mMainView.findViewById(R.id.user_message_image);

        }

        public void setMessage_profile_layout(String image)
        {
            Picasso.get().load(image).placeholder(R.drawable.profile).into(message_profile_layout);
        }

    }
}
