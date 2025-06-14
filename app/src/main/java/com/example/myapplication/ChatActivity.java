package com.example.myapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChatActivity extends AppCompatActivity {
    private static final int MAX_IMAGE_SIZE = 800; // Maximum width or height in pixels
    private static final int COMPRESSION_QUALITY = 80; // JPEG compression quality (0-100)

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
    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri uri) {
                    if (uri != null) {
                        Uri imageUri = uri;
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(ChatActivity.this.getContentResolver(), imageUri);
                            Bitmap resizedBitmap = resizeImage(bitmap);
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESSION_QUALITY, baos);
                            byte[] imageData = baos.toByteArray();
                            String base64Image = android.util.Base64.encodeToString(imageData, android.util.Base64.DEFAULT);
                            Message message = new Message(base64Image, currentUser.getUid(), receiverUserId, Message.TYPE_IMAGE);
                            uploadImage(message);
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(ChatActivity.this, "Failed to load image", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
    );
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
        imagePickerLauncher.launch("image/*");
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
//            Uri imageUri = data.getData();
//            if (imageUri != null) {
//                uploadImage(imageUri);
//            }
//        }
//    }

    private Bitmap resizeImage(Bitmap originalBitmap) {
        int width = originalBitmap.getWidth();
        int height = originalBitmap.getHeight();

        float scale = 1.0f;

        // Calculate scale factor if image is larger than MAX_IMAGE_SIZE
        if (width > height && width > MAX_IMAGE_SIZE) {
            scale = (float) MAX_IMAGE_SIZE / width;
        } else if (height > width && height > MAX_IMAGE_SIZE) {
            scale = (float) MAX_IMAGE_SIZE / height;
        }

        // If no resizing is needed, return original
        if (scale == 1.0f) {
            return originalBitmap;
        }

        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);

        return Bitmap.createBitmap(
                originalBitmap,
                0, 0,
                width, height,
                matrix,
                true
        );
    }

    private void uploadImage(Message message) {
        dbr.push().setValue(message);
    }
}