package com.example.stack_forduo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class GameOverActivity extends AppCompatActivity {
    private int whichMode;
    private boolean isFirstTouch = true; // 첫 번째 터치 여부를 확인하는 플래그

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        whichMode = getIntent().getIntExtra("MODE", 0);

        if (whichMode == 1 || whichMode == 2) {
            setContentView(R.layout.activity_gameover_multi);

            String winner = getIntent().getStringExtra("WINNER");
            String player1Name = getIntent().getStringExtra("PLAYER1_NAME");
            int player1Score = getIntent().getIntExtra("PLAYER1_SCORE", 0);
            String player2Name = getIntent().getStringExtra("PLAYER2_NAME");
            int player2Score = getIntent().getIntExtra("PLAYER2_SCORE", 0);

            // "WINNER IS" 텍스트로 변경
            TextView winnerTextView = findViewById(R.id.winner_text);
            winnerTextView.setText("WINNER IS");

            TextView winnerNameTextView = findViewById(R.id.winner);
            winnerNameTextView.setText(winner);

            TextView player1ScoreView = findViewById(R.id.player1_score_view);
            TextView player2ScoreView = findViewById(R.id.player2_score_view);

            player1ScoreView.setText(player1Name + " Score: " + player1Score);
            player2ScoreView.setText(player2Name + " Score: " + player2Score);

            // 리더보드 업데이트 및 표시
            updateLeaderboard(player1Name, player1Score);
            updateLeaderboard(player2Name, player2Score);
        }
        // single 모드일 경우
        else {
            setContentView(R.layout.activity_gameover_single);

            String player1Name = getIntent().getStringExtra("PLAYER1_NAME");
            int player1Score = getIntent().getIntExtra("PLAYER1_SCORE", 0);

            // 싱글모드에서는 "Game Over" 표시
            TextView winnerTextView = findViewById(R.id.winner_text);
            winnerTextView.setText("Game Over");

            TextView player1ScoreView = findViewById(R.id.player1_score_view);
            player1ScoreView.setText(player1Name + " Score: " + player1Score);

            // 리더보드 업데이트 및 표시
            updateLeaderboard(player1Name, player1Score);
        }

        // 스크린 전체화면 설정(appbar, homebar 숨기기)
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        Button playAgainButton = findViewById(R.id.playagain);
        Button quitButton = findViewById(R.id.quitButton);

        playAgainButton.setOnClickListener(v -> {
            Intent intent = new Intent(GameOverActivity.this, NicknameActivity.class);
            // 어떤 모드인지 상관없이 NicknameActivity로 다시 이동
            intent.putExtra("MODE", whichMode);
            startActivity(intent);
        });

        quitButton.setOnClickListener(v -> {
            Intent intent = new Intent(GameOverActivity.this, MainActivity.class);
            startActivity(intent);
        });

        setupOnBackPressedDispatcher();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isFirstTouch && event.getAction() == MotionEvent.ACTION_DOWN) {
            isFirstTouch = false;

            // 첫 번째 터치 이후 리더보드와 버튼을 표시
            displayLeaderboard();

            TextView leaderboardTextView = findViewById(R.id.leaderboard_text);
            TextView leaderboardScoresView = findViewById(R.id.leaderboard_scores);
            Button playAgainButton = findViewById(R.id.playagain);
            Button quitButton = findViewById(R.id.quitButton);

            leaderboardTextView.setVisibility(View.VISIBLE);
            leaderboardScoresView.setVisibility(View.VISIBLE);
            playAgainButton.setVisibility(View.VISIBLE);
            quitButton.setVisibility(View.VISIBLE);

            // 첫 번째 화면의 텍스트는 숨김 처리
            TextView winnerText = findViewById(R.id.winner_text);
            TextView winnerNameText = findViewById(R.id.winner);
            TextView playerScoreText = findViewById(R.id.playerscore_text);
            TextView player1ScoreView = findViewById(R.id.player1_score_view);
            TextView player2ScoreView = findViewById(R.id.player2_score_view);

            if (winnerText != null) {
                winnerText.setVisibility(View.GONE);
            }
            if (winnerNameText != null) {
                winnerNameText.setVisibility(View.GONE);
            }
            if (playerScoreText != null) {
                playerScoreText.setVisibility(View.GONE);
            }
            if (player1ScoreView != null) {
                player1ScoreView.setVisibility(View.GONE);
            }
            if (player2ScoreView != null) {
                player2ScoreView.setVisibility(View.GONE);
            }
        }
        return super.onTouchEvent(event);
    }

    private void updateLeaderboard(String playerName, int score) {
        if (playerName == null || playerName.isEmpty()) {
            return;
        }
        SharedPreferences preferences = getSharedPreferences("Leaderboard", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        List<String> leaderboard = new ArrayList<>();

        // 기존 리더보드 읽기
        for (int i = 0; i < 10; i++) {
            String entry = preferences.getString("entry_" + i, null);
            if (entry != null) {
                leaderboard.add(entry);
            }
        }

        // 새로운 점수 추가
        leaderboard.add(playerName + ":" + score);

        // 리더보드 정렬 - 점수를 기준으로 내림차순 정렬
        leaderboard.sort((a, b) -> {
            int scoreA = Integer.parseInt(a.split(":")[1]);
            int scoreB = Integer.parseInt(b.split(":")[1]);
            return Integer.compare(scoreB, scoreA); // 내림차순 정렬
        });

        // 상위 10개 점수 저장
        for (int i = 0; i < Math.min(10, leaderboard.size()); i++) {
            editor.putString("entry_" + i, leaderboard.get(i));
        }
        editor.apply();
    }

    private void displayLeaderboard() {
        SharedPreferences preferences = getSharedPreferences("Leaderboard", MODE_PRIVATE);
        StringBuilder leaderboardText = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            String entry = preferences.getString("entry_" + i, null);
            if (entry != null) {
                leaderboardText.append(entry).append("\n");
            }
        }

        TextView leaderboardScoresView = findViewById(R.id.leaderboard_scores);
        leaderboardScoresView.setText(leaderboardText.toString());
    }

    private void setupOnBackPressedDispatcher() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // 뒤로가기 버튼 무효화
            }
        });
    }
}
