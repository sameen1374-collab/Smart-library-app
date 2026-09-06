package com.example.librarymanagementsystem;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;

public class AdminDashboardActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;

    // Tab switching
    private TabLayout adminTabLayout;
    private View containerViewAll, containerAddResource, containerManageUsers,
            containerIssuedResources, containerStudentRequests;

    // Tab 0: View All Resources
    private RecyclerView rvAllBooks;
    private TextView tvNoBooks;
    private AdminBookAdapter bookAdapter;

    // Tab 1: Add Resource form
    private EditText etResourceId, etTitle, etAuthor, etCategory, etQuantity;
    private Button btnAddResource, btnCancel;

    // Tab 2: Manage Users
    private EditText etSearchUser;
    private RecyclerView rvAllUsers;
    private TextView tvNoUsers;
    private AdminUserAdapter userAdapter;

    // Tab 3: Issued Resources
    private RecyclerView rvIssuedResources;
    private TextView tvNoIssued;
    private AdminIssuedAdapter issuedAdapter;

    // Tab 4: Student Requests
    private RecyclerView rvStudentRequests;
    private TextView tvNoRequests;
    private AdminRequestAdapter requestAdapter;

    private Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        dbHelper = new DatabaseHelper(this);

        bindViews();
        setupAddResourceForm();
        setupBooksList();
        setupUsersList();
        setupIssuedList();
        setupRequestsList();
        setupTabSwitching();

        btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        loadBooks();
        loadUsers("");
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBooks();
        loadUsers(etSearchUser.getText().toString().trim());
        loadIssuedResources();
        loadRequests();
    }

    private void bindViews() {
        adminTabLayout = findViewById(R.id.adminTabLayout);
        containerViewAll = findViewById(R.id.containerViewAll);
        containerAddResource = findViewById(R.id.containerAddResource);
        containerManageUsers = findViewById(R.id.containerManageUsers);
        containerIssuedResources = findViewById(R.id.containerIssuedResources);
        containerStudentRequests = findViewById(R.id.containerStudentRequests);

        rvAllBooks = findViewById(R.id.rvAllBooks);
        tvNoBooks = findViewById(R.id.tvNoBooks);

        etResourceId = findViewById(R.id.etResourceId);
        etTitle = findViewById(R.id.etTitle);
        etAuthor = findViewById(R.id.etAuthor);
        etCategory = findViewById(R.id.etCategory);
        etQuantity = findViewById(R.id.etQuantity);
        btnAddResource = findViewById(R.id.btnAddResource);
        btnCancel = findViewById(R.id.btnCancel);

        etSearchUser = findViewById(R.id.etSearchUser);
        rvAllUsers = findViewById(R.id.rvAllUsers);
        tvNoUsers = findViewById(R.id.tvNoUsers);

        rvIssuedResources = findViewById(R.id.rvIssuedResources);
        tvNoIssued = findViewById(R.id.tvNoIssued);

        rvStudentRequests = findViewById(R.id.rvStudentRequests);
        tvNoRequests = findViewById(R.id.tvNoRequests);

        btnLogout = findViewById(R.id.btnLogout);
    }

    private void setupTabSwitching() {
        adminTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                showContainer(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        showContainer(0); // default tab on open
    }

    private void showContainer(int position) {
        containerViewAll.setVisibility(position == 0 ? View.VISIBLE : View.GONE);
        containerAddResource.setVisibility(position == 1 ? View.VISIBLE : View.GONE);
        containerManageUsers.setVisibility(position == 2 ? View.VISIBLE : View.GONE);
        containerIssuedResources.setVisibility(position == 3 ? View.VISIBLE : View.GONE);
        containerStudentRequests.setVisibility(position == 4 ? View.VISIBLE : View.GONE);

        if (position == 0) loadBooks();
        if (position == 2) loadUsers(etSearchUser.getText().toString().trim());
        if (position == 3) loadIssuedResources();
        if (position == 4) loadRequests();
    }

    // ---------- TAB 0: View All Resources ----------

    private void setupBooksList() {
        bookAdapter = new AdminBookAdapter(
                (bookId, bookTitle) -> confirmDeleteBook(bookId, bookTitle),
                (book) -> showEditBookDialog(book)
        );
        rvAllBooks.setLayoutManager(new LinearLayoutManager(this));
        rvAllBooks.setAdapter(bookAdapter);
    }

    private void loadBooks() {
        Cursor cursor = dbHelper.getAllBooks();
        boolean isEmpty = cursor.getCount() == 0;
        bookAdapter.setBooksFromCursor(cursor);

        tvNoBooks.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rvAllBooks.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void confirmDeleteBook(String bookId, String bookTitle) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Resource")
                .setMessage("Delete \"" + bookTitle + "\" from the library? This cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    boolean success = dbHelper.removeResource(bookId);
                    if (success) {
                        Toast.makeText(this, "Resource deleted.", Toast.LENGTH_SHORT).show();
                        loadBooks();
                    } else {
                        Toast.makeText(this, "Could not delete. Try again.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEditBookDialog(AdminBookAdapter.BookItem book) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_book, null);

        EditText etEditTitle = dialogView.findViewById(R.id.etEditTitle);
        EditText etEditAuthor = dialogView.findViewById(R.id.etEditAuthor);
        EditText etEditCategory = dialogView.findViewById(R.id.etEditCategory);
        EditText etEditQuantity = dialogView.findViewById(R.id.etEditQuantity);

        etEditTitle.setText(book.title);
        etEditAuthor.setText(book.author);
        etEditCategory.setText(book.category);
        etEditQuantity.setText(String.valueOf(book.totalQty));

        new AlertDialog.Builder(this)
                .setTitle("Edit Resource (ID: " + book.id + ")")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newTitle = etEditTitle.getText().toString().trim();
                    String newAuthor = etEditAuthor.getText().toString().trim();
                    String newCategory = etEditCategory.getText().toString().trim();
                    String newQtyStr = etEditQuantity.getText().toString().trim();

                    if (newTitle.isEmpty() || newAuthor.isEmpty() || newCategory.isEmpty() || newQtyStr.isEmpty()) {
                        Toast.makeText(this, "Please fill all fields.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int newQty;
                    try {
                        newQty = Integer.parseInt(newQtyStr);
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Enter a valid quantity.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int issuedCount = dbHelper.getIssuedCountForBook(book.id);
                    if (newQty < issuedCount) {
                        Toast.makeText(this,
                                "Can't set quantity below " + issuedCount + " — that many copies are currently issued out.",
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    boolean success = dbHelper.updateResource(book.id, newTitle, newAuthor, newCategory, newQty);
                    if (success) {
                        Toast.makeText(this, "Resource updated!", Toast.LENGTH_SHORT).show();
                        loadBooks();
                    } else {
                        Toast.makeText(this, "Could not update. Try again.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ---------- TAB 1: Add Resource ----------

    private void setupAddResourceForm() {
        btnAddResource.setOnClickListener(v -> handleAddResource());
        btnCancel.setOnClickListener(v -> clearForm());
    }

    private void handleAddResource() {
        String bookId = etResourceId.getText().toString().trim();
        String title = etTitle.getText().toString().trim();
        String author = etAuthor.getText().toString().trim();
        String category = etCategory.getText().toString().trim();
        String quantityStr = etQuantity.getText().toString().trim();

        if (bookId.isEmpty() || title.isEmpty() || author.isEmpty() || category.isEmpty() || quantityStr.isEmpty()) {
            Toast.makeText(this, "Please fill all fields!", Toast.LENGTH_SHORT).show();
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(quantityStr);
        } catch (NumberFormatException e) {
            etQuantity.setError("Enter a valid number");
            etQuantity.requestFocus();
            return;
        }

        if (quantity <= 0) {
            etQuantity.setError("Quantity must be at least 1");
            etQuantity.requestFocus();
            return;
        }

        boolean success = dbHelper.addResource(bookId, title, author, category, quantity);

        if (success) {
            Toast.makeText(this, "Resource added successfully!", Toast.LENGTH_SHORT).show();
            clearForm();
            loadBooks();
        } else {
            Toast.makeText(this, "Failed to add resource. Try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearForm() {
        etResourceId.setText("");
        etTitle.setText("");
        etAuthor.setText("");
        etCategory.setText("");
        etQuantity.setText("");
    }

    // ---------- TAB 2: Manage Users ----------

    private void setupUsersList() {
        userAdapter = new AdminUserAdapter(
                (email, name) -> confirmDeleteUser(email, name),
                (email, name) -> confirmCancelMembership(email, name)
        );
        rvAllUsers.setLayoutManager(new LinearLayoutManager(this));
        rvAllUsers.setAdapter(userAdapter);

        etSearchUser.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadUsers(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadUsers(String query) {
        Cursor cursor = query.isEmpty() ? dbHelper.getAllUsers() : dbHelper.searchUsers(query);
        boolean isEmpty = cursor.getCount() == 0;
        userAdapter.setUsersFromCursor(cursor);

        tvNoUsers.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rvAllUsers.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void confirmDeleteUser(String email, String name) {
        new AlertDialog.Builder(this)
                .setTitle("Delete User")
                .setMessage("Delete \"" + name + "\"'s account? This cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    boolean success = dbHelper.deleteUser(email);
                    if (success) {
                        Toast.makeText(this, "User deleted.", Toast.LENGTH_SHORT).show();
                        loadUsers(etSearchUser.getText().toString().trim());
                    } else {
                        Toast.makeText(this, "Could not delete. Try again.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmCancelMembership(String email, String name) {
        new AlertDialog.Builder(this)
                .setTitle("Cancel Membership")
                .setMessage("Reset \"" + name + "\"'s membership to Basic tier?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    boolean success = dbHelper.cancelMembership(email);
                    if (success) {
                        Toast.makeText(this, "Membership reset to Basic.", Toast.LENGTH_SHORT).show();
                        loadUsers(etSearchUser.getText().toString().trim());
                    } else {
                        Toast.makeText(this, "Could not update. Try again.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    // ---------- TAB 3: Issued Resources ----------

    private void setupIssuedList() {
        issuedAdapter = new AdminIssuedAdapter();
        rvIssuedResources.setLayoutManager(new LinearLayoutManager(this));
        rvIssuedResources.setAdapter(issuedAdapter);
    }

    private void loadIssuedResources() {
        Cursor cursor = dbHelper.getAllActiveIssuedBooks();
        boolean isEmpty = cursor.getCount() == 0;
        issuedAdapter.setItemsFromCursor(cursor);

        tvNoIssued.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rvIssuedResources.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    // ---------- TAB 4: Student Requests ----------

    private void setupRequestsList() {
        requestAdapter = new AdminRequestAdapter(new AdminRequestAdapter.OnActionClickListener() {
            @Override
            public void onAccept(int requestId, String bookTitle) {
                boolean success = dbHelper.acceptRequest(requestId);
                if (success) {
                    Toast.makeText(AdminDashboardActivity.this,
                            "Request accepted. Remember to add \"" + bookTitle + "\" via Add Resource.",
                            Toast.LENGTH_LONG).show();
                    loadRequests();
                }
            }

            @Override
            public void onReject(int requestId, String bookTitle) {
                boolean success = dbHelper.rejectRequest(requestId);
                if (success) {
                    Toast.makeText(AdminDashboardActivity.this, "Request rejected.", Toast.LENGTH_SHORT).show();
                    loadRequests();
                }
            }
        });
        rvStudentRequests.setLayoutManager(new LinearLayoutManager(this));
        rvStudentRequests.setAdapter(requestAdapter);
    }

    private void loadRequests() {
        Cursor cursor = dbHelper.getPendingRequests();
        boolean isEmpty = cursor.getCount() == 0;
        requestAdapter.setRequestsFromCursor(cursor);

        tvNoRequests.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rvStudentRequests.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }
}