package com.example.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends Activity {
 Button logout;
 Button continu;
 TextView Mname;
 TextView Memail;
 ImageView src;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        continu=findViewById(R.id.continu);
        logout=findViewById(R.id.logout);
        Mname=findViewById(R.id.mName);
        Memail=findViewById(R.id.mEmail);
        src=findViewById(R.id.src);





        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent f=new Intent(MainActivity.this,RegisterActivity.class);
                startActivity(f);
            }
        });



        continu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent f=new Intent(MainActivity.this, Chat.class);
                startActivity(f);
            }
        });
    }
}