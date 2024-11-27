package com.example.stack_forduo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.stack_forduo.GameActivity;
import com.example.stack_forduo.Player;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class GameView extends View {
    private Paint blockPaint;
    private int screenWidth, screenHeight;
    private final int blockWidth = 150;
    private final int blockHeight = 50;
    private int blockX, blockY;
    private boolean blockMovingRight = true;
    private int blockSpeed = 8;

    private final List<BlockInfo> blocks = new ArrayList<>();
    private int towerHeight = 0;
    private Player player;
    private EditText scoreView;
    private GameActivity gameActivity;
    private boolean isPaused = false;
    private String clientId;
    private final Random random = new Random();
    private final int[] blockColors = {
            Color.parseColor("#2B4C40"), Color.parseColor("#547734"),
            Color.parseColor("#485726"), Color.parseColor("#8C924F")
    };

    // Socket variables
    private Socket socket;
    private PrintWriter out;
    private Scanner in;
    public void setOut(PrintWriter out) {
        this.out = out; // `GameActivity`에서 전달받은 `out`을 설정
    }

    public GameView(Context context, Player player, EditText scoreView, PrintWriter out, String clientId) {
        super(context);
        this.player = player;
        this.scoreView = scoreView;
        this.out = out;  // GameActivity에서 전달받은 PrintWriter
        this.gameActivity = (GameActivity) context;
        this.clientId = clientId;
        init();
    }
    // clientId를 설정하는 메서드 추가
    public void setClientId(String clientId) {
        this.clientId = clientId; // 외부에서 clientId 설정 가능
    }

    private void init() {
        blockPaint = new Paint();
        updateBlockColor();

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        screenWidth = displayMetrics.widthPixels;

        if (gameActivity.whichMode() == 0) {
            screenHeight = displayMetrics.heightPixels;
        } else {
            screenHeight = displayMetrics.heightPixels / 2;
        }

        blockY = screenHeight - blockHeight;
        blockX = 0;
    }



    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 블록 움직임
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

        // 블록 그리기
        canvas.drawRect(blockX,
                blockY - towerHeight * blockHeight,
                blockX + blockWidth,
                blockY - (towerHeight - 1) * blockHeight,
                blockPaint);

        for (int i = 0; i < blocks.size(); i++) {
            BlockInfo block = blocks.get(i);
            Paint blockPaint = new Paint();
            blockPaint.setColor(block.color);
            canvas.drawRect(block.x, blockY - i * blockHeight, block.x + blockWidth, blockY - (i - 1) * blockHeight, blockPaint);
        }

        // 일시정지 상태가 아닐 때만 화면 갱신
        if (!isPaused) {
            invalidate();
        }
    }

    private boolean processBlock(int x, int color) {
        // 블록 추가
        blocks.add(new BlockInfo(x, color));
        towerHeight++;

        // 게임 종료 조건 확인
        if (blocks.size() > 1) {
            int previousBlockCenterX = blocks.get(blocks.size() - 2).x + blockWidth / 2;
            int newBlockCenterX = blocks.get(blocks.size() - 1).x + blockWidth / 2;
            double maxOffset = (double) blockWidth / 2;

            if (Math.abs(newBlockCenterX - previousBlockCenterX) > maxOffset) {
                if (out != null) {
                    new Thread(() -> {
                        try {
                            out.println("GAME_OVER|" + clientId); // 게임 종료 메시지와 클라이언트 ID 전송
                            out.flush();
                            android.util.Log.d("GameView", "서버에게 게임 종료 알림 전송 완료");
                        } catch (Exception e) {
                            android.util.Log.e("GameView", "게임 종료 메시지 전송 실패", e);
                        }
                    }).start();
                }
                gameActivity.gameOver(); // 게임 종료
                return false;
            }
        }
        player.incrementScore();
        scoreView.setText(String.valueOf(player.getScore()));

        // 블록 속도 증가
        if (towerHeight == 10) {
            blockSpeed = 11;
        }
        if (towerHeight % 10 == 0) {
            blockSpeed += 1;
        }

        // 화면 크기 초과 시 블록 제거
        if (towerHeight * blockHeight > screenHeight / 4) {
            blocks.remove(0);
            towerHeight--;
        }

        // 블록 색상 업데이트 및 화면 다시 그리기
        updateBlockColor();
        invalidate();

        return true; // 블록이 정상적으로 처리됨
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (gameActivity.whichMode() == 2 && !gameActivity.isOpponentConnected) {
            // Toast.makeText(getContext(), "상대방과 매칭 중입니다. 잠시만 기다려 주세요.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (gameActivity.whichMode() == 2 && player == gameActivity.getPlayer1()) {
            // Toast.makeText(getContext(), "상대방 영역입니다.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            int newBlockCenterX = blockX + blockWidth / 2;
            int blockColor = blockPaint.getColor();

            if (out != null) {
                final String message = blockX + "," + blockColor;
                new Thread(() -> {
                    try {
                        out.println(message);
                        android.util.Log.d("GameView", "블록 정보 전송 성공: " + message);
                    } catch (Exception e) {
                        android.util.Log.e("GameView", "블록 정보 전송 실패", e);
                    }
                }).start();
            }
            processBlock(blockX, blockColor);

            // 상대방에게 블록 정보 전송

            return true;
        }
        return false;
    }



    public void addOpponentBlock(int x, int color) {
        processBlock(x, color); // 상대방 블록 처리
    }

    // 게임 일시정지
    public void pauseGame() {
        isPaused = true;
    }

    // 게임 재개
    public void resumeGame() {
        isPaused = false;
        invalidate();
    }

    private void updateBlockColor() {
        int randomColor = blockColors[random.nextInt(blockColors.length)];
        blockPaint.setColor(randomColor);
    }
    private static class BlockInfo {
        int x;
        int color;

        BlockInfo(int x, int color) {
            this.x = x;
            this.color = color;
        }
    }
}