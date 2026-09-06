package com.example.librarymanagementsystem;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CardView btnAdmin = findViewById(R.id.btnAdminDomain);
        CardView btnUser = findViewById(R.id.btnUserDomain);

        btnAdmin.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, AdminLoginActivity.class)));
        btnUser.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, LoginActivity.class)));
    }
}