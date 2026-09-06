package com.example.librarymanagementsystem;

import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.ViewHolder> {

    public interface OnReserveClickListener {
        void onReserveClick(String bookId, String bookTitle);
    }

    // Small self-contained data holder so we don't need to touch the existing Book.java model
    public static class BookItem {
        String id, title, author, category;
        int availableQuantity;

        BookItem(String id, String title, String author, String category, int availableQuantity) {
            this.id = id;
            this.title = title;
            this.author = author;
            this.category = category;
            this.availableQuantity = availableQuantity;
        }
    }

    private List<BookItem> books = new ArrayList<>();
    private final OnReserveClickListener reserveListener;

    // Pass null if you don't need reserve functionality in a particular screen
    public BookAdapter(OnReserveClickListener reserveListener) {
        this.reserveListener = reserveListener;
    }

    // Rebuilds the list from a fresh DB Cursor (call this after every search/filter)
    public void setBooksFromCursor(Cursor cursor) {
        books.clear();
        while (cursor.moveToNext()) {
            String id = cursor.getString(cursor.getColumnIndexOrThrow("id"));
            String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
            String author = cursor.getString(cursor.getColumnIndexOrThrow("author"));
            String category = cursor.getString(cursor.getColumnIndexOrThrow("category"));
            int availableQty = cursor.getInt(cursor.getColumnIndexOrThrow("available_quantity"));
            books.add(new BookItem(id, title, author, category, availableQty));
        }
        cursor.close();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_book, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BookItem book = books.get(position);
        holder.tvTitle.setText(book.title);
        holder.tvAuthor.setText("by " + book.author);
        holder.tvId.setText("ID: " + book.id);
        holder.tvCategory.setText("Category: " + book.category);

        if (book.availableQuantity > 0) {
            holder.tvAvailability.setText("✓ Available (" + book.availableQuantity + ")");
            holder.tvAvailability.setTextColor(0xFF34A853); // green
            holder.btnReserve.setVisibility(View.GONE);
        } else {
            holder.tvAvailability.setText("✗ Not Available");
            holder.tvAvailability.setTextColor(0xFFEA4335); // red

            // Only show Reserve when the book is out of copies, and only if a listener was provided
            if (reserveListener != null) {
                holder.btnReserve.setVisibility(View.VISIBLE);
                holder.btnReserve.setOnClickListener(v -> reserveListener.onReserveClick(book.id, book.title));
            } else {
                holder.btnReserve.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return books.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvAuthor, tvId, tvCategory, tvAvailability;
        Button btnReserve;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvBookTitle);
            tvAuthor = itemView.findViewById(R.id.tvBookAuthor);
            tvId = itemView.findViewById(R.id.tvBookId);
            tvCategory = itemView.findViewById(R.id.tvBookCategory);
            tvAvailability = itemView.findViewById(R.id.tvBookAvailability);
            btnReserve = itemView.findViewById(R.id.btnReserve);
        }
    }
}