package com.example.stack_forduo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class GameOverActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gameover);

        String winner = getIntent().getStringExtra("WINNER");
        int player1Score = getIntent().getIntExtra("PLAYER1_SCORE", 0);
        int player2Score = getIntent().getIntExtra("PLAYER2_SCORE", 0);

        TextView winnerTextView = findViewById(R.id.winner);
        winnerTextView.setText(winner);

        TextView player1ScoreView = findViewById(R.id.player1_score_view);
        TextView player2ScoreView = findViewById(R.id.player2_score_view);

        player1ScoreView.setText("Player 1 Score: " + player1Score);
        player2ScoreView.setText("Player 2 Score: " + player2Score);

        Button playAgainButton = (Button) findViewById(R.id.playagain);
        playAgainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GameOverActivity.this, GameActivity.class);
                startActivity(intent);
            }
        });

        Button quitButton = (Button) findViewById(R.id.quitButton);
        playAgainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GameOverActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        // 스크린 전체화면 설정(appbar, homebar 숨기기)
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );
    }
}