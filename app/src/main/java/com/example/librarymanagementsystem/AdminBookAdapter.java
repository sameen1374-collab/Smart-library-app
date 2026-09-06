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

public class AdminBookAdapter extends RecyclerView.Adapter<AdminBookAdapter.ViewHolder> {

    public interface OnDeleteClickListener {
        void onDeleteClick(String bookId, String bookTitle);
    }

    public interface OnEditClickListener {
        void onEditClick(BookItem book);
    }

    public static class BookItem {
        String id, title, author, category;
        int totalQty, availableQty;

        BookItem(String id, String title, String author, String category, int totalQty, int availableQty) {
            this.id = id;
            this.title = title;
            this.author = author;
            this.category = category;
            this.totalQty = totalQty;
            this.availableQty = availableQty;
        }
    }

    private List<BookItem> books = new ArrayList<>();
    private final OnDeleteClickListener deleteListener;
    private final OnEditClickListener editListener;

    public AdminBookAdapter(OnDeleteClickListener deleteListener, OnEditClickListener editListener) {
        this.deleteListener = deleteListener;
        this.editListener = editListener;
    }

    public void setBooksFromCursor(Cursor cursor) {
        books.clear();
        while (cursor.moveToNext()) {
            String id = cursor.getString(cursor.getColumnIndexOrThrow("id"));
            String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
            String author = cursor.getString(cursor.getColumnIndexOrThrow("author"));
            String category = cursor.getString(cursor.getColumnIndexOrThrow("category"));
            int totalQty = cursor.getInt(cursor.getColumnIndexOrThrow("total_quantity"));
            int availableQty = cursor.getInt(cursor.getColumnIndexOrThrow("available_quantity"));
            books.add(new BookItem(id, title, author, category, totalQty, availableQty));
        }
        cursor.close();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_book, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BookItem book = books.get(position);
        holder.tvTitle.setText(book.title);
        holder.tvAuthor.setText("by " + book.author);
        holder.tvMeta.setText("ID: " + book.id + " • " + book.category + " • " + book.availableQty + "/" + book.totalQty + " available");

        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) deleteListener.onDeleteClick(book.id, book.title);
        });

        holder.btnEdit.setOnClickListener(v -> {
            if (editListener != null) editListener.onEditClick(book);
        });
    }

    @Override
    public int getItemCount() {
        return books.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvAuthor, tvMeta;
        Button btnDelete, btnEdit;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvAuthor = itemView.findViewById(R.id.tvAuthor);
            tvMeta = itemView.findViewById(R.id.tvMeta);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnEdit = itemView.findViewById(R.id.btnEdit);
        }
    }
}