package com.example.stack_forduo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    private ImageView imageView;
    private LinearLayout modeLayout; // 모드 선택 레이아웃
    private View overlayBackground; // 옅은 회색 배경 뷰
    private TextView textViewStart; // 시작 텍스트 뷰
    private TextView textViewStack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        textViewStack = findViewById(R.id.textViewStack);
        imageView = findViewById(R.id.imageView);
        textViewStart = findViewById(R.id.textViewStart);
        modeLayout = findViewById(R.id.mode_layout);
        overlayBackground = findViewById(R.id.overlay_background);

        // 초기에는 숨김처리(모드 선택)
        modeLayout.setVisibility(View.GONE);
        overlayBackground.setVisibility(View.GONE);

        // blink 애니메이션 가져오기
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.blink_animation);

        // 애니메이션 재생
        textViewStart.startAnimation(anim);

        Button singleModeButton = findViewById(R.id.singleMode_button);
        Button multiModeButton = findViewById(R.id.multiMode_button);
        Button serverModeButton = findViewById(R.id.serverMode_button);

        singleModeButton.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, NicknameActivity.class);
            intent.putExtra("MODE", 0); // SINGLE 모드
            startActivity(intent);
        });

        multiModeButton.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, NicknameActivity.class);
            intent.putExtra("MODE", 1); // MULTI 모드
            startActivity(intent);
        });

        serverModeButton.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, NicknameActivity.class);
            intent.putExtra("MODE", 2); // SERVER 모드
            startActivity(intent);
        });

        // 스크린 전체화면 설정(appbar, homebar 숨기기)
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.v("start", "onStart() called");
    }

    // activity lifecycle logcat에 출력
    @Override
    protected void onResume() {
        super.onResume();
        Log.v("resume", "onResume() called");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.v("pause", "onPause() called");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.v("stop", "onStop() called");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v("destroy", "onDestroy() called");
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            textViewStack.setVisibility(View.GONE);
            imageView.setVisibility(View.GONE);

            // 애니메이션 정지
            textViewStart.clearAnimation();
            textViewStart.setVisibility(View.GONE);

            // 모드 선택
            overlayBackground.setVisibility(View.VISIBLE);
            modeLayout.setVisibility(View.VISIBLE);
            return true;
        }
        return super.onTouchEvent(event);
    }
}
