package com.example.librarymanagementsystem;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SearchOnlineFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private String userEmail;

    private EditText etOnlineSearch;
    private Button btnOnlineSearch;
    private RadioGroup rgSearchType;
    private ProgressBar progressBar;
    private TextView tvOnlineStatus;
    private RecyclerView rvOnlineResults;
    private OnlineBookAdapter adapter;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_online, container, false);

        dbHelper = new DatabaseHelper(requireContext());
        userEmail = getArguments() != null ? getArguments().getString("USER_EMAIL") : null;

        etOnlineSearch = view.findViewById(R.id.etOnlineSearch);
        btnOnlineSearch = view.findViewById(R.id.btnOnlineSearch);
        rgSearchType = view.findViewById(R.id.rgSearchType);
        progressBar = view.findViewById(R.id.progressBar);
        tvOnlineStatus = view.findViewById(R.id.tvOnlineStatus);
        rvOnlineResults = view.findViewById(R.id.rvOnlineResults);

        adapter = new OnlineBookAdapter(this::confirmRequest);
        rvOnlineResults.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvOnlineResults.setAdapter(adapter);

        rgSearchType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbByAuthor) {
                etOnlineSearch.setHint("Search by author...");
            } else {
                etOnlineSearch.setHint("Search by title...");
            }
        });

        btnOnlineSearch.setOnClickListener(v -> {
            String query = etOnlineSearch.getText().toString().trim();
            if (query.isEmpty()) {
                Toast.makeText(requireContext(), "Type something to search first.", Toast.LENGTH_SHORT).show();
                return;
            }
            boolean searchByAuthor = rgSearchType.getCheckedRadioButtonId() == R.id.rbByAuthor;
            performSearch(query, searchByAuthor);
        });

        return view;
    }

    private void performSearch(String query, boolean searchByAuthor) {
        progressBar.setVisibility(View.VISIBLE);
        tvOnlineStatus.setVisibility(View.GONE);
        rvOnlineResults.setVisibility(View.GONE);

        executor.execute(() -> {
            List<OnlineBookResult> results = fetchFromOpenLibrary(query, searchByAuthor);

            mainHandler.post(() -> {
                if (!isAdded()) return; // fragment might have been closed while waiting

                progressBar.setVisibility(View.GONE);

                if (results == null) {
                    tvOnlineStatus.setVisibility(View.VISIBLE);
                    tvOnlineStatus.setText("Could not connect. Check your internet connection and try again.");
                } else if (results.isEmpty()) {
                    tvOnlineStatus.setVisibility(View.VISIBLE);
                    tvOnlineStatus.setText("No results found for \"" + query + "\".");
                } else {
                    rvOnlineResults.setVisibility(View.VISIBLE);
                    adapter.setResults(results);
                }
            });
        });
    }

    // Runs on a background thread — calls Open Library's free search API and parses the JSON
    private List<OnlineBookResult> fetchFromOpenLibrary(String query, boolean searchByAuthor) {
        List<OnlineBookResult> results = new ArrayList<>();
        try {
            String encodedQuery = URLEncoder.encode(query, "UTF-8");
            String param = searchByAuthor ? "author" : "title";
            URL url = new URL("https://openlibrary.org/search.json?" + param + "=" + encodedQuery + "&limit=15");

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(8000);
            connection.setReadTimeout(8000);

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            connection.disconnect();

            JSONObject json = new JSONObject(response.toString());
            JSONArray docs = json.getJSONArray("docs");

            for (int i = 0; i < docs.length(); i++) {
                JSONObject doc = docs.getJSONObject(i);

                String title = doc.optString("title", "Unknown Title");

                String author = "Unknown Author";
                if (doc.has("author_name")) {
                    JSONArray authors = doc.getJSONArray("author_name");
                    if (authors.length() > 0) {
                        author = authors.getString(0);
                    }
                }

                String year = doc.has("first_publish_year") ? String.valueOf(doc.getInt("first_publish_year")) : "";

                results.add(new OnlineBookResult(title, author, year));
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null; // signals a connection/parsing error to the UI
        }
        return results;
    }

    private void confirmRequest(String bookTitle) {
        if (userEmail == null) {
            Toast.makeText(requireContext(), "User not found, please log in again.", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Request This Book")
                .setMessage("Ask the library to add \"" + bookTitle + "\" to the catalog?")
                .setPositiveButton("Request", (dialog, which) -> {
                    boolean success = dbHelper.reserveBook(userEmail, bookTitle); // writes to book_requests table
                    if (success) {
                        Toast.makeText(requireContext(), "Request sent to the library admin!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "Could not send request. Try again.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Don't shut down the executor here — fragment view can be recreated while tab switches
    }
}