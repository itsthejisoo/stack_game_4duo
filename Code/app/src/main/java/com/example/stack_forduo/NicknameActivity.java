package com.example.stack_forduo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class NicknameActivity extends AppCompatActivity {
    private EditText player1NameInput;
    private EditText player2NameInput;
    private int whichMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nickname);

        // 모드 확인
        whichMode = getIntent().getIntExtra("MODE", 0);

        player1NameInput = findViewById(R.id.player1_name);
        player2NameInput = findViewById(R.id.player2_name);

        // 싱글 모드일 때 두 번째 입력란 숨기기
        if (whichMode == 0) {
            player2NameInput.setVisibility(View.GONE);
        }

        Button startGameButton = findViewById(R.id.start_game_button);
        startGameButton.setOnClickListener(view -> {
            Intent intent = new Intent(NicknameActivity.this, GameActivity.class);
            intent.putExtra("MODE", whichMode);
            intent.putExtra("PLAYER1_NAME", player1NameInput.getText().toString().trim());
            if (whichMode != 0) {
                intent.putExtra("PLAYER2_NAME", player2NameInput.getText().toString().trim());
            }
            startActivity(intent);
            finish();
        });

        // 전체화면 설정
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }
}
