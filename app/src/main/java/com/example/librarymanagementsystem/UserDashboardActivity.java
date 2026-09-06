package com.example.librarymanagementsystem;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class UserDashboardActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private String userEmail;

    private TextView tvWelcome;
    private Button btnLogout, btnMembership;

    // Tab containers + their labels + underlines, so we can style the active one
    private View tabSearch, tabIssue, tabReturn, tabRecords, tabOnline;
    private TextView tvTabSearch, tvTabIssue, tvTabReturn, tvTabRecords, tvTabOnline;
    private View underlineSearch, underlineIssue, underlineReturn, underlineRecords, underlineOnline;

    private static final int COLOR_ACTIVE = 0xFF6F4E37; // brown_primary
    private static final int COLOR_INACTIVE = 0xFF8D6E63; // text_muted

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_dashboard);

        dbHelper = new DatabaseHelper(this);
        userEmail = getIntent().getStringExtra("USER_EMAIL");

        tvWelcome = findViewById(R.id.tvWelcome);
        btnLogout = findViewById(R.id.btnLogout);
        btnMembership = findViewById(R.id.btnMembership);

        tabSearch = findViewById(R.id.tabSearch);
        tabIssue = findViewById(R.id.tabIssue);
        tabReturn = findViewById(R.id.tabReturn);
        tabRecords = findViewById(R.id.tabRecords);
        tabOnline = findViewById(R.id.tabOnline);

        tvTabSearch = findViewById(R.id.tvTabSearch);
        tvTabIssue = findViewById(R.id.tvTabIssue);
        tvTabReturn = findViewById(R.id.tvTabReturn);
        tvTabRecords = findViewById(R.id.tvTabRecords);
        tvTabOnline = findViewById(R.id.tvTabOnline);

        underlineSearch = findViewById(R.id.underlineSearch);
        underlineIssue = findViewById(R.id.underlineIssue);
        underlineReturn = findViewById(R.id.underlineReturn);
        underlineRecords = findViewById(R.id.underlineRecords);
        underlineOnline = findViewById(R.id.underlineOnline);

        loadWelcomeName();

        // Default tab shown on open
        selectTab(0);

        tabSearch.setOnClickListener(v -> selectTab(0));
        tabIssue.setOnClickListener(v -> selectTab(1));
        tabReturn.setOnClickListener(v -> selectTab(2));
        tabRecords.setOnClickListener(v -> selectTab(3));
        tabOnline.setOnClickListener(v -> selectTab(4));

        btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(UserDashboardActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        btnMembership.setOnClickListener(v -> {
            Intent intent = new Intent(UserDashboardActivity.this, MembershipActivity.class);
            intent.putExtra("USER_EMAIL", userEmail);
            startActivity(intent);
        });
    }

    private void loadWelcomeName() {
        Cursor cursor = dbHelper.getUserDetails(userEmail);
        if (cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            tvWelcome.setText("Welcome, " + name);
        }
        cursor.close();
    }

    private void selectTab(int index) {
        // Reset all tabs to inactive style
        tvTabSearch.setTextColor(COLOR_INACTIVE);
        tvTabIssue.setTextColor(COLOR_INACTIVE);
        tvTabReturn.setTextColor(COLOR_INACTIVE);
        tvTabRecords.setTextColor(COLOR_INACTIVE);
        tvTabOnline.setTextColor(COLOR_INACTIVE);
        tvTabSearch.setTypeface(null, android.graphics.Typeface.NORMAL);
        tvTabIssue.setTypeface(null, android.graphics.Typeface.NORMAL);
        tvTabReturn.setTypeface(null, android.graphics.Typeface.NORMAL);
        tvTabRecords.setTypeface(null, android.graphics.Typeface.NORMAL);
        tvTabOnline.setTypeface(null, android.graphics.Typeface.NORMAL);

        underlineSearch.setBackgroundColor(0x00000000);
        underlineIssue.setBackgroundColor(0x00000000);
        underlineReturn.setBackgroundColor(0x00000000);
        underlineRecords.setBackgroundColor(0x00000000);
        underlineOnline.setBackgroundColor(0x00000000);

        Fragment fragment;

        switch (index) {
            case 1:
                tvTabIssue.setTextColor(COLOR_ACTIVE);
                tvTabIssue.setTypeface(null, android.graphics.Typeface.BOLD);
                underlineIssue.setBackgroundColor(COLOR_ACTIVE);
                fragment = new IssueResourceFragment();
                break;
            case 2:
                tvTabReturn.setTextColor(COLOR_ACTIVE);
                tvTabReturn.setTypeface(null, android.graphics.Typeface.BOLD);
                underlineReturn.setBackgroundColor(COLOR_ACTIVE);
                fragment = new ReturnResourceFragment();
                break;
            case 3:
                tvTabRecords.setTextColor(COLOR_ACTIVE);
                tvTabRecords.setTypeface(null, android.graphics.Typeface.BOLD);
                underlineRecords.setBackgroundColor(COLOR_ACTIVE);
                fragment = new MyRecordsFragment();
                break;
            case 4:
                tvTabOnline.setTextColor(COLOR_ACTIVE);
                tvTabOnline.setTypeface(null, android.graphics.Typeface.BOLD);
                underlineOnline.setBackgroundColor(COLOR_ACTIVE);
                fragment = new SearchOnlineFragment();
                break;
            default:
                tvTabSearch.setTextColor(COLOR_ACTIVE);
                tvTabSearch.setTypeface(null, android.graphics.Typeface.BOLD);
                underlineSearch.setBackgroundColor(COLOR_ACTIVE);
                fragment = new SearchResourceFragment();
                break;
        }

        // Pass the logged-in user's email to whichever fragment loads
        Bundle args = new Bundle();
        args.putString("USER_EMAIL", userEmail);
        fragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.commit();
    }
}