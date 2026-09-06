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

public class AdminRequestAdapter extends RecyclerView.Adapter<AdminRequestAdapter.ViewHolder> {

    public interface OnActionClickListener {
        void onAccept(int requestId, String bookTitle);
        void onReject(int requestId, String bookTitle);
    }

    public static class RequestItem {
        int id;
        String bookTitle, userEmail;

        RequestItem(int id, String bookTitle, String userEmail) {
            this.id = id;
            this.bookTitle = bookTitle;
            this.userEmail = userEmail;
        }
    }

    private List<RequestItem> requests = new ArrayList<>();
    private final OnActionClickListener listener;

    public AdminRequestAdapter(OnActionClickListener listener) {
        this.listener = listener;
    }

    public void setRequestsFromCursor(Cursor cursor) {
        requests.clear();
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            String title = cursor.getString(cursor.getColumnIndexOrThrow("book_title"));
            String email = cursor.getString(cursor.getColumnIndexOrThrow("user_email"));
            requests.add(new RequestItem(id, title, email));
        }
        cursor.close();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RequestItem item = requests.get(position);
        holder.tvBookTitle.setText(item.bookTitle);
        holder.tvRequestedBy.setText("Requested by: " + item.userEmail);

        holder.btnAccept.setOnClickListener(v -> {
            if (listener != null) listener.onAccept(item.id, item.bookTitle);
        });
        holder.btnReject.setOnClickListener(v -> {
            if (listener != null) listener.onReject(item.id, item.bookTitle);
        });
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvBookTitle, tvRequestedBy;
        Button btnAccept, btnReject;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBookTitle = itemView.findViewById(R.id.tvBookTitle);
            tvRequestedBy = itemView.findViewById(R.id.tvRequestedBy);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnReject = itemView.findViewById(R.id.btnReject);
        }
    }
}