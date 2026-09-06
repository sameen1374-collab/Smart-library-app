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

public class ReservationAdapter extends RecyclerView.Adapter<ReservationAdapter.ViewHolder> {

    public interface OnCancelClickListener {
        void onCancelClick(int reservationId);
    }

    public static class ReservationItem {
        int id;
        String bookTitle, reservationDate;

        ReservationItem(int id, String bookTitle, String reservationDate) {
            this.id = id;
            this.bookTitle = bookTitle;
            this.reservationDate = reservationDate;
        }
    }

    private List<ReservationItem> reservations = new ArrayList<>();
    private final OnCancelClickListener listener;

    public ReservationAdapter(OnCancelClickListener listener) {
        this.listener = listener;
    }

    public void setReservationsFromCursor(Cursor cursor) {
        reservations.clear();
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            String title = cursor.getString(cursor.getColumnIndexOrThrow("book_title"));
            String date = cursor.getString(cursor.getColumnIndexOrThrow("reservation_date"));
            reservations.add(new ReservationItem(id, title, date));
        }
        cursor.close();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reservation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ReservationItem item = reservations.get(position);
        holder.tvTitle.setText(item.bookTitle);
        holder.tvDate.setText("Reserved on: " + item.reservationDate);
        holder.btnCancel.setOnClickListener(v -> {
            if (listener != null) listener.onCancelClick(item.id);
        });
    }

    @Override
    public int getItemCount() {
        return reservations.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDate;
        Button btnCancel;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvReservationTitle);
            tvDate = itemView.findViewById(R.id.tvReservationDate);
            btnCancel = itemView.findViewById(R.id.btnCancelReservation);
        }
    }
}