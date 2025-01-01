// SearchFragment.java

package com.example.watchly.UI;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.watchly.R;
import com.example.watchly.models.Movie;
import com.example.watchly.models.MovieResponse;
import com.example.watchly.network.TMDbApi;
import com.example.watchly.network.TMDbService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchFragment extends Fragment {

    private static final String ARG_QUERY = "query";
    private RecyclerView searchRecyclerView;
    private MovieAdapter movieAdapter;
    private String query;

    // Firebase variables
    private FirebaseFirestore firestore;
    private String userId;


    public static SearchFragment newInstance(String query) {
        SearchFragment fragment = new SearchFragment();
        Bundle args = new Bundle();
        args.putString(ARG_QUERY, query);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        if (getArguments() != null) {
            query = getArguments().getString(ARG_QUERY);
        }

        // Initialize Firebase
        firestore = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getUid();

        searchRecyclerView = view.findViewById(R.id.search_recycler_view);
        searchRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        movieAdapter = new MovieAdapter(requireContext());
        searchRecyclerView.setAdapter(movieAdapter);

        movieAdapter.setOnMovieClickListener(this::showMovieDialog);

        fetchSearchResults(query);

        return view;
    }

    private void fetchSearchResults(String query) {
        TMDbService tmDbService = TMDbApi.getTMDbService();
        String apiKey = getString(R.string.tmdb_api_key);

        tmDbService.searchMovies(apiKey, query).enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Movie> movies = response.body().getResults();
                    movieAdapter.setMovies(movies);
                } else {
                    Log.e("SearchFragment", "Error fetching search results: " + response.message());
                    Toast.makeText(getContext(), "No results found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {
                Log.e("SearchFragment", "Error: " + t.getMessage());
                Toast.makeText(getContext(), "Network error while searching movies", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showMovieDialog(Movie movie) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Add to List");

        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_to_list, null, false);
        builder.setView(dialogView);

        RadioGroup statusGroup = dialogView.findViewById(R.id.status_group);
        EditText ratingInput = dialogView.findViewById(R.id.rating_input);
        EditText reviewInput = dialogView.findViewById(R.id.review_input);
        DatePicker datePicker = dialogView.findViewById(R.id.date_picker);

        // Default visibility based on "Watched" status
        ratingInput.setVisibility(View.VISIBLE);
        reviewInput.setVisibility(View.VISIBLE);
        datePicker.setVisibility(View.VISIBLE);

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

        builder.setPositiveButton("Add to List", (dialog, which) -> {
            String status = (statusGroup.getCheckedRadioButtonId() == R.id.status_watched) ? "watched" : "to-watch";
            String rating = ratingInput.getText().toString().trim();
            String review = reviewInput.getText().toString().trim();
            String dateWatched = (status.equals("watched"))
                    ? datePicker.getDayOfMonth() + "/" + (datePicker.getMonth() + 1) + "/" + datePicker.getYear()
                    : null;

            // Prepare movie data
            Map<String, Object> movieData = new HashMap<>();
            movieData.put("title", movie.getTitle());
            movieData.put("posterPath", movie.getPosterPath());
            movieData.put("rating", status.equals("watched") ? rating : null);
            movieData.put("review", status.equals("watched") ? review : null);
            movieData.put("dateWatched", dateWatched);
            movieData.put("status", status);
            movieData.put("userId", userId);

            // Save to Firebase
            firestore.collection("movies")
                    .add(movieData)
                    .addOnSuccessListener(documentReference ->
                            Toast.makeText(requireContext(), "Movie added!", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(requireContext(), "Failed to add movie: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
}