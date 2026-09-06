package com.example.librarymanagementsystem;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MembershipActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private String userEmail;

    private TextView tvCurrentTier, tvBookLimit, tvLoanDuration, tvFineRate;
    private Button btnUpgradeDowngrade, btnRenew, btnCancelMembership;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_membership);

        dbHelper = new DatabaseHelper(this);
        userEmail = getIntent().getStringExtra("USER_EMAIL");

        tvCurrentTier = findViewById(R.id.tvCurrentTier);
        tvBookLimit = findViewById(R.id.tvBookLimit);
        tvLoanDuration = findViewById(R.id.tvLoanDuration);
        tvFineRate = findViewById(R.id.tvFineRate);
        btnUpgradeDowngrade = findViewById(R.id.btnUpgradeDowngrade);
        btnRenew = findViewById(R.id.btnRenew);
        btnCancelMembership = findViewById(R.id.btnCancelMembership);

        refreshDisplay();

        btnUpgradeDowngrade.setOnClickListener(v -> handleUpgradeDowngrade());
        btnRenew.setOnClickListener(v -> handleRenew());
        btnCancelMembership.setOnClickListener(v -> handleCancel());
    }

    private void refreshDisplay() {
        if (userEmail == null) return;

        String currentTier = dbHelper.getMembershipType(userEmail);
        int loanDays = Membership.getLoanDays(currentTier);
        int bookLimit = Membership.getBookLimit(currentTier);

        tvCurrentTier.setText("Current Plan: " + currentTier);
        tvBookLimit.setText("Book Limit: " + bookLimit + " books at a time");
        tvLoanDuration.setText("Loan Duration: " + loanDays + " days");
        tvFineRate.setText("Fine Rate: Rs " + (int) Membership.FINE_PER_DAY + "/day");

        if (Membership.PREMIUM.equalsIgnoreCase(currentTier)) {
            btnUpgradeDowngrade.setText("Downgrade to Basic");
        } else {
            btnUpgradeDowngrade.setText("Upgrade to Premium");
        }
    }

    private void handleUpgradeDowngrade() {
        String currentTier = dbHelper.getMembershipType(userEmail);
        boolean isCurrentlyPremium = Membership.PREMIUM.equalsIgnoreCase(currentTier);
        String targetTier = isCurrentlyPremium ? Membership.BASIC : Membership.PREMIUM;

        // Going Premium -> Basic reduces the book limit, so make sure they aren't over the new limit
        if (isCurrentlyPremium) {
            int activeCount = dbHelper.getActiveIssuedCount(userEmail);
            int basicLimit = Membership.BASIC_BOOK_LIMIT;

            if (activeCount > basicLimit) {
                new AlertDialog.Builder(this)
                        .setTitle("Cannot Downgrade")
                        .setMessage("You currently have " + activeCount + " books issued, but Basic membership only allows " +
                                basicLimit + ". Please return some books before downgrading.")
                        .setPositiveButton("OK", null)
                        .show();
                return;
            }
        }

        String actionWord = isCurrentlyPremium ? "downgrade to Basic" : "upgrade to Premium";
        new AlertDialog.Builder(this)
                .setTitle(isCurrentlyPremium ? "Downgrade Membership" : "Upgrade Membership")
                .setMessage("Are you sure you want to " + actionWord + "?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    boolean success = dbHelper.updateMembership(userEmail, targetTier);
                    if (success) {
                        Toast.makeText(this, "Membership updated to " + targetTier + "!", Toast.LENGTH_SHORT).show();
                        refreshDisplay();
                    } else {
                        Toast.makeText(this, "Something went wrong. Try again.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void handleRenew() {
        new AlertDialog.Builder(this)
                .setTitle("Renew Membership")
                .setMessage("Renew your current membership for another year?")
                .setPositiveButton("Renew", (dialog, which) -> {
                    // No expiry tracking yet — this just confirms the membership stays active
                    Toast.makeText(this, "Membership renewed successfully!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void handleCancel() {
        new AlertDialog.Builder(this)
                .setTitle("Cancel Membership")
                .setMessage("This will cancel your membership and reset you to Basic tier. Continue?")
                .setPositiveButton("Yes, Cancel", (dialog, which) -> {
                    boolean success = dbHelper.cancelMembership(userEmail);
                    if (success) {
                        Toast.makeText(this, "Membership cancelled. You're now on Basic tier.", Toast.LENGTH_SHORT).show();
                        refreshDisplay();
                    } else {
                        Toast.makeText(this, "Something went wrong. Try again.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }
}