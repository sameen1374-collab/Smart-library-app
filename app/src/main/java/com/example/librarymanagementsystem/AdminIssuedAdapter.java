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

public class AdminIssuedAdapter extends RecyclerView.Adapter<AdminIssuedAdapter.ViewHolder> {

    public static class IssuedItem {
        String title, userEmail, issueDate, dueDate;
        double fine;

        IssuedItem(String title, String userEmail, String issueDate, String dueDate, double fine) {
            this.title = title;
            this.userEmail = userEmail;
            this.issueDate = issueDate;
            this.dueDate = dueDate;
            this.fine = fine;
        }
    }

    private List<IssuedItem> items = new ArrayList<>();

    public void setItemsFromCursor(Cursor cursor) {
        items.clear();
        while (cursor.moveToNext()) {
            String title = cursor.getString(cursor.getColumnIndexOrThrow("book_title"));
            String userEmail = cursor.getString(cursor.getColumnIndexOrThrow("user_email"));
            String issueDate = cursor.getString(cursor.getColumnIndexOrThrow("issue_date"));
            String dueDate = cursor.getString(cursor.getColumnIndexOrThrow("due_date"));
            double fine = cursor.getDouble(cursor.getColumnIndexOrThrow("fine"));
            items.add(new IssuedItem(title, userEmail, issueDate, dueDate, fine));
        }
        cursor.close();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_issued, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        IssuedItem item = items.get(position);
        holder.tvTitle.setText(item.title);
        holder.tvUserEmail.setText("Issued to: " + item.userEmail);
        holder.tvDates.setText("Issued: " + item.issueDate + " • Due: " + item.dueDate);

        if (item.fine > 0) {
            holder.tvFineStatus.setVisibility(View.VISIBLE);
            holder.tvFineStatus.setText("⚠ Overdue — Fine: Rs " + item.fine);
        } else {
            holder.tvFineStatus.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvUserEmail, tvDates, tvFineStatus;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
            tvDates = itemView.findViewById(R.id.tvDates);
            tvFineStatus = itemView.findViewById(R.id.tvFineStatus);
        }
    }
}