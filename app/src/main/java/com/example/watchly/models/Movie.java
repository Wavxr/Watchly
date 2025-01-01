package com.example.watchly.models;

import android.util.Log;

import com.google.gson.annotations.SerializedName;

public class Movie {
    private String movieId;
    @SerializedName("title")
    private String title;
    @SerializedName("vote_average")
    private Double rating;
    private String review;
    private String dateWatched;
    @SerializedName("poster_path")
    private String posterPath;
    private String status; // "watched" or "to-watch"
    private String userId; // To associate with a specific user
    private String documentId;

    // Empty constructor for Firebase
    public Movie() {}
    public Movie(String title, String posterPath) {
        this.title = title;
        this.posterPath = posterPath;
        this.rating = rating;
    }

    // Getters and setters
    public String getMovieId() {
        return movieId;
    }

    public void setMovieId(String movieId) {
        this.movieId = movieId;
    }
    public String getPosterPath() {
        return posterPath;
    }

    public String getTitle() {
        return title;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(String rating) {
        try {
            if (rating != null && !rating.isEmpty()) {
                this.rating = Double.valueOf(rating);
            } else {
                this.rating = 0.0;  // or set to a default value
            }
        } catch (NumberFormatException e) {
            Log.e("Movie", "Invalid rating value: " + rating, e);
            this.rating = 0.0;  // Default value in case of invalid input
        }
    }


    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }

    public String getDateWatched() {
        return dateWatched;
    }

    public void setDateWatched(String dateWatched) {
        this.dateWatched = dateWatched;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }
}