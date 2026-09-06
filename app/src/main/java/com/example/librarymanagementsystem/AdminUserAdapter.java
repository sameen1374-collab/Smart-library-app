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

public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.ViewHolder> {

    public interface OnDeleteClickListener {
        void onDeleteClick(String email, String name);
    }

    public interface OnCancelMembershipClickListener {
        void onCancelMembershipClick(String email, String name);
    }

    public static class UserItem {
        String name, email, membership;
        double wallet;

        UserItem(String name, String email, String membership, double wallet) {
            this.name = name;
            this.email = email;
            this.membership = membership;
            this.wallet = wallet;
        }
    }

    private List<UserItem> users = new ArrayList<>();
    private final OnDeleteClickListener deleteListener;
    private final OnCancelMembershipClickListener cancelListener;

    public AdminUserAdapter(OnDeleteClickListener deleteListener, OnCancelMembershipClickListener cancelListener) {
        this.deleteListener = deleteListener;
        this.cancelListener = cancelListener;
    }

    public void setUsersFromCursor(Cursor cursor) {
        users.clear();
        while (cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            String email = cursor.getString(cursor.getColumnIndexOrThrow("email"));
            String membership = cursor.getString(cursor.getColumnIndexOrThrow("membership"));
            double wallet = cursor.getDouble(cursor.getColumnIndexOrThrow("wallet"));
            users.add(new UserItem(name, email, membership, wallet));
        }
        cursor.close();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserItem user = users.get(position);
        holder.tvName.setText(user.name);
        holder.tvEmail.setText(user.email);
        holder.tvMeta.setText(user.membership + " membership • Wallet: Rs " + user.wallet);

        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) deleteListener.onDeleteClick(user.email, user.name);
        });

        holder.btnCancelMembership.setOnClickListener(v -> {
            if (cancelListener != null) cancelListener.onCancelMembershipClick(user.email, user.name);
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail, tvMeta;
        Button btnDelete, btnCancelMembership;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvMeta = itemView.findViewById(R.id.tvMeta);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnCancelMembership = itemView.findViewById(R.id.btnCancelMembership);
        }
    }
}