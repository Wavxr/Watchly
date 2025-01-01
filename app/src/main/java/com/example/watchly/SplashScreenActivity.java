package com.example.watchly;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashScreenActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Check if user is already logged in
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // If user is logged in, go to DashboardActivity, else go to IntroActivity
        if (currentUser != null) {
            // User is logged in, redirect to DashboardActivity
            startActivity(new Intent(SplashScreenActivity.this, DashboardActivity.class));
        } else {
            // User is not logged in, redirect to IntroActivity (Welcome screen)
            startActivity(new Intent(SplashScreenActivity.this, IntroActivity.class));
        }

        // Close SplashScreenActivity to prevent user from returning here
        finish();
    }
}
