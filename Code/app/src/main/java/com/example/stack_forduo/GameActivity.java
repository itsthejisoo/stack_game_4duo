package com.example.stack_forduo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameActivity extends AppCompatActivity {
    private Player player1, player2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        player1 = new Player("Player 1");
        player2 = new Player("Player 2");

        // 플레이어 점수
        EditText player1ScoreView = findViewById(R.id.player1_score);
        EditText player2ScoreView = findViewById(R.id.player2_score);

        FrameLayout player1Layout = findViewById(R.id.player1);
        FrameLayout player2Layout = findViewById(R.id.player2);

        GameView player1GameView = new GameView(this, player1, player1ScoreView);
        GameView player2GameView = new GameView(this, player2, player2ScoreView);

        player1Layout.addView(player1GameView);
        player2Layout.addView(player2GameView);

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

    public void gameOver() {
        Intent intent = new Intent(GameActivity.this, GameOverActivity.class);

        intent.putExtra("WINNER", player1.getScore() > player2.getScore() ? player1.getName() : player2.getName());
        intent.putExtra("PLAYER1_SCORE", player1.getScore());
        intent.putExtra("PLAYER2_SCORE", player2.getScore());

        startActivity(intent);
    }

    class GameView extends View {

        private Paint blockPaint;
        private int screenWidth, screenHeight;
        private final int blockWidth = 150; // 가로 길이 150
        private final int blockHeight = 50; // 세로 길이 50
        private int blockX, blockY;
        private boolean blockMovingRight = true;
        private int blockSpeed = 10;

        // 각 블록의 X 좌표와 색상 정보를 저장하는 리스트
        private final List<BlockInfo> blocks = new ArrayList<>();
        private int towerHeight = 0; // 탑 높이
        private Player player;
        private EditText scoreView;
        private GameActivity gameActivity;
        private final Random random = new Random();
        private final int[] blockColors = {Color.parseColor("#2B4C40"), Color.parseColor("#547734"), Color.parseColor("#485726"), Color.parseColor("#8C924F")};

        public GameView(Context context, Player player, EditText scoreView) {
            super(context);
            this.player = player;
            this.scoreView = scoreView;
            this.gameActivity = (GameActivity) context;
            init();
        }

        private void init() {
            blockPaint = new Paint();
            updateBlockColor();

            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            screenWidth = displayMetrics.widthPixels;
            screenHeight = displayMetrics.heightPixels;

            blockY = displayMetrics.heightPixels / 2 - blockHeight;
            blockX = 0;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            // 블록 좌우로 움직임
            if (blockMovingRight) {
                blockX += blockSpeed;
                if (blockX + blockWidth > screenWidth) {
                    blockMovingRight = false;
                }
            } else {
                blockX -= blockSpeed;
                if (blockX < 0) {
                    blockMovingRight = true;
                }
            }

            // 움직이는 블록
            canvas.drawRect(blockX, blockY - towerHeight * blockHeight, blockX + blockWidth, blockY - (towerHeight - 1) * blockHeight, blockPaint);

            // 쌓인 블록들 그리기
            for (int i = 0; i < blocks.size(); i++) {
                BlockInfo block = blocks.get(i);
                Paint blockPaint = new Paint();
                blockPaint.setColor(block.color);
                canvas.drawRect(block.x, blockY - i * blockHeight, block.x + blockWidth, blockY - (i - 1) * blockHeight, blockPaint);
            }

            invalidate();
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                int newBlockCenterX = blockX + blockWidth / 2;
                if (!blocks.isEmpty()) {
                    int previousBlockCenterX = blocks.get(blocks.size() - 1).x + blockWidth / 2;

                    double maxOffset = (double) blockWidth / 2;
                    if (Math.abs(newBlockCenterX - previousBlockCenterX) > maxOffset) {
                        gameActivity.gameOver();
                        return true;
                    }
                }

                // 새 블록 정보 추가
                blocks.add(new BlockInfo(blockX, blockPaint.getColor()));
                towerHeight++;

                player.incrementScore();
                scoreView.setText(String.valueOf(player.getScore())); // 점수 업데이트

                if (towerHeight == 10) {
                    blockSpeed = 11;
                }
                if (towerHeight % 20 == 0) {
                    blockSpeed += 1;
                }

                // 새 블록 색상 업데이트
                updateBlockColor();

                if (towerHeight * blockHeight > screenHeight / 4) {
                    blocks.remove(0);  // 가장 아래 블록 삭제
                    towerHeight--;      // 탑의 높이 감소
                }
            }
            return true;
        }

        private void updateBlockColor() {
            int randomColor = blockColors[random.nextInt(blockColors.length)];
            blockPaint.setColor(randomColor);
        }

        // 블록 정보를 저장하는 클래스
        class BlockInfo {
            int x;
            int color;

            BlockInfo(int x, int color) {
                this.x = x;
                this.color = color;
            }
        }
    }
}