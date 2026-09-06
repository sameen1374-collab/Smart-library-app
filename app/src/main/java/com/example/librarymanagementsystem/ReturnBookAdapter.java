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

public class ReturnBookAdapter extends RecyclerView.Adapter<ReturnBookAdapter.ViewHolder> {

    public interface OnReturnClickListener {
        void onReturnClick(int issuedBookId, String bookId, String bookTitle);
    }

    public static class IssuedBookItem {
        int issuedBookId;
        String bookId, title, author, issueDate, dueDate;
        double fine;

        IssuedBookItem(int issuedBookId, String bookId, String title, String author,
                       String issueDate, String dueDate, double fine) {
            this.issuedBookId = issuedBookId;
            this.bookId = bookId;
            this.title = title;
            this.author = author;
            this.issueDate = issueDate;
            this.dueDate = dueDate;
            this.fine = fine;
        }
    }

    private List<IssuedBookItem> items = new ArrayList<>();
    private final OnReturnClickListener listener;

    public ReturnBookAdapter(OnReturnClickListener listener) {
        this.listener = listener;
    }

    public void setItemsFromCursor(Cursor cursor) {
        items.clear();
        while (cursor.moveToNext()) {
            int issuedBookId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            String bookId = cursor.getString(cursor.getColumnIndexOrThrow("book_id"));
            String title = cursor.getString(cursor.getColumnIndexOrThrow("book_title"));
            String author = cursor.getString(cursor.getColumnIndexOrThrow("author"));
            String issueDate = cursor.getString(cursor.getColumnIndexOrThrow("issue_date"));
            String dueDate = cursor.getString(cursor.getColumnIndexOrThrow("due_date"));
            double fine = cursor.getDouble(cursor.getColumnIndexOrThrow("fine"));

            items.add(new IssuedBookItem(issuedBookId, bookId, title, author, issueDate, dueDate, fine));
        }
        cursor.close();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_return_book, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        IssuedBookItem item = items.get(position);
        holder.tvTitle.setText(item.title);
        holder.tvAuthor.setText("by " + item.author);
        holder.tvDates.setText("Issued: " + item.issueDate + "    Due: " + item.dueDate);

        if (item.fine > 0) {
            holder.tvFineWarning.setVisibility(View.VISIBLE);
            holder.tvFineWarning.setText("⚠ Overdue! Fine: Rs " + item.fine);
        } else {
            holder.tvFineWarning.setVisibility(View.GONE);
        }

        holder.btnReturn.setOnClickListener(v -> {
            if (listener != null) {
                listener.onReturnClick(item.issuedBookId, item.bookId, item.title);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvAuthor, tvDates, tvFineWarning;
        Button btnReturn;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvAuthor = itemView.findViewById(R.id.tvAuthor);
            tvDates = itemView.findViewById(R.id.tvDates);
            tvFineWarning = itemView.findViewById(R.id.tvFineWarning);
            btnReturn = itemView.findViewById(R.id.btnReturn);
        }
    }
}