package com.ukrit.gamebrick;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class GameOverActivity extends AppCompatActivity {

    private TextView scoreTextView, highScoreTextView;
    private Button retryButton, exitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_over);

        // Initialize views
        scoreTextView = findViewById(R.id.score_text);
        highScoreTextView = findViewById(R.id.high_score_text);
        retryButton = findViewById(R.id.retry_button);
        exitButton = findViewById(R.id.exit_button);

        // Get score and high score from Intent
        int score = getIntent().getIntExtra("score", 0);

        // Load high score from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("game_prefs", MODE_PRIVATE);
        int highScore = prefs.getInt("high_score", 0);

        // Update high score if current score is higher
        if (score > highScore) {
            highScore = score;
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("high_score", highScore);
            editor.apply();
        }

        // Display score and high score
        scoreTextView.setText("Score: " + score);
        highScoreTextView.setText("High Score: " + highScore);

        // Retry button click listener
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Restart the game
                Intent intent = new Intent(GameOverActivity.this, GameActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Exit button click listener
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Close the app
            }
        });
    }
}

