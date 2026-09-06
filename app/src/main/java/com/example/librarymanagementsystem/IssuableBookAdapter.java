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

public class IssuableBookAdapter extends RecyclerView.Adapter<IssuableBookAdapter.ViewHolder> {

    public interface OnIssueClickListener {
        void onIssueClick(String bookId, String bookTitle);
    }

    public static class BookItem {
        String id, title, author;
        int availableQty;

        BookItem(String id, String title, String author, int availableQty) {
            this.id = id;
            this.title = title;
            this.author = author;
            this.availableQty = availableQty;
        }
    }

    private List<BookItem> books = new ArrayList<>();
    private final OnIssueClickListener listener;

    public IssuableBookAdapter(OnIssueClickListener listener) {
        this.listener = listener;
    }

    public void setBooksFromCursor(Cursor cursor) {
        books.clear();
        while (cursor.moveToNext()) {
            String id = cursor.getString(cursor.getColumnIndexOrThrow("id"));
            String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
            String author = cursor.getString(cursor.getColumnIndexOrThrow("author"));
            int availableQty = cursor.getInt(cursor.getColumnIndexOrThrow("available_quantity"));
            books.add(new BookItem(id, title, author, availableQty));
        }
        cursor.close();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_issuable_book, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BookItem book = books.get(position);
        holder.tvTitle.setText(book.title);
        holder.tvAuthor.setText("by " + book.author);
        holder.tvMeta.setText("ID: " + book.id + " • " + book.availableQty + " copies available");

        holder.btnIssue.setOnClickListener(v -> {
            if (listener != null) {
                listener.onIssueClick(book.id, book.title);
            }
        });
    }

    @Override
    public int getItemCount() {
        return books.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvAuthor, tvMeta;
        Button btnIssue;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvAuthor = itemView.findViewById(R.id.tvAuthor);
            tvMeta = itemView.findViewById(R.id.tvMeta);
            btnIssue = itemView.findViewById(R.id.btnIssue);
        }
    }
}