package com.example.chattingapp;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ChatsFragment extends Fragment {
    private RecyclerView mChatList;
    private View mMainView;
    private FirebaseAuth mAuth;
    private String mCurrentUserID;
    private ConversationRecyclerAdapter adapter;
    private DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();

    public ChatsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mMainView = inflater.inflate(R.layout.fragment_chats,container,false);
        mChatList = mMainView.findViewById(R.id.chat_recycler_user);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUserID = mAuth.getCurrentUser().getUid();

        Context context = getContext();

        mChatList.setLayoutManager(new LinearLayoutManager(context));
        mChatList.setHasFixedSize(true);

        FirebaseRecyclerOptions<Conversation> options =
                new FirebaseRecyclerOptions.Builder<Conversation>()
                        .setQuery(FirebaseDatabase.getInstance().getReference().child("chat").child(mCurrentUserID)
                                ,Conversation.class)
                        .build();


        adapter = new ConversationRecyclerAdapter(options,context);
        mChatList.setAdapter(adapter);

        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

}