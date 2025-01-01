package com.example.watchly;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class IntroActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        // Find the button and set an OnClickListener
        Button getStartedButton = findViewById(R.id.get_started_button);
        getStartedButton.setOnClickListener(v -> {
            // Start LoginActivity when button is clicked
            Intent intent = new Intent(IntroActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }
}
