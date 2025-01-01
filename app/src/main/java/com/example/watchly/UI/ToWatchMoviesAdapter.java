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

public class ToWatchMoviesAdapter extends RecyclerView.Adapter<ToWatchMoviesAdapter.ToWatchMovieViewHolder> {

    private List<Movie> toWatchMovies;
    private final Context context;
    private OnToWatchMovieClickListener listener; // Remove static

    public ToWatchMoviesAdapter(List<Movie> toWatchMovies, Context context) {
        this.toWatchMovies = toWatchMovies;
        this.context = context;
    }

    public interface OnToWatchMovieClickListener {
        void onToWatchMovieClick(Movie movie, String documentId);
    }

    public void setOnToWatchMovieClickListener(OnToWatchMovieClickListener listener) {
        this.listener = listener; // Use instance-level reference
    }

    @NonNull
    @Override
    public ToWatchMovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_to_watch_movie, parent, false);
        return new ToWatchMovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ToWatchMovieViewHolder holder, int position) {
        Movie movie = toWatchMovies.get(position);

        // Set title and placeholder rating
        holder.title.setText(movie.getTitle());
        holder.rating.setText("To Watch");

        // Use Glide to load the poster image
        String imageUrl = "https://image.tmdb.org/t/p/w500" + movie.getPosterPath();
        Glide.with(context)
                .load(imageUrl)
                .into(holder.poster);

        // Set item click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onToWatchMovieClick(movie, movie.getDocumentId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return toWatchMovies.size();
    }

    static class ToWatchMovieViewHolder extends RecyclerView.ViewHolder {
        public ImageView poster;
        TextView title, rating;

        public ToWatchMovieViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.to_watch_movie_title);
            rating = itemView.findViewById(R.id.to_watch_movie_rating);
            poster = itemView.findViewById(R.id.movie_poster); // Initialize ImageView for poster
        }
    }
}
