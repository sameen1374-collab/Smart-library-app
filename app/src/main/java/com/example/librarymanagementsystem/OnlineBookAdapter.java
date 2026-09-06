package com.example.librarymanagementsystem;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class OnlineBookAdapter extends RecyclerView.Adapter<OnlineBookAdapter.ViewHolder> {

    public interface OnRequestClickListener {
        void onRequestClick(String bookTitle);
    }

    private List<OnlineBookResult> results = new ArrayList<>();
    private final OnRequestClickListener listener;

    public OnlineBookAdapter(OnRequestClickListener listener) {
        this.listener = listener;
    }

    public void setResults(List<OnlineBookResult> newResults) {
        results = newResults;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_online_book, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OnlineBookResult book = results.get(position);
        holder.tvTitle.setText(book.title);
        holder.tvAuthor.setText("by " + book.author + (book.year != null && !book.year.isEmpty() ? " (" + book.year + ")" : ""));
        holder.btnRequest.setOnClickListener(v -> {
            if (listener != null) listener.onRequestClick(book.title);
        });
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvAuthor;
        Button btnRequest;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvOnlineTitle);
            tvAuthor = itemView.findViewById(R.id.tvOnlineAuthor);
            btnRequest = itemView.findViewById(R.id.btnRequestBook);
        }
    }
}