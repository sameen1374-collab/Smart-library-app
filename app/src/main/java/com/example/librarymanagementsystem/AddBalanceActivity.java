package com.example.librarymanagementsystem;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AddBalanceActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private String userEmail;

    private EditText etAmount, etEasyPaisaNumber, etCardNumber, etExpiry, etCvv;
    private RadioGroup rgPaymentMethod;
    private LinearLayout layoutEasyPaisa, layoutCard;
    private Button btnConfirmPayment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_balance);

        dbHelper = new DatabaseHelper(this);
        userEmail = getIntent().getStringExtra("USER_EMAIL");

        etAmount = findViewById(R.id.etAmount);
        etEasyPaisaNumber = findViewById(R.id.etEasyPaisaNumber);
        etCardNumber = findViewById(R.id.etCardNumber);
        etExpiry = findViewById(R.id.etExpiry);
        etCvv = findViewById(R.id.etCvv);
        rgPaymentMethod = findViewById(R.id.rgPaymentMethod);
        layoutEasyPaisa = findViewById(R.id.layoutEasyPaisa);
        layoutCard = findViewById(R.id.layoutCard);
        btnConfirmPayment = findViewById(R.id.btnConfirmPayment);

        // Toggle which fields show based on selected payment method
        rgPaymentMethod.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbEasyPaisa) {
                layoutEasyPaisa.setVisibility(android.view.View.VISIBLE);
                layoutCard.setVisibility(android.view.View.GONE);
            } else {
                layoutEasyPaisa.setVisibility(android.view.View.GONE);
                layoutCard.setVisibility(android.view.View.VISIBLE);
            }
        });

        btnConfirmPayment.setOnClickListener(v -> handleAddBalance());
    }

    private void handleAddBalance() {
        String amountStr = etAmount.getText().toString().trim();

        if (amountStr.isEmpty()) {
            etAmount.setError("Enter an amount");
            etAmount.requestFocus();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            etAmount.setError("Enter a valid number");
            etAmount.requestFocus();
            return;
        }

        if (amount <= 0) {
            etAmount.setError("Amount must be greater than 0");
            etAmount.requestFocus();
            return;
        }

        // Validate the demo payment fields based on selected method
        if (rgPaymentMethod.getCheckedRadioButtonId() == R.id.rbEasyPaisa) {
            String number = etEasyPaisaNumber.getText().toString().trim();
            if (number.isEmpty() || number.length() < 10) {
                etEasyPaisaNumber.setError("Enter a valid mobile number");
                etEasyPaisaNumber.requestFocus();
                return;
            }
        } else {
            String cardNumber = etCardNumber.getText().toString().trim();
            String expiry = etExpiry.getText().toString().trim();
            String cvv = etCvv.getText().toString().trim();

            if (cardNumber.length() < 12) {
                etCardNumber.setError("Enter a valid card number");
                etCardNumber.requestFocus();
                return;
            }
            if (expiry.isEmpty()) {
                etExpiry.setError("Enter expiry date");
                etExpiry.requestFocus();
                return;
            }
            if (cvv.length() < 3) {
                etCvv.setError("Enter a valid CVV");
                etCvv.requestFocus();
                return;
            }
        }

        // All good — this is a demo, so we just simulate a successful payment
        showProcessingThenSuccess(amount);
    }

    private void showProcessingThenSuccess(double amount) {
        btnConfirmPayment.setEnabled(false);
        btnConfirmPayment.setText("Processing...");

        // Small fake delay so it feels like a real transaction, then credit the wallet
        btnConfirmPayment.postDelayed(() -> {
            boolean success = dbHelper.addBalance(userEmail, amount);

            if (success) {
                new AlertDialog.Builder(this)
                        .setTitle("Payment Successful")
                        .setMessage("Rs " + amount + " has been added to your wallet.")
                        .setPositiveButton("OK", (dialog, which) -> finish())
                        .setCancelable(false)
                        .show();
            } else {
                Toast.makeText(this, "Something went wrong. Try again.", Toast.LENGTH_SHORT).show();
                btnConfirmPayment.setEnabled(true);
                btnConfirmPayment.setText("Add Balance");
            }
        }, 1200);
    }
}