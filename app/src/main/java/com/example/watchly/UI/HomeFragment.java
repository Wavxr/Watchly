package com.example.watchly.UI;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import androidx.appcompat.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.watchly.R;
import com.example.watchly.models.Movie;
import com.example.watchly.network.TMDbApi;
import com.example.watchly.network.TMDbService;
import com.example.watchly.models.MovieResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {
    private RecyclerView recyclerView;
    private MovieAdapter movieAdapter;
    private FirebaseFirestore firestore;
    private String userId;
    private RecyclerView carouselRecyclerView, dramaRecyclerView, actionRecyclerView, comedyRecyclerView, romanceRecyclerView, horrorRecyclerView;
    private SearchView searchBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize RecyclerViews
        carouselRecyclerView = view.findViewById(R.id.carousel_recycler_view);
        dramaRecyclerView = view.findViewById(R.id.top_drama_recycler_view);
        actionRecyclerView = view.findViewById(R.id.top_action_recycler_view);
        comedyRecyclerView = view.findViewById(R.id.top_comedy_recycler_view);
        romanceRecyclerView = view.findViewById(R.id.top_romance_recycler_view);
        horrorRecyclerView = view.findViewById(R.id.top_horror_recycler_view);

        // Setup adapters for each RecyclerView
        MovieAdapter carouselAdapter = setupCarouselRecyclerView(carouselRecyclerView);
        MovieAdapter dramaAdapter = setupRecyclerView(dramaRecyclerView);
        MovieAdapter actionAdapter = setupRecyclerView(actionRecyclerView);
        MovieAdapter comedyAdapter = setupRecyclerView(comedyRecyclerView);
        MovieAdapter romanceAdapter = setupRecyclerView(romanceRecyclerView);
        MovieAdapter horrorAdapter = setupRecyclerView(horrorRecyclerView);

        // Set click listener for each adapter
        carouselAdapter.setOnMovieClickListener(this::showMovieDialog);
        dramaAdapter.setOnMovieClickListener(this::showMovieDialog);
        actionAdapter.setOnMovieClickListener(this::showMovieDialog);
        comedyAdapter.setOnMovieClickListener(this::showMovieDialog);
        romanceAdapter.setOnMovieClickListener(this::showMovieDialog);
        horrorAdapter.setOnMovieClickListener(this::showMovieDialog);

        // Initialize Firestore and User ID
        firestore = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getUid();

        // Fetch Movies
        fetchMovies(carouselAdapter, dramaAdapter, actionAdapter, comedyAdapter, romanceAdapter, horrorAdapter);

        // Initialize SearchView
        searchBar = view.findViewById(R.id.search_bar);
        setupSearchBar();

        return view;
    }


    private void setupSearchBar() {
        searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!TextUtils.isEmpty(query)) {
                    openSearchFragment(query);
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!TextUtils.isEmpty(newText)) {
                    openSearchFragment(newText);
                }
                return true;
            }
        });
    }
    private void openSearchFragment(String query) {
        SearchFragment searchFragment = SearchFragment.newInstance(query);
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.search_results_container, searchFragment, "SEARCH_FRAGMENT");
        transaction.commit();
    }

    private void closeSearchFragment() {
        requireActivity().getSupportFragmentManager().popBackStack();
    }

    private MovieAdapter setupCarouselRecyclerView(RecyclerView recyclerView) {
        CenterScalingLayoutManager layoutManager = new CenterScalingLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);

        // Attach PagerSnapHelper for center snapping
        PagerSnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(recyclerView);

        // Add scroll listener for scaling
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                // Trigger scaleChildren manually
                if (layoutManager instanceof CenterScalingLayoutManager) {
                    layoutManager.scaleChildren();
                }
            }
        });

        MovieAdapter adapter = new MovieAdapter(requireContext());
        recyclerView.setAdapter(adapter);
        return adapter;
    }


    private MovieAdapter setupRecyclerView(RecyclerView recyclerView) {
        if (recyclerView.getId() == R.id.carousel_recycler_view) {
            recyclerView.setLayoutManager(new CenterScalingLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
            PagerSnapHelper snapHelper = new PagerSnapHelper();
            snapHelper.attachToRecyclerView(recyclerView);
        } else {
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        }

        MovieAdapter adapter = new MovieAdapter(requireContext());
        recyclerView.setAdapter(adapter);
        return adapter;
    }


    private void fetchMovies(MovieAdapter carouselAdapter,
                             MovieAdapter dramaAdapter,
                             MovieAdapter actionAdapter,
                             MovieAdapter comedyAdapter,
                             MovieAdapter romanceAdapter,
                             MovieAdapter horrorAdapter) {
        TMDbService tmDbService = TMDbApi.getTMDbService();
        String apiKey = getString(R.string.tmdb_api_key);
        int currentYear = 2024;

        fetchMoviesForSection(tmDbService, apiKey, null, currentYear, carouselRecyclerView, carouselAdapter);  // Top Movies 2024
        fetchMoviesForSection(tmDbService, apiKey, "18", currentYear, dramaRecyclerView, dramaAdapter);    // Drama
        fetchMoviesForSection(tmDbService, apiKey, "28", currentYear, actionRecyclerView, actionAdapter);   // Action
        fetchMoviesForSection(tmDbService, apiKey, "35", currentYear, comedyRecyclerView, comedyAdapter);   // Comedy
        fetchMoviesForSection(tmDbService, apiKey, "10749", currentYear, romanceRecyclerView, romanceAdapter); // Romance
        fetchMoviesForSection(tmDbService, apiKey, "27", currentYear, horrorRecyclerView, horrorAdapter);   // Horror
    }

    private void fetchMoviesForSection(TMDbService service, String apiKey, String genreId, int year, RecyclerView recyclerView, MovieAdapter adapter) {
        service.getMoviesByGenre(apiKey, genreId, "popularity.desc", year, 1).enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Movie> movies = response.body().getResults();
                    adapter.setMovies(movies);

                    // Auto-center only the carousel RecyclerView
                    if (recyclerView.getId() == R.id.carousel_recycler_view) {
                        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                        if (layoutManager != null && movies.size() > 0) {
                            recyclerView.post(() -> {
                                int centerPosition = movies.size() / 2;

                                // Calculate offset for centering
                                View child = layoutManager.findViewByPosition(centerPosition);
                                if (child == null) {
                                    recyclerView.scrollToPosition(centerPosition);
                                    recyclerView.post(() -> adjustCenter(layoutManager, recyclerView, centerPosition));
                                } else {
                                    adjustCenter(layoutManager, recyclerView, centerPosition);
                                }
                            });
                        }
                    }
                } else {
                    Log.e("HomeFragment", "Error fetching movies for section: " + response.message());
                    Toast.makeText(getContext(), "Failed to fetch movies for section", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {
                Log.e("HomeFragment", "Error: " + t.getMessage());
                Toast.makeText(getContext(), "Network error while fetching movies", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void adjustCenter(LinearLayoutManager layoutManager, RecyclerView recyclerView, int centerPosition) {
        View child = layoutManager.findViewByPosition(centerPosition);
        if (child != null) {
            int childWidth = child.getWidth();
            int recyclerViewWidth = recyclerView.getWidth();
            int offset = (recyclerViewWidth - childWidth) / 2; // Calculate center offset
            layoutManager.scrollToPositionWithOffset(centerPosition, offset);
        }
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
