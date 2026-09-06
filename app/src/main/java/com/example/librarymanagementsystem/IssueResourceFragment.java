package com.example.librarymanagementsystem;

import android.app.AlertDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class IssueResourceFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private String userEmail;

    private TextView tvQuotaInfo;
    private EditText etSearch;
    private RecyclerView rvIssuableBooks;
    private IssuableBookAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_issue_resource, container, false);

        dbHelper = new DatabaseHelper(requireContext());
        userEmail = getArguments() != null ? getArguments().getString("USER_EMAIL") : null;

        tvQuotaInfo = view.findViewById(R.id.tvQuotaInfo);
        etSearch = view.findViewById(R.id.etSearch);
        rvIssuableBooks = view.findViewById(R.id.rvIssuableBooks);

        adapter = new IssuableBookAdapter((bookId, bookTitle) -> confirmIssue(bookId, bookTitle));
        rvIssuableBooks.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvIssuableBooks.setAdapter(adapter);

        loadQuotaInfo();
        loadBooks("");

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadBooks(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadQuotaInfo();
        loadBooks(etSearch.getText().toString().trim());
    }

    private void loadQuotaInfo() {
        if (userEmail == null) return;
        String membership = dbHelper.getMembershipType(userEmail);
        int remaining = dbHelper.getRemainingBookQuota(userEmail);
        tvQuotaInfo.setText("You can issue " + remaining + " more book(s) (" + membership + " membership)");
    }

    private void loadBooks(String query) {
        Cursor cursor = query.isEmpty()
                ? dbHelper.getAvailableBooks()
                : dbHelper.searchAvailableBooks(query);
        adapter.setBooksFromCursor(cursor);
    }

    private void confirmIssue(String bookId, String bookTitle) {
        if (userEmail == null) {
            Toast.makeText(requireContext(), "User not found, please log in again.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check membership limit BEFORE attempting to issue, so we can show a clear message
        if (!dbHelper.canIssueBook(userEmail)) {
            String membership = dbHelper.getMembershipType(userEmail);
            int limit = Membership.getBookLimit(membership);
            Toast.makeText(requireContext(),
                    "Book limit reached! Your " + membership + " membership allows max " + limit + " books at a time.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Issue Book")
                .setMessage("Issue \"" + bookTitle + "\" to your account?")
                .setPositiveButton("Issue", (dialog, which) -> {
                    boolean success = dbHelper.issueBook(userEmail, bookId);
                    if (success) {
                        Toast.makeText(requireContext(), "Book issued successfully!", Toast.LENGTH_SHORT).show();
                        loadQuotaInfo();
                        loadBooks(etSearch.getText().toString().trim());
                    } else {
                        Toast.makeText(requireContext(), "Could not issue book. It may no longer be available.", Toast.LENGTH_SHORT).show();
                        loadBooks(etSearch.getText().toString().trim());
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}