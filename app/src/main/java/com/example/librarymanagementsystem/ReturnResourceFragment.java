package com.example.librarymanagementsystem;

import android.app.AlertDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ReturnResourceFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private String userEmail;

    private TextView tvEmptyState;
    private RecyclerView rvReturnBooks;
    private ReturnBookAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_return_resource, container, false);

        dbHelper = new DatabaseHelper(requireContext());
        userEmail = getArguments() != null ? getArguments().getString("USER_EMAIL") : null;

        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        rvReturnBooks = view.findViewById(R.id.rvReturnBooks);

        adapter = new ReturnBookAdapter((issuedBookId, bookId, bookTitle) ->
                confirmReturn(issuedBookId, bookId, bookTitle));

        rvReturnBooks.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvReturnBooks.setAdapter(adapter);

        loadIssuedBooks();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadIssuedBooks(); // refresh every time this tab is shown
    }

    private void loadIssuedBooks() {
        if (userEmail == null) return;

        Cursor cursor = dbHelper.getActiveIssuedBooks(userEmail);
        boolean isEmpty = cursor.getCount() == 0;

        adapter.setItemsFromCursor(cursor); // this also closes the cursor

        tvEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rvReturnBooks.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void confirmReturn(int issuedBookId, String bookId, String bookTitle) {
        // Recalculate fine fresh right before showing the confirmation, so the number is accurate
        double fine = dbHelper.calculateAndUpdateFine(issuedBookId);

        String message = fine > 0
                ? "Return \"" + bookTitle + "\"?\n\nThis book is overdue — a fine of Rs " + fine + " will be added to your account."
                : "Return \"" + bookTitle + "\"?";

        new AlertDialog.Builder(requireContext())
                .setTitle("Return Book")
                .setMessage(message)
                .setPositiveButton("Return", (dialog, which) -> {
                    boolean success = dbHelper.returnBook(issuedBookId, bookId);
                    if (success) {
                        Toast.makeText(requireContext(), "Book returned successfully!", Toast.LENGTH_SHORT).show();
                        loadIssuedBooks();
                    } else {
                        Toast.makeText(requireContext(), "Could not return book. Try again.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}