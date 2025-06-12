package com.example.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

public class MainActivity extends Activity {
 Button logout;
 Button continu;
 TextView Mname;
 TextView Memail;
 ImageView src;
FirebaseAuth fAuth;
FirebaseFirestore firestore;
String userID;

//المضاف
private static final int REQUEST_CAMERA = 100;
    private static final int REQUEST_GALLERY = 200;
    private Uri imageUri;
    ImageView profileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//المضاف
        profileImage = findViewById(R.id.src);

        profileImage.setOnClickListener(view -> showImagePickerDialog());
        //انتهينا

        continu=findViewById(R.id.continu);
        logout=findViewById(R.id.logout);
        Mname=findViewById(R.id.mName);
        Memail=findViewById(R.id.mEmail);
        src=findViewById(R.id.src);
         fAuth=FirebaseAuth.getInstance();
         firestore=FirebaseFirestore.getInstance();
        userID = fAuth.getCurrentUser().getUid();


        DocumentReference documentReference=firestore.collection("users").document(userID);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {
                if(documentSnapshot.exists()){
                 Mname.setText(documentSnapshot.getString("fName"));
                 Memail.setText(documentSnapshot.getString("email"));
                }else{
                    Log.d("tag" ,"onEvent:Document do not exists ");
                }
            }
        });



        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();//logout
                Intent f=new Intent(MainActivity.this,RegisterActivity.class);
                startActivity(f);
                finish();
            }
        });



        continu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent f=new Intent(MainActivity.this, Chat.class);
                startActivity(f);
                finish();
            }
        });
    }


//مضاف
private void showImagePickerDialog() {
    String[] options = {"Choose from gallery", "Take a photo"};
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle("Choose a picture");
    builder.setItems(options, (dialog, which) -> {
        if (which == 0) {
            // اختر من المعرض
            Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(galleryIntent, REQUEST_GALLERY);
        } else {
            // افتح الكاميرا
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE, "New Picture");
            values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera");
            imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(cameraIntent, REQUEST_CAMERA);
        }
    });
    builder.show();
}

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_GALLERY && data != null) {
                Uri selectedImage = data.getData();
                profileImage.setImageURI(selectedImage);
            } else if (requestCode == REQUEST_CAMERA) {
                profileImage.setImageURI(imageUri);
            }
        }
    }
    //انتهينا

}