package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UsersActivity extends AppCompatActivity implements UsersAdapter.OnUserClickListener {
    private RecyclerView usersRecyclerView;
    private ProgressBar progressBar;
    private UsersAdapter usersAdapter;
    private List<User> usersList;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        // Initialize views
        usersRecyclerView = findViewById(R.id.usersRecyclerView);
        progressBar = findViewById(R.id.progressBar);

        // Initialize Firebase
        usersRef = FirebaseDatabase.getInstance().getReference("Users");

        // Setup RecyclerView
        usersList = new ArrayList<>();
        usersAdapter = new UsersAdapter(usersList, this);
        usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        usersRecyclerView.setAdapter(usersAdapter);

        // Load users
        loadUsers();
    }

    private void loadUsers() {
        progressBar.setVisibility(View.VISIBLE);
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                usersList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        usersList.add(user);
                    }
                }
                usersAdapter.updateUsers(usersList);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(UsersActivity.this, "Error loading users: " + databaseError.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onUserClick(User user) {
        // Handle user click - you can start a chat or show user details
        Intent intent = new Intent(this,ChatActivity.class);
        intent.putExtra("userID",user.getUserID());
        intent.putExtra("userName",user.getName());
        startActivity(intent);
        Toast.makeText(this, "Selected user: " + user.getName(), Toast.LENGTH_SHORT).show();
    }
} 