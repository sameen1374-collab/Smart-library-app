package com.example.librarymanagementsystem;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "Library.db";
    // Bumped to 5: added total_quantity/available_quantity to books table
    private static final int DATABASE_VERSION = 5;

    // Consistent date format used everywhere (matches "8/3/2026" style from your screenshots)
    public static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("M/d/yyyy", Locale.US);

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE users (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, email TEXT UNIQUE, password TEXT, role TEXT, membership TEXT DEFAULT 'Basic', wallet REAL DEFAULT 0.0)");
        db.execSQL("CREATE TABLE books (id INTEGER PRIMARY KEY, title TEXT, author TEXT, category TEXT, status TEXT DEFAULT 'Available', total_quantity INTEGER DEFAULT 1, available_quantity INTEGER DEFAULT 1)");
        db.execSQL("CREATE TABLE issued_books (id INTEGER PRIMARY KEY AUTOINCREMENT, user_email TEXT, book_id TEXT, book_title TEXT, author TEXT, issue_date TEXT, due_date TEXT, return_date TEXT, status TEXT DEFAULT 'Active', fine REAL DEFAULT 0.0)");
        db.execSQL("CREATE TABLE favourites (id INTEGER PRIMARY KEY AUTOINCREMENT, user_email TEXT, book_id TEXT)");
        db.execSQL("CREATE TABLE book_requests (id INTEGER PRIMARY KEY AUTOINCREMENT, user_email TEXT, book_title TEXT, status TEXT DEFAULT 'Pending')");
        // Dedicated table for Reserve Book / My Reservations / Cancel Reservation
        db.execSQL("CREATE TABLE reservations (id INTEGER PRIMARY KEY AUTOINCREMENT, user_email TEXT, book_id TEXT, book_title TEXT, reservation_date TEXT, status TEXT DEFAULT 'Pending')");

        // Insert Initial Sample Data matching screenshots
        insertSampleData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS users");
        db.execSQL("DROP TABLE IF EXISTS books");
        db.execSQL("DROP TABLE IF EXISTS issued_books");
        db.execSQL("DROP TABLE IF EXISTS favourites");
        db.execSQL("DROP TABLE IF EXISTS book_requests");
        db.execSQL("DROP TABLE IF EXISTS reservations");
        onCreate(db);
    }

    private void insertSampleData(SQLiteDatabase db) {
        // Sample Books matching screenshot (ID: 101, 102, 103), each with 3 copies to start
        db.execSQL("INSERT OR IGNORE INTO books (id, title, author, category, status, total_quantity, available_quantity) VALUES (101, 'The Great Gatsby', 'F. Scott Fitzgerald', 'Fiction', 'Available', 3, 2)");
        db.execSQL("INSERT OR IGNORE INTO books (id, title, author, category, status, total_quantity, available_quantity) VALUES (102, 'Clean Code', 'Robert C. Martin', 'Technology', 'Available', 3, 3)");
        db.execSQL("INSERT OR IGNORE INTO books (id, title, author, category, status, total_quantity, available_quantity) VALUES (103, '1984', 'George Orwell', 'Fiction', 'Available', 3, 3)");

        // Sample Issued Books / My Records matching screenshot
        db.execSQL("INSERT INTO issued_books (user_email, book_id, book_title, author, issue_date, due_date, return_date, status, fine) " +
                "VALUES ('sameen@gmail.com', '101', 'The Great Gatsby', 'F. Scott Fitzgerald', '8/3/2026', '8/17/2026', 'Not returned', 'Active', 0.0)");

        db.execSQL("INSERT INTO issued_books (user_email, book_id, book_title, author, issue_date, due_date, return_date, status, fine) " +
                "VALUES ('sameen@gmail.com', '103', '1984', 'George Orwell', '8/3/2026', '8/17/2026', '8/3/2026', 'Returned', 0.0)");
    }

    // --- Authentication & Users ---
    public boolean registerUser(String name, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("email", email);
        values.put("password", password);
        values.put("role", "User");
        values.put("membership", Membership.BASIC);
        values.put("wallet", 0.0);
        long result = db.insert("users", null, values);
        return result != -1;
    }

    public boolean checkUserCredentials(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM users WHERE email = ? AND password = ?", new String[]{email, password});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public Cursor getUserDetails(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM users WHERE email = ?", new String[]{email});
    }

    // Recalculates fine on every active issued book, then returns the total
    public double getUserFineBalance(String userEmail) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id FROM issued_books WHERE user_email = ? AND status = 'Active'",
                new String[]{userEmail});

        double total = 0.0;
        while (cursor.moveToNext()) {
            int id = cursor.getInt(0);
            total += calculateAndUpdateFine(id);
        }
        cursor.close();
        return total;
    }

    // --- Book Operations ---
    public Cursor getAvailableBooks() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM books WHERE available_quantity > 0", null);
    }

    // Returns ALL books regardless of availability (useful for Admin's "View All Resources")
    public Cursor getAllBooks() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM books", null);
    }

    public Cursor searchBooks(String query) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM books WHERE title LIKE ? OR author LIKE ? OR CAST(id AS TEXT) LIKE ?",
                new String[]{"%" + query + "%", "%" + query + "%", "%" + query + "%"});
    }

    // Same as searchBooks, but only returns books with copies available (used on the Issue screen)
    public Cursor searchAvailableBooks(String query) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM books WHERE available_quantity > 0 AND (title LIKE ? OR author LIKE ? OR CAST(id AS TEXT) LIKE ?)",
                new String[]{"%" + query + "%", "%" + query + "%", "%" + query + "%"});
    }

    // --- Admin: Add / Update Resource with Quantity ---

    /**
     * Adds a new book, or if the ID already exists, increases its quantity instead
     * (so admin can "restock" an existing title by adding it again).
     */
    public boolean addResource(String bookId, String title, String author, String category, int quantity) {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery("SELECT id FROM books WHERE id = ?", new String[]{bookId});
        boolean exists = cursor.moveToFirst();
        cursor.close();

        if (exists) {
            // Book already exists: bump both total and available quantity
            db.execSQL("UPDATE books SET total_quantity = total_quantity + ?, available_quantity = available_quantity + ?, status = 'Available' WHERE id = ?",
                    new Object[]{quantity, quantity, bookId});
            return true;
        } else {
            ContentValues values = new ContentValues();
            values.put("id", bookId);
            values.put("title", title);
            values.put("author", author);
            values.put("category", category);
            values.put("status", "Available");
            values.put("total_quantity", quantity);
            values.put("available_quantity", quantity);
            long result = db.insert("books", null, values);
            return result != -1;
        }
    }

    public boolean removeResource(String bookId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete("books", "id = ?", new String[]{bookId});
        return rows > 0;
    }

    // How many copies of this book are currently issued out (needed before shrinking quantity)
    public int getIssuedCountForBook(String bookId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM issued_books WHERE book_id = ? AND status = 'Active'",
                new String[]{bookId});
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    /**
     * Updates a book's details. New total quantity can't go below how many copies
     * are currently issued out — check getIssuedCountForBook() before calling this.
     */
    public boolean updateResource(String bookId, String title, String author, String category, int newTotalQty) {
        int issuedCount = getIssuedCountForBook(bookId);
        if (newTotalQty < issuedCount) {
            return false; // caller should validate this first and show a proper message
        }

        int newAvailableQty = newTotalQty - issuedCount;

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("author", author);
        values.put("category", category);
        values.put("total_quantity", newTotalQty);
        values.put("available_quantity", newAvailableQty);
        values.put("status", newAvailableQty > 0 ? "Available" : "Issued");

        int rows = db.update("books", values, "id = ?", new String[]{bookId});
        return rows > 0;
    }

    // --- Admin: Manage Users ---
    public Cursor getAllUsers() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM users WHERE role = 'User'", null);
    }

    public Cursor searchUsers(String query) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM users WHERE role = 'User' AND (name LIKE ? OR email LIKE ?)",
                new String[]{"%" + query + "%", "%" + query + "%"});
    }

    public boolean deleteUser(String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete("users", "email = ?", new String[]{email});
        return rows > 0;
    }

    // --- Admin: View All Issued Resources ---
    public Cursor getAllActiveIssuedBooks() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM issued_books WHERE status = 'Active' ORDER BY due_date ASC", null);
    }

    // --- Admin: Student Requests (from the "Search Online + Request" feature) ---
    public Cursor getPendingRequests() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM book_requests WHERE status = 'Pending'", null);
    }

    public boolean acceptRequest(int requestId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("status", "Accepted");
        int rows = db.update("book_requests", values, "id = ?", new String[]{String.valueOf(requestId)});
        return rows > 0;
    }

    public boolean rejectRequest(int requestId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("status", "Rejected");
        int rows = db.update("book_requests", values, "id = ?", new String[]{String.valueOf(requestId)});
        return rows > 0;
    }

    // --- Membership helpers ---
    public String getMembershipType(String userEmail) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT membership FROM users WHERE email = ?", new String[]{userEmail});
        String membership = Membership.BASIC;
        if (cursor.moveToFirst()) {
            membership = cursor.getString(cursor.getColumnIndexOrThrow("membership"));
        }
        cursor.close();
        return membership;
    }

    public boolean updateMembership(String userEmail, String newType) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("membership", newType);
        int rows = db.update("users", values, "email = ?", new String[]{userEmail});
        return rows > 0;
    }

    public boolean cancelMembership(String userEmail) {
        // Cancelling resets them back to Basic tier
        return updateMembership(userEmail, Membership.BASIC);
    }

    // How many books this user currently has issued (not yet returned)
    public int getActiveIssuedCount(String userEmail) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM issued_books WHERE user_email = ? AND status = 'Active'",
                new String[]{userEmail});
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    // Call this BEFORE issueBook() so the UI can show "limit reached" nicely
    public boolean canIssueBook(String userEmail) {
        String membership = getMembershipType(userEmail);
        int limit = Membership.getBookLimit(membership);
        return getActiveIssuedCount(userEmail) < limit;
    }

    public int getRemainingBookQuota(String userEmail) {
        String membership = getMembershipType(userEmail);
        int limit = Membership.getBookLimit(membership);
        return limit - getActiveIssuedCount(userEmail);
    }

    // --- Issue & Return Logic (membership-aware) ---
    public boolean issueBook(String userEmail, String bookId) {
        if (!canIssueBook(userEmail)) {
            return false; // Activity should call canIssueBook() first to show a proper message
        }

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT title, author, available_quantity FROM books WHERE id = ?", new String[]{bookId});

        if (cursor != null && cursor.moveToFirst()) {
            String title = cursor.getString(0);
            String author = cursor.getString(1);
            int availableQty = cursor.getInt(2);
            cursor.close();

            if (availableQty <= 0) {
                return false; // no copies left, nothing to issue
            }

            String membership = getMembershipType(userEmail);
            String issueDate = DATE_FORMAT.format(new Date());
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, Membership.getLoanDays(membership));
            String dueDate = DATE_FORMAT.format(cal.getTime());

            ContentValues values = new ContentValues();
            values.put("user_email", userEmail);
            values.put("book_id", bookId);
            values.put("book_title", title);
            values.put("author", author);
            values.put("issue_date", issueDate);
            values.put("due_date", dueDate);
            values.put("return_date", "Not returned");
            values.put("status", "Active");
            values.put("fine", 0.0);

            long result = db.insert("issued_books", null, values);

            if (result != -1) {
                // One less copy available; mark fully "Issued" only if that was the last copy
                db.execSQL("UPDATE books SET available_quantity = available_quantity - 1, " +
                                "status = CASE WHEN available_quantity - 1 <= 0 THEN 'Issued' ELSE 'Available' END " +
                                "WHERE id = ?",
                        new Object[]{bookId});
                return true;
            }
        }
        return false;
    }

    public Cursor getActiveIssuedBooks(String userEmail) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM issued_books WHERE user_email = ? AND status = 'Active'", new String[]{userEmail});
    }

    public boolean returnBook(int reqId, String bookId) {
        // Make sure the fine is calculated/saved before marking as returned
        calculateAndUpdateFine(reqId);

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("status", "Returned");
        values.put("return_date", DATE_FORMAT.format(new Date()));
        int rows = db.update("issued_books", values, "id = ?", new String[]{String.valueOf(reqId)});

        // One more copy becomes available; status goes back to 'Available'
        db.execSQL("UPDATE books SET available_quantity = available_quantity + 1, status = 'Available' WHERE id = ?",
                new Object[]{bookId});

        // Check if someone is waiting for this exact book — if so, auto-issue it to them
        fulfillNextReservation(bookId);

        return rows > 0;
    }

    /**
     * Looks for the oldest pending reservation on this book and, if that user is still
     * within their membership's book limit, automatically issues the book to them.
     * If they're over their limit, their reservation stays pending and we don't touch it.
     */
    private void fulfillNextReservation(String bookId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT id, user_email FROM reservations WHERE book_id = ? AND status = 'Pending' ORDER BY reservation_date ASC LIMIT 1",
                new String[]{bookId});

        if (cursor.moveToFirst()) {
            int reservationId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            String reservedByEmail = cursor.getString(cursor.getColumnIndexOrThrow("user_email"));
            cursor.close();

            if (canIssueBook(reservedByEmail)) {
                boolean issued = issueBook(reservedByEmail, bookId);
                if (issued) {
                    SQLiteDatabase writableDb = this.getWritableDatabase();
                    ContentValues values = new ContentValues();
                    values.put("status", "Fulfilled");
                    writableDb.update("reservations", values, "id = ?", new String[]{String.valueOf(reservationId)});
                }
            }
            // If they're over their book limit, we simply leave the reservation pending
            // so it can be picked up again the next time a copy is returned.
        } else {
            cursor.close();
        }
    }

    public Cursor getUserHistory(String userEmail) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM issued_books WHERE user_email = ?", new String[]{userEmail});
    }

    // --- Fine calculation ---
    public double calculateAndUpdateFine(int issuedBookId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT due_date, status FROM issued_books WHERE id = ?",
                new String[]{String.valueOf(issuedBookId)});

        double fine = 0.0;
        if (cursor.moveToFirst()) {
            String dueDateStr = cursor.getString(cursor.getColumnIndexOrThrow("due_date"));
            fine = calculateFineFromDueDate(dueDateStr);
        }
        cursor.close();

        SQLiteDatabase writableDb = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("fine", fine);
        writableDb.update("issued_books", values, "id = ?", new String[]{String.valueOf(issuedBookId)});

        return fine;
    }

    private double calculateFineFromDueDate(String dueDateStr) {
        if (dueDateStr == null || dueDateStr.isEmpty()) return 0.0;
        try {
            Date dueDate = DATE_FORMAT.parse(dueDateStr);
            Date today = DATE_FORMAT.parse(DATE_FORMAT.format(new Date()));
            long diffMillis = today.getTime() - dueDate.getTime();
            long daysLate = diffMillis / (1000 * 60 * 60 * 24);
            if (daysLate > 0) {
                return daysLate * Membership.FINE_PER_DAY;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    /**
     * Pays fine using the user's wallet balance.
     * Returns "OK", "INSUFFICIENT_BALANCE", or "NO_FINE"
     */
    public String payFine(String userEmail, double amount) {
        if (amount <= 0) return "NO_FINE";

        double balance = getWalletBalance(userEmail);
        if (amount > balance) {
            return "INSUFFICIENT_BALANCE";
        }

        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE users SET wallet = wallet - ? WHERE email = ?",
                new Object[]{amount, userEmail});

        // Clear fines on active issued books, oldest due date first, until amount runs out
        Cursor cursor = db.rawQuery(
                "SELECT id, fine FROM issued_books WHERE user_email = ? AND status = 'Active' AND fine > 0 ORDER BY due_date ASC",
                new String[]{userEmail});

        double remaining = amount;
        while (cursor.moveToNext() && remaining > 0) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            double fine = cursor.getDouble(cursor.getColumnIndexOrThrow("fine"));

            double payment = Math.min(fine, remaining);
            double newFine = fine - payment;
            remaining -= payment;

            ContentValues values = new ContentValues();
            values.put("fine", newFine);
            db.update("issued_books", values, "id = ?", new String[]{String.valueOf(id)});
        }
        cursor.close();

        return "OK";
    }

    // --- Balance / Wallet ---
    public double getWalletBalance(String userEmail) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT wallet FROM users WHERE email = ?", new String[]{userEmail});
        double balance = 0.0;
        if (cursor.moveToFirst()) {
            balance = cursor.getDouble(cursor.getColumnIndexOrThrow("wallet"));
        }
        cursor.close();
        return balance;
    }

    public boolean addBalance(String userEmail, double amount) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE users SET wallet = wallet + ? WHERE email = ?",
                new Object[]{amount, userEmail});
        return true;
    }

    // --- Reserve Book (hold a specific book by ID; separate from book_requests below) ---
    public boolean reserveBookById(String userEmail, String bookId, String bookTitle) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_email", userEmail);
        values.put("book_id", bookId);
        values.put("book_title", bookTitle);
        values.put("reservation_date", DATE_FORMAT.format(new Date()));
        values.put("status", "Pending");
        long result = db.insert("reservations", null, values);
        return result != -1;
    }

    public Cursor getUserReservations(String userEmail) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM reservations WHERE user_email = ? AND status = 'Pending'",
                new String[]{userEmail});
    }

    public boolean cancelReservation(int reservationId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("status", "Cancelled");
        int rows = db.update("reservations", values, "id = ?", new String[]{String.valueOf(reservationId)});
        return rows > 0;
    }

    // --- Request a book that isn't in the library yet (the "Search Online + Request" feature) ---
    public boolean reserveBook(String userEmail, String bookTitle) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_email", userEmail);
        values.put("book_title", bookTitle);
        values.put("status", "Pending");
        long result = db.insert("book_requests", null, values);
        return result != -1;
    }

    // --- Favourites ---
    public boolean addFavourite(String userEmail, String bookId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_email", userEmail);
        values.put("book_id", bookId);
        long result = db.insert("favourites", null, values);
        return result != -1;
    }
}