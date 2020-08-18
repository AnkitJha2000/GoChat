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

public class RequestsFragment extends Fragment {

    private RecyclerView mrequestlist;
    FirebaseAuth mAuth;
    private DatabaseReference mFriendRequestDatabase;
    private FriendReqRecyclerAdapter adapter;

    public RequestsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mMainView = inflater.inflate(R.layout.fragment_requests, container, false);

        // hooks -------------------...../////////////////////
        mrequestlist = mMainView.findViewById(R.id.friend_req_list);

        mrequestlist.setLayoutManager(new LinearLayoutManager(getContext()));
        mrequestlist.setHasFixedSize(true);

        mAuth = FirebaseAuth.getInstance();
        String mCurrentUserID = mAuth.getCurrentUser().getUid();

        mFriendRequestDatabase = FirebaseDatabase.getInstance().getReference().child("friend_req").child(mCurrentUserID);
        mFriendRequestDatabase.keepSynced(true);

        FirebaseRecyclerOptions<FriendReq> options =
                new FirebaseRecyclerOptions.Builder<FriendReq>()
                        .setQuery(FirebaseDatabase.getInstance().getReference().child("friend_req").child(mCurrentUserID)
                                ,FriendReq.class)
                        .build();

        Context context = getContext();
        adapter = new FriendReqRecyclerAdapter(options,context);
        mrequestlist.setAdapter(adapter);

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