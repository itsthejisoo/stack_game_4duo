package com.example.stack_forduo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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

    private final Random random = new Random();
    private final int[] blockColors = {
            Color.parseColor("#2B4C40"), Color.parseColor("#547734"),
            Color.parseColor("#485726"), Color.parseColor("#8C924F")
    };

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
        if (((GameActivity) getContext()).isSingleMode()) {
            screenHeight = displayMetrics.heightPixels; // Single 모드일 때 전체 높이 사용
        } else {
            screenHeight = displayMetrics.heightPixels / 2; // Double 모드일 때 절반 높이 사용
        }

        blockY = screenHeight - blockHeight;
        blockX = 0;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
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

            blocks.add(new BlockInfo(blockX, blockPaint.getColor()));
            towerHeight++;
            player.incrementScore();
            scoreView.setText(String.valueOf(player.getScore()));

            if (towerHeight == 10) {
                blockSpeed = 11;
            }
            if (towerHeight % 10 == 0) {
                blockSpeed += 1;
            }

            updateBlockColor();

            if (towerHeight * blockHeight > screenHeight / 4) {
                blocks.remove(0);
                towerHeight--;
            }
        }
        return true;
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
