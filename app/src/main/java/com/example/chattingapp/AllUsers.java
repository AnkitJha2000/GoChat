package com.example.chattingapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class AllUsers extends AppCompatActivity {

    private RecyclerView mUserList;
    private UserRecyclerAdapter adapter;
    Button backtoMain;

    private DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_users);


        // hooks
        mUserList = findViewById(R.id.mUserList);
        backtoMain = findViewById(R.id.backtoMain);

        // back to Main
        backtoMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AllUsers.this,MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // recyclerView

        mUserList.setHasFixedSize(true);
        mUserList.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));

        FirebaseRecyclerOptions<Users> options =
                new FirebaseRecyclerOptions.Builder<Users>()
                        .setQuery(FirebaseDatabase.getInstance().getReference().child("users"), Users.class)
                        .build();
        Context context = getApplicationContext();
        adapter = new UserRecyclerAdapter(options,context);
        mUserList.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
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
        adapter.stopListening();
        mAuth = FirebaseAuth.getInstance();
        // String uid = mAuth.getCurrentUser().getUid();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            mRootRef.child("users").child(currentUser.getUid()).child("online").setValue(ServerValue.TIMESTAMP);
        }

    }




























    /*
    private void recycling() {
        System.out.println("Recycling is running now ?????????????????????????????????????????????????????");
        Toast.makeText(this, "Recycling", Toast.LENGTH_SHORT).show();
        FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = current_user.getUid();
        Query query = FirebaseDatabase.getInstance().getReference().child("users").child(uid);
        FirebaseRecyclerOptions<Users> options =
                new FirebaseRecyclerOptions.Builder<Users>()
                        .setQuery(query, Users.class)
                        .build();

        FirebaseRecyclerAdapter<Users, UsersViewHolder> adapter = new FirebaseRecyclerAdapter<Users, UsersViewHolder>(options) {
            @Override
            public UsersViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                // Create a new instance of the ViewHolder, in this case we are using a custom
                // layout called R.layout.message for each item
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.recycle_layout_all_users, parent, false);
                System.out.println("Adapter RECYCLER start is running now ?????????????????????????????????????????????????????");
                Toast.makeText(getApplicationContext(), "Adapter", Toast.LENGTH_SHORT).show();
                return new UsersViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(UsersViewHolder holder, int position, Users model) {
                // Bind the Chat object to the ChatHolder
                // ...
                // RecycleHelperClass recycleHelperClass = recycleHelperClasses.get(position);
                holder.user_status.setText(model.getStatus());
                holder.user_display_name.setText(model.getName());
                Picasso.get().load(model.getImage()).into(holder.user_profile_thumb);
                System.out.println("VIEW BINDING is running now ?????????????????????????????????????????????????????");
                Toast.makeText(getApplicationContext(), "View Binding", Toast.LENGTH_SHORT).show();
            }

        };
        mUserList.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Toast.makeText(this, "ON start", Toast.LENGTH_SHORT).show();
        recycling();
    }

    public class UsersViewHolder extends RecyclerView.ViewHolder{

        CircleImageView user_profile_thumb;
        TextView user_display_name,user_status;

        public UsersViewHolder(@NonNull View itemView) {
            super(itemView);

            user_profile_thumb = itemView.findViewById(R.id.user_profile_thumb);
            user_display_name = itemView.findViewById(R.id.user_display_name);
            user_status = itemView.findViewById(R.id.user_status);

        }
    }

 */
}