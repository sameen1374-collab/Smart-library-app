package com.example.librarymanagementsystem;

import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.ViewHolder> {

    public static class RecordItem {
        String title, author, issueDate, dueDate, returnDate, status;
        double fine;

        RecordItem(String title, String author, String issueDate, String dueDate,
                   String returnDate, String status, double fine) {
            this.title = title;
            this.author = author;
            this.issueDate = issueDate;
            this.dueDate = dueDate;
            this.returnDate = returnDate;
            this.status = status;
            this.fine = fine;
        }
    }

    private List<RecordItem> records = new ArrayList<>();

    public void setRecordsFromCursor(Cursor cursor) {
        records.clear();
        while (cursor.moveToNext()) {
            String title = cursor.getString(cursor.getColumnIndexOrThrow("book_title"));
            String author = cursor.getString(cursor.getColumnIndexOrThrow("author"));
            String issueDate = cursor.getString(cursor.getColumnIndexOrThrow("issue_date"));
            String dueDate = cursor.getString(cursor.getColumnIndexOrThrow("due_date"));
            String returnDate = cursor.getString(cursor.getColumnIndexOrThrow("return_date"));
            String status = cursor.getString(cursor.getColumnIndexOrThrow("status"));
            double fine = cursor.getDouble(cursor.getColumnIndexOrThrow("fine"));

            records.add(new RecordItem(title, author, issueDate, dueDate, returnDate, status, fine));
        }
        cursor.close();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_record, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecordItem record = records.get(position);
        holder.tvTitle.setText(record.title);
        holder.tvAuthor.setText("by " + record.author);
        holder.tvIssueDate.setText("Issue Date: " + record.issueDate);
        holder.tvDueDate.setText("Due Date: " + record.dueDate);
        holder.tvReturnDate.setText("Return Date: " + record.returnDate);

        if (record.fine > 0) {
            holder.tvFine.setText("Fine: Rs " + record.fine);
            holder.tvFine.setTextColor(0xFFEA4335); // red
        } else {
            holder.tvFine.setText("No Fine");
            holder.tvFine.setTextColor(0xFF34A853); // green
        }

        holder.tvStatusBadge.setText(record.status);
        if ("Active".equalsIgnoreCase(record.status)) {
            holder.tvStatusBadge.setBackgroundColor(0xFF1A73E8); // blue
        } else {
            holder.tvStatusBadge.setBackgroundColor(0xFF888888); // grey for Returned
        }
    }

    @Override
    public int getItemCount() {
        return records.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvAuthor, tvIssueDate, tvDueDate, tvReturnDate, tvFine, tvStatusBadge;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvRecordTitle);
            tvAuthor = itemView.findViewById(R.id.tvRecordAuthor);
            tvIssueDate = itemView.findViewById(R.id.tvIssueDate);
            tvDueDate = itemView.findViewById(R.id.tvDueDate);
            tvReturnDate = itemView.findViewById(R.id.tvReturnDate);
            tvFine = itemView.findViewById(R.id.tvFine);
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
        }
    }
}