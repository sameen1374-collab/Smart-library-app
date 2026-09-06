package com.example.librarymanagementsystem;

import android.app.AlertDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MyRecordsFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private String userEmail;

    private TextView tvWalletBalance, tvTotalFine, tvNoReservations;
    private Button btnPayFine, btnAddBalance;
    private RecyclerView rvRecords, rvReservations;
    private RecordAdapter adapter;
    private ReservationAdapter reservationAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_records, container, false);

        dbHelper = new DatabaseHelper(requireContext());
        userEmail = getArguments() != null ? getArguments().getString("USER_EMAIL") : null;

        tvWalletBalance = view.findViewById(R.id.tvWalletBalance);
        tvTotalFine = view.findViewById(R.id.tvTotalFine);
        tvNoReservations = view.findViewById(R.id.tvNoReservations);
        btnPayFine = view.findViewById(R.id.btnPayFine);
        btnAddBalance = view.findViewById(R.id.btnAddBalance);
        rvRecords = view.findViewById(R.id.rvRecords);
        rvReservations = view.findViewById(R.id.rvReservations);

        adapter = new RecordAdapter();
        rvRecords.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvRecords.setAdapter(adapter);

        reservationAdapter = new ReservationAdapter(reservationId -> confirmCancelReservation(reservationId));
        rvReservations.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvReservations.setAdapter(reservationAdapter);

        loadData();

        btnPayFine.setOnClickListener(v -> confirmPayFine());

        btnAddBalance.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(requireContext(), AddBalanceActivity.class);
            intent.putExtra("USER_EMAIL", userEmail);
            startActivity(intent);
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData(); // refresh every time this tab is shown
    }

    private void loadData() {
        if (userEmail == null) return;

        double balance = dbHelper.getWalletBalance(userEmail);
        double totalFine = dbHelper.getUserFineBalance(userEmail);

        tvWalletBalance.setText("Rs " + balance);
        tvTotalFine.setText("Rs " + totalFine);

        Cursor cursor = dbHelper.getUserHistory(userEmail);
        adapter.setRecordsFromCursor(cursor);

        loadReservations();
    }

    private void loadReservations() {
        Cursor cursor = dbHelper.getUserReservations(userEmail);
        boolean isEmpty = cursor.getCount() == 0;

        reservationAdapter.setReservationsFromCursor(cursor); // also closes the cursor

        tvNoReservations.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rvReservations.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void confirmCancelReservation(int reservationId) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Cancel Reservation")
                .setMessage("Cancel this reservation?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    boolean success = dbHelper.cancelReservation(reservationId);
                    if (success) {
                        Toast.makeText(requireContext(), "Reservation cancelled.", Toast.LENGTH_SHORT).show();
                        loadReservations();
                    } else {
                        Toast.makeText(requireContext(), "Could not cancel. Try again.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void confirmPayFine() {
        double totalFine = dbHelper.getUserFineBalance(userEmail);
        double balance = dbHelper.getWalletBalance(userEmail);

        if (totalFine <= 0) {
            Toast.makeText(requireContext(), "You have no fine to pay.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (balance < totalFine) {
            Toast.makeText(requireContext(),
                    "Insufficient wallet balance. You need Rs " + totalFine + " but have Rs " + balance + ". Add balance first.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Pay Fine")
                .setMessage("Pay Rs " + totalFine + " from your wallet to clear your fine?")
                .setPositiveButton("Pay", (dialog, which) -> {
                    String result = dbHelper.payFine(userEmail, totalFine);
                    if ("OK".equals(result)) {
                        Toast.makeText(requireContext(), "Fine paid successfully!", Toast.LENGTH_SHORT).show();
                        loadData();
                    } else {
                        Toast.makeText(requireContext(), "Payment failed. Try again.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}