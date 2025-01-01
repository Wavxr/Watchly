package com.example.watchly.UI;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.watchly.R;
import com.example.watchly.models.Movie;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListFragment extends Fragment {

    private RecyclerView recyclerViewWatched, recyclerViewToWatch;
    private WatchedMoviesAdapter watchedAdapter;
    private ToWatchMoviesAdapter toWatchAdapter;
    private List<Movie> watchedMoviesList;
    private List<Movie> toWatchMoviesList;
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private String userId = auth.getUid();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        // Initialize FirebaseAuth to get the userId
        String userId = FirebaseAuth.getInstance().getUid();

        // Ensure userId is available
        if (userId == null) {
            Toast.makeText(getContext(), "Error: User not authenticated.", Toast.LENGTH_SHORT).show();
            return view;
        }

        // Initialize buttons and RecyclerViews
        Button btnWatched = view.findViewById(R.id.btn_watched);
        Button btnToWatch = view.findViewById(R.id.btn_to_watch);

        recyclerViewWatched = view.findViewById(R.id.recyclerViewWatched);
        recyclerViewToWatch = view.findViewById(R.id.recyclerViewToWatch);

        // Use GridLayoutManager for 3 columns
        recyclerViewWatched.setLayoutManager(new GridLayoutManager(getContext(), 3));
        recyclerViewToWatch.setLayoutManager(new GridLayoutManager(getContext(), 3));

        // Initialize movie lists and adapters
        watchedMoviesList = new ArrayList<>();
        toWatchMoviesList = new ArrayList<>();

        watchedAdapter = new WatchedMoviesAdapter(watchedMoviesList, requireContext());
        toWatchAdapter = new ToWatchMoviesAdapter(toWatchMoviesList, requireContext());

        recyclerViewWatched.setAdapter(watchedAdapter);
        recyclerViewToWatch.setAdapter(toWatchAdapter);

        // Set click listeners
        watchedAdapter.setOnWatchedMovieClickListener((movie, documentId) -> showWatchedMovieDetailsDialog(movie, documentId));
        toWatchAdapter.setOnToWatchMovieClickListener((movie, documentId) -> showMovieDialog(movie, documentId));

        // Fetch data for both lists
        fetchWatchedMovies(userId);
        fetchToWatchMovies(userId);

        // Button toggles for visibility
        btnWatched.setOnClickListener(v -> {
            recyclerViewWatched.setVisibility(View.VISIBLE);
            recyclerViewToWatch.setVisibility(View.GONE);
        });

        btnToWatch.setOnClickListener(v -> {
            recyclerViewWatched.setVisibility(View.GONE);
            recyclerViewToWatch.setVisibility(View.VISIBLE);
        });

        return view;
    }



    private void fetchWatchedMovies(String userId) {
        Log.d("FirestoreCheck", "fetchWatchedMovies is being called with userId: " + userId);
        if (userId == null) {
            Log.e("FirestoreError", "User ID is null!");
            Toast.makeText(getContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("movies")
                .whereEqualTo("userId", userId)
                .whereEqualTo("status", "watched")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Movie> movies = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Movie movie = document.toObject(Movie.class);
                        movie.setDocumentId(document.getId());  // Set documentId
                        movies.add(movie);
                    }
                    watchedMoviesList.clear();
                    watchedMoviesList.addAll(movies);
                    watchedAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreError", "Error fetching data", e);
                    Toast.makeText(getContext(), "Failed to fetch watched movies: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchToWatchMovies(String userId) {
        Log.d("FirestoreCheck", "fetchToWatchMovies is being called with userId: " + userId);
        if (userId == null) {
            Log.e("FirestoreError", "User ID is null!");
            Toast.makeText(getContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("movies")
                .whereEqualTo("userId", userId)
                .whereEqualTo("status", "to-watch")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Movie> movies = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Movie movie = document.toObject(Movie.class);
                        movie.setDocumentId(document.getId());  // Set documentId
                        movies.add(movie);
                    }

                    // Debugging log
                    Log.d("FirestoreQuery", "Fetched to-watch movies: " + movies.size());

                    // Update the toWatchMoviesList and notify the adapter
                    toWatchMoviesList.clear();
                    toWatchMoviesList.addAll(movies); // Add the fetched movies to the list
                    toWatchAdapter.notifyDataSetChanged(); // Notify adapter to refresh RecyclerView
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreError", "Error fetching to-watch data", e);
                    Toast.makeText(getContext(), "Failed to fetch to-watch movies: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    private void showMovieDialog(Movie movie) {
        // Assuming that the movie object has the documentId field
        String documentId = movie.getDocumentId();
        if (documentId != null) {
            showMovieDialog(movie, documentId);
        } else {
            Log.e("MovieDialog", "Document ID is null");
            Toast.makeText(requireContext(), "Failed to fetch movie data.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showWatchedMovieDetailsDialog(Movie movie, String documentId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Movie Details");

        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_movie_details, null, false);
        builder.setView(dialogView);

        TextView titleView = dialogView.findViewById(R.id.movie_details_title);
        TextView ratingView = dialogView.findViewById(R.id.movie_details_rating);
        TextView reviewView = dialogView.findViewById(R.id.movie_details_review);
        TextView dateWatchedView = dialogView.findViewById(R.id.movie_details_date_watched);

        // Populate the dialog with movie data
        titleView.setText(movie.getTitle());
        ratingView.setText(movie.getRating() != null ? "Rating: " + movie.getRating() : "No Rating");
        reviewView.setText(movie.getReview() != null ? "Review: " + movie.getReview() : "No Review");
        dateWatchedView.setText(movie.getDateWatched() != null ? "Date Watched: " + movie.getDateWatched() : "Date Watched: N/A");

        // Add buttons for Edit and Delete
        builder.setNeutralButton("Edit", (dialog, which) -> {
            dialog.dismiss(); // Close details dialog
            showMovieDialog(movie, documentId); // Open the edit dialog
        });

        builder.setNegativeButton("Delete", (dialog, which) -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Confirm Delete")
                    .setMessage("Are you sure you want to delete this movie?")
                    .setPositiveButton("Yes", (confirmDialog, confirmWhich) -> {
                        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                        firestore.collection("movies")
                                .document(documentId)
                                .delete()
                                .addOnSuccessListener(aVoid ->
                                        Toast.makeText(requireContext(), "Movie deleted successfully!", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e ->
                                        Toast.makeText(requireContext(), "Failed to delete movie: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    })
                    .setNegativeButton("No", (confirmDialog, confirmWhich) -> confirmDialog.dismiss())
                    .show();
        });

        builder.setPositiveButton("Close", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private void showMovieDialog(Movie movie, String documentId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Edit or Delete Movie");

        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_or_delete, null, false);
        builder.setView(dialogView);

        RadioGroup statusGroup = dialogView.findViewById(R.id.status_group);
        EditText ratingInput = dialogView.findViewById(R.id.edit_movie_rating);
        EditText reviewInput = dialogView.findViewById(R.id.edit_movie_review);
        DatePicker datePicker = dialogView.findViewById(R.id.edit_date_picker);

        // Initialize Firestore
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        // Set default visibility based on the movie's current status
        if ("to-watch".equals(movie.getStatus())) {
            statusGroup.check(R.id.status_to_watch);
            ratingInput.setVisibility(View.GONE);
            reviewInput.setVisibility(View.GONE);
            datePicker.setVisibility(View.GONE);
        } else {
            statusGroup.check(R.id.status_watched);
            ratingInput.setVisibility(View.VISIBLE);
            reviewInput.setVisibility(View.VISIBLE);
            datePicker.setVisibility(View.VISIBLE);
        }

        // Update visibility dynamically when the status changes
        statusGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.status_to_watch) {
                ratingInput.setVisibility(View.GONE);
                reviewInput.setVisibility(View.GONE);
                datePicker.setVisibility(View.GONE);
            } else {
                ratingInput.setVisibility(View.VISIBLE);
                reviewInput.setVisibility(View.VISIBLE);
                datePicker.setVisibility(View.VISIBLE);
            }
        });

        // Pre-fill the form with the movie's current data
        ratingInput.setText(movie.getRating() != null ? String.valueOf(movie.getRating()) : "");
        reviewInput.setText(movie.getReview() != null ? movie.getReview() : "");

        if (movie.getDateWatched() != null) {
            String[] dateParts = movie.getDateWatched().split("/");
            int day = Integer.parseInt(dateParts[0]);
            int month = Integer.parseInt(dateParts[1]) - 1; // Month is 0-based
            int year = Integer.parseInt(dateParts[2]);
            datePicker.updateDate(year, month, day);
        }

        builder.setPositiveButton("Save Changes", (dialog, which) -> {
            String status = (statusGroup.getCheckedRadioButtonId() == R.id.status_watched) ? "watched" : "to-watch";
            String updatedRating = status.equals("watched") ? ratingInput.getText().toString().trim() : null;
            String updatedReview = status.equals("watched") ? reviewInput.getText().toString().trim() : null;
            String updatedDate = status.equals("watched")
                    ? datePicker.getDayOfMonth() + "/" + (datePicker.getMonth() + 1) + "/" + datePicker.getYear()
                    : null;

            // Prepare data to update
            Map<String, Object> updatedData = new HashMap<>();
            updatedData.put("status", status);
            updatedData.put("rating", updatedRating);
            updatedData.put("review", updatedReview);
            updatedData.put("dateWatched", updatedDate);

            // Update Firestore document
            firestore.collection("movies")
                    .document(documentId)
                    .update(updatedData)
                    .addOnSuccessListener(aVoid ->
                            Toast.makeText(requireContext(), "Movie updated successfully!", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(requireContext(), "Failed to update movie: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        builder.setNegativeButton("Delete", (dialog, which) -> {
            firestore.collection("movies")
                    .document(documentId)
                    .delete()
                    .addOnSuccessListener(aVoid ->
                            Toast.makeText(requireContext(), "Movie deleted successfully!", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(requireContext(), "Failed to delete movie: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        builder.setNeutralButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
    }
}