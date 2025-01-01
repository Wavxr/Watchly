package com.example.watchly.UI;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.watchly.R;
import com.example.watchly.models.Movie;

import java.util.List;

public class WatchedMoviesAdapter extends RecyclerView.Adapter<WatchedMoviesAdapter.WatchedMovieViewHolder> {

    private List<Movie> watchedMovies;
    private final Context context;
    private OnWatchedMovieClickListener listener; // Removed static keyword

    public WatchedMoviesAdapter(List<Movie> watchedMovies, Context context) {
        this.watchedMovies = watchedMovies;
        this.context = context;
    }

    public interface OnWatchedMovieClickListener {
        void onWatchedMovieClick(Movie movie, String documentId);
    }

    public void setOnWatchedMovieClickListener(OnWatchedMovieClickListener listener) {
        this.listener = listener; // Use instance-level listener
    }

    @NonNull
    @Override
    public WatchedMovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_watched_movie, parent, false);
        return new WatchedMovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WatchedMovieViewHolder holder, int position) {
        Movie movie = watchedMovies.get(position);

        // Set title, rating, review, and date
        holder.title.setText(movie.getTitle());
        holder.rating.setText(movie.getRating() != null ? "⭐ " + movie.getRating() : "N/A");
        holder.review.setText(movie.getReview() != null ? movie.getReview() : "No Review");
        holder.dateWatched.setText(movie.getDateWatched() != null ? movie.getDateWatched() : "No Date");

        // Use Glide to load the poster image
        String imageUrl = "https://image.tmdb.org/t/p/w500" + movie.getPosterPath();
        Glide.with(context)
                .load(imageUrl)
                .into(holder.poster);

        // Set item click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onWatchedMovieClick(movie, movie.getDocumentId()); // Ensure documentId is passed
            }
        });
    }

    @Override
    public int getItemCount() {
        return watchedMovies.size();
    }

    static class WatchedMovieViewHolder extends RecyclerView.ViewHolder {
        ImageView poster;
        TextView title, rating, review, dateWatched;

        public WatchedMovieViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.watched_movie_title);
            rating = itemView.findViewById(R.id.watched_movie_rating);
            review = itemView.findViewById(R.id.watched_movie_review);
            dateWatched = itemView.findViewById(R.id.watched_movie_date);
            poster = itemView.findViewById(R.id.movie_poster);
        }
    }
}
