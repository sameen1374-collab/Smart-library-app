package com.example.librarymanagementsystem;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText etResetEmail;
    private Button btnSendReset;
    private TextView tvBackToLogin;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        mAuth = FirebaseAuth.getInstance();

        etResetEmail = findViewById(R.id.etResetEmail);
        btnSendReset = findViewById(R.id.btnSendReset);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);

        btnSendReset.setOnClickListener(v -> handleSendReset());
        tvBackToLogin.setOnClickListener(v -> finish());
    }

    private void handleSendReset() {
        String email = etResetEmail.getText().toString().trim();

        if (email.isEmpty()) {
            etResetEmail.setError("Enter your email");
            etResetEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etResetEmail.setError("Enter a valid email address");
            etResetEmail.requestFocus();
            return;
        }

        btnSendReset.setEnabled(false);

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    btnSendReset.setEnabled(true);

                    if (task.isSuccessful()) {
                        Toast.makeText(this,
                                "Reset link sent! Check your email inbox (and spam folder).",
                                Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        Toast.makeText(this,
                                "Could not send reset link. Make sure this email is registered.",
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}