package com.example.chattingapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ConversationRecyclerAdapter extends FirebaseRecyclerAdapter <Conversation, ConversationRecyclerAdapter.ConverstionViewHolder> {

    Context mContext;

    public ConversationRecyclerAdapter(@NonNull FirebaseRecyclerOptions<Conversation> options , Context context) {
        super(options);
        this.mContext = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull final ConverstionViewHolder holder, int position, @NonNull Conversation model) {

        final String userID = getRef(position).getKey();
        // getting user data =-----------------------------/////////////////////////////////////
        DatabaseReference usersDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(userID);
        usersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // see if user is online ----------------///////////////////////////////////////////
                if(snapshot.hasChild("online")) {
                    String online = snapshot.child("online").getValue().toString();
                    if (online.equals("true"))
                    {
                        holder.online.setVisibility(View.VISIBLE);
                    }
                }

                final String display_name = snapshot.child("name").getValue().toString();
                holder.user_display_name.setText(display_name);

                final String image = snapshot.child("thumb_image").getValue().toString();
                Picasso.get().load(image).placeholder(R.drawable.profile).into(holder.user_profile_thumb);

                String status = snapshot.child("status").getValue().toString();
                holder.user_last_text.setText(status);

                // click on chat single layout ---------------////////////////////////////////////

                holder.mainView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Intent intent = new Intent(mContext,ChattingActivity.class);
                        intent.putExtra("user_id",userID);
                        intent.putExtra("display_name",display_name);
                        intent.putExtra("imageUrl",image);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mContext.startActivity(intent);

                    }
                });

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // getting last message sent ///////////////////////////////////////////////////////////



    }

    @NonNull
    @Override
    public ConverstionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_layout_all_users , parent,false);

        return new ConverstionViewHolder(view);
    }

    class ConverstionViewHolder extends RecyclerView.ViewHolder{

        View mainView;
        CircleImageView user_profile_thumb;
        TextView user_display_name,user_last_text;
        ImageView online;

        public ConverstionViewHolder(@NonNull View itemView) {
            super(itemView);

            mainView = itemView;

            user_profile_thumb = mainView.findViewById(R.id.user_profile_thumb);
            user_display_name = mainView.findViewById(R.id.user_display_name);
            user_last_text = mainView.findViewById(R.id.user_status);
            online = mainView.findViewById(R.id.online_user);

        }
    }

}
