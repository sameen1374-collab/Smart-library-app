package com.example.librarymanagementsystem;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class AdminLoginActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        EditText etPass = findViewById(R.id.etAdminPassword);
        Button btnVerify = findViewById(R.id.btnAdminLogin);

        btnVerify.setOnClickListener(v -> {
            if (etPass.getText().toString().equals("admin123")) {
                startActivity(new Intent(AdminLoginActivity.this, AdminDashboardActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Access Denied! Wrong Code.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}