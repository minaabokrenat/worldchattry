package com.example.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChatActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;

    private RecyclerView messagesRecyclerView;
    private EditText messageInput;
    private ImageButton sendButton;
    private ImageButton attachButton;
    private ImageButton backButton;
    private TextView chatUserName;
    private MessageAdapter messageAdapter;
    private List<Message> messages;

    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private DatabaseReference dbr;
    private String receiverUserId;
    private String receiverUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_acivity);

        // Get receiver's information from intent
        receiverUserId = getIntent().getStringExtra("userID");
        receiverUserName = getIntent().getStringExtra("userName");

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        dbr = FirebaseDatabase.getInstance().getReference("Messages");

        // Initialize views
        messagesRecyclerView = findViewById(R.id.messagesRecyclerView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        attachButton = findViewById(R.id.attachButton);
        backButton = findViewById(R.id.backButton);
        chatUserName = findViewById(R.id.chatUserName);

        // Set receiver's name in the toolbar
        chatUserName.setText(receiverUserName);

        // Setup RecyclerView
        messages = new ArrayList<>();
        messageAdapter = new MessageAdapter(auth.getUid(), messages);
        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        messagesRecyclerView.setAdapter(messageAdapter);

        // Setup click listeners
        sendButton.setOnClickListener(v -> sendTextMessage());
        attachButton.setOnClickListener(v -> openImagePicker());
        backButton.setOnClickListener(v -> finish());

        // Load messages
        loadMessages();
    }

    private void loadMessages() {

        dbr.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messages.clear();
                for (DataSnapshot  data  : snapshot.getChildren()) {
                    Message message = data.getValue(Message.class);
                    if (message == null) continue;
                    if (message.getSenderID().equals(currentUser.getUid()) && message.getRecieveID().equals(receiverUserId) ||
                            message.getSenderID().equals(receiverUserId) && message.getRecieveID().equals(currentUser.getUid()))
                        messages.add(message);

                }
                messagesRecyclerView.scrollToPosition(messages.size() - 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void sendTextMessage() {
        String messageText = messageInput.getText().toString().trim();
        if (!TextUtils.isEmpty(messageText)) {
            Message message = new Message(messageText, currentUser.getUid(), receiverUserId, Message.TYPE_TEXT);
            dbr.push().setValue(message);
            messageInput.setText("");
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                uploadImage(imageUri);
            }
        }
    }

    private void uploadImage(Uri imageUri) {
        // Implement image sending logic here
    }
}