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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class SearchResourceFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private String userEmail;
    private EditText etSearch;
    private RecyclerView rvBooks;
    private BookAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_resource, container, false);

        dbHelper = new DatabaseHelper(requireContext());
        userEmail = getArguments() != null ? getArguments().getString("USER_EMAIL") : null;

        etSearch = view.findViewById(R.id.etSearch);
        rvBooks = view.findViewById(R.id.rvBooks);

        adapter = new BookAdapter((bookId, bookTitle) -> confirmReserve(bookId, bookTitle));
        rvBooks.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvBooks.setAdapter(adapter);

        loadAllBooks();

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (query.isEmpty()) {
                    loadAllBooks();
                } else {
                    Cursor cursor = dbHelper.searchBooks(query);
                    adapter.setBooksFromCursor(cursor);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        return view;
    }

    private void loadAllBooks() {
        Cursor cursor = dbHelper.getAllBooks();
        adapter.setBooksFromCursor(cursor);
    }

    private void confirmReserve(String bookId, String bookTitle) {
        if (userEmail == null) {
            Toast.makeText(requireContext(), "User not found, please log in again.", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Reserve Book")
                .setMessage("Reserve \"" + bookTitle + "\"? We'll hold a copy for you once one becomes available.")
                .setPositiveButton("Reserve", (dialog, which) -> {
                    boolean success = dbHelper.reserveBookById(userEmail, bookId, bookTitle);
                    if (success) {
                        Toast.makeText(requireContext(), "Book reserved! Check My Records for your reservations.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(requireContext(), "Could not reserve book. Try again.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}