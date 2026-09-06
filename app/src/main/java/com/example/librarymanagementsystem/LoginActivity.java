package com.example.librarymanagementsystem;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPassword;
    private Button btnMainAction;
    private TextView tvToggleMode, tvForgotPassword;
    private TextView tabLogin, tabSignup;
    private DatabaseHelper dbHelper;
    private FirebaseAuth mAuth;
    private boolean isSignUpMode = false;

    // Password Policy Regex: Min 8 chars, 1 Upper, 1 Lower, 1 Digit, 1 Special Char
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!_])(?=\\S+$).{8,}$");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbHelper = new DatabaseHelper(this);
        mAuth = FirebaseAuth.getInstance();

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnMainAction = findViewById(R.id.btnMainAction);
        tvToggleMode = findViewById(R.id.tvToggleMode);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tabLogin = findViewById(R.id.tabLogin);
        tabSignup = findViewById(R.id.tabSignup);

        tabLogin.setOnClickListener(v -> switchToSignIn());
        tabSignup.setOnClickListener(v -> switchToSignUp());

        tvForgotPassword.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class)));

        btnMainAction.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.setError("Please enter a valid email address!");
                etEmail.requestFocus();
                return;
            }

            if (isSignUpMode) {
                handleSignUp(email, password);
            } else {
                handleSignIn(email, password);
            }
        });
    }

    private void handleSignUp(String email, String password) {
        String name = etName.getText().toString().trim();
        if (name.isEmpty()) {
            etName.setError("Name is required!");
            etName.requestFocus();
            return;
        }

        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            etPassword.setError("Password must contain:\n- Min 8 chars\n- 1 Uppercase (A-Z)\n- 1 Lowercase (a-z)\n- 1 Digit (0-9)\n- 1 Special Char (@,#,$,etc)");
            etPassword.requestFocus();
            return;
        }

        btnMainAction.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    btnMainAction.setEnabled(true);

                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            firebaseUser.sendEmailVerification();
                        }

                        // Keep a local profile (membership, wallet, etc.) tied to this email
                        dbHelper.registerUser(name, email, password);

                        Toast.makeText(this,
                                "Account created! Please check your email to verify your account before signing in.",
                                Toast.LENGTH_LONG).show();

                        mAuth.signOut(); // don't let them in until verified
                        switchToSignIn();
                    } else {
                        String errorMsg = task.getException() != null
                                ? task.getException().getMessage()
                                : "Registration failed. Try again.";
                        Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void handleSignIn(String email, String password) {
        btnMainAction.setEnabled(false);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    btnMainAction.setEnabled(true);

                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();

                        if (firebaseUser != null && firebaseUser.isEmailVerified()) {
                            Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginActivity.this, UserDashboardActivity.class);
                            intent.putExtra("USER_EMAIL", email);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(this,
                                    "Please verify your email first. Check your inbox for the verification link.",
                                    Toast.LENGTH_LONG).show();
                            mAuth.signOut();
                        }
                    } else {
                        Toast.makeText(this, "Invalid email or password.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void switchToSignIn() {
        isSignUpMode = false;
        etName.setVisibility(View.GONE);
        btnMainAction.setText("LOGIN");
        tvForgotPassword.setVisibility(View.VISIBLE);

        tabLogin.setBackgroundResource(R.drawable.bg_tab_selected);
        tabLogin.setTextColor(getResources().getColor(R.color.text_white));
        tabSignup.setBackground(null);
        tabSignup.setTextColor(getResources().getColor(R.color.text_muted));
    }

    private void switchToSignUp() {
        isSignUpMode = true;
        etName.setVisibility(View.VISIBLE);
        btnMainAction.setText("SIGN UP");
        tvForgotPassword.setVisibility(View.GONE);

        tabSignup.setBackgroundResource(R.drawable.bg_tab_selected);
        tabSignup.setTextColor(getResources().getColor(R.color.text_white));
        tabLogin.setBackground(null);
        tabLogin.setTextColor(getResources().getColor(R.color.text_muted));
    }
}