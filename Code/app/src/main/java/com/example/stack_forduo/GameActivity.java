package com.example.stack_forduo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.stack_forduo.GameView;
import com.example.stack_forduo.Player;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class GameActivity extends AppCompatActivity {
    private GameView player1GameView; // player1 화면
    private GameView player2GameView; // player2 화면
    private Player player1, player2;
    private boolean isSingleMode;
    private Socket socket;
    private PrintWriter out;
    private Scanner in;
    public boolean isOpponentConnected = false;
    public String clientId;

    public PrintWriter getOut() {
        return out;
    }
    // Getter for isSingleMode
    public boolean isSingleMode() {
        return isSingleMode;
    }
    public Player getPlayer1() {
        return player1;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 모드 확인
        Intent intent = getIntent();
        String mode = intent.getStringExtra("MODE");
        isSingleMode = "SINGLE".equals(mode);

        if (!isSingleMode) {
            setContentView(R.layout.activity_game_multi);

            player2 = new Player("Player 2");

            // 상대방 연결 처리
            connectToServer();
        } else {
            setContentView(R.layout.activity_game_single);
        }

        // 플레이어1 초기화
        player1 = new Player("Player 1");
        EditText player1ScoreView = findViewById(R.id.player1_score);
        FrameLayout player1Layout = findViewById(R.id.player1);
        player1GameView = new GameView(this, player1, player1ScoreView, out, null); // PrintWriter 전달
        player1Layout.addView(player1GameView);

        if (!isSingleMode) {
            // 플레이어2 초기화
            EditText player2ScoreView = findViewById(R.id.player2_score);
            FrameLayout player2Layout = findViewById(R.id.player2);
            player2GameView = new GameView(this, player2, player2ScoreView, out, clientId); // PrintWriter 전달
            player2Layout.addView(player2GameView);
        }

        // 전체화면 설정
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );

        setupOnBackPressedDispatcher();

        // 상대방 연결 대기
        if (!isSingleMode) {
            checkOpponentConnection();
        }
    }

    private void connectToServer() {
        new Thread(() -> {
            try {
                // 서버 소켓 연결
                socket = new Socket("10.0.2.2", 4531); // 10.0.2.2는 로컬 아이피
                out = new PrintWriter(socket.getOutputStream(), true); // 소켓 출력 스트림
                in = new Scanner(socket.getInputStream()); // 소켓 입력 스트림

                // 클라이언트 ID 생성 및 서버로 전송
                clientId = "Client_" + System.currentTimeMillis(); // 고유 ID 생성

                // `GameView`에 `out`과 `clientId` 전달
                runOnUiThread(() -> {
                    if (player1GameView != null) {
                        player1GameView.setOut(out);
                        player1GameView.setClientId(clientId); // GameView에 clientId 전달
                    }
                    if (player2GameView != null) {
                        player2GameView.setOut(out);
                        player2GameView.setClientId(clientId); // GameView에 clientId 전달
                    }
                });

                // 서버로부터 메시지 수신 루프
                while (in.hasNextLine()) {
                    String message = in.nextLine();
                    android.util.Log.d("GameActivity", "서버로부터 메시지 수신: " + message);

                    if (message.equals("ID_REQUEST")) {
                        // 서버에서 ID 요청 시 다시 전송
                        out.println(clientId);
                        android.util.Log.d("GameActivity", "서버로 클라이언트 ID 전송: " + clientId);
                    } else if (message.equals("WAITING")) {

                        // 대기 상태 표시
                        runOnUiThread(() -> Toast.makeText(this, "상대방을 기다리는 중...", Toast.LENGTH_SHORT).show());
                    }else if (message.equals("CONNECTED")) {
                        // 매칭 완료 메시지 처리
                        runOnUiThread(() -> {
                            isOpponentConnected = true;
                            Toast.makeText(this, "상대방과 연결되었습니다!", Toast.LENGTH_SHORT).show();
                        });
                    } else if (message.contains(",")) {
                        // 블록 정보 메시지 처리
                        String[] parts = message.split(",");
                        try {
                            int receivedX = Integer.parseInt(parts[0]);
                            int receivedColor = Integer.parseInt(parts[1]);

                            // 상대방 블록을 UI에 반영
                            runOnUiThread(() -> {
                                if (player1GameView != null) {
                                    player1GameView.addOpponentBlock(receivedX, receivedColor);
                                }
                            });
                        } catch (NumberFormatException e) {
                            android.util.Log.e("GameActivity", "잘못된 블록 데이터 형식: " + message, e);
                        }
                    }
                }
            } catch (Exception e) {
                android.util.Log.e("GameActivity", "서버 연결 실패", e);
                runOnUiThread(() -> {
                    Toast.makeText(this, "서버 연결 실패! 메인 화면으로 돌아갑니다.", Toast.LENGTH_SHORT).show();
                    // MainActivity로 이동
                    Intent intent = new Intent(GameActivity.this, MainActivity.class);
                    startActivity(intent);
                    // GameActivity 종료
                    finish();
                });
            }
        }).start();
    }

    private void checkOpponentConnection() {
        new Thread(() -> {
            try {
                // 상대방 연결 대기
                int waitTime = 0;
                while (!isOpponentConnected && waitTime < 100000) {
                    Thread.sleep(1000);
                    waitTime += 1000;
                }

                if (!isOpponentConnected) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "상대방 연결 실패. 싱글 모드로 전환합니다.", Toast.LENGTH_LONG).show();
                        isSingleMode = true;
                        restartSingleMode();
                    });
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void restartSingleMode() {
        Intent intent = new Intent(GameActivity.this, GameActivity.class);
        intent.putExtra("MODE", "SINGLE");
        startActivity(intent);
        finish();
    }

    // 게임 일시 정지
    @Override
    protected void onPause() {
        super.onPause();
        if (player1 != null) {
            player1GameView.pauseGame();
        }
        if (player2GameView != null) {
            player2GameView.pauseGame();
        }
    }

    // 게임이 다시 시작
    @Override
    protected void onResume() {
        super.onResume();
        if (player1 != null) {
            player1GameView.resumeGame();
        }
        if (player2GameView != null) {
            player2GameView.resumeGame();
        }
    }

    // 뒤로가기 버튼을 누르면 일시정지 팝업창 표시
    private void setupOnBackPressedDispatcher() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // 게임 일시정지
                if (player1 != null) {
                    player1GameView.pauseGame();
                }
                if (player2GameView != null) {
                    player2GameView.pauseGame();
                }

                // 팝업창 표시
                new AlertDialog.Builder(GameActivity.this)
                        .setTitle("게임 일시정지")
                        .setMessage("계속하시겠습니까?")

                        // 게임 재개
                        .setNegativeButton("계속하기", (dialog, which) -> {
                            if (player1 != null) {
                                player1GameView.resumeGame();
                            }
                            if (player2GameView != null) {
                                player2GameView.resumeGame();
                            }
                        })
                        // 메인 화면으로 이동
                        .setPositiveButton("종료", (dialog, which) -> {
                            Intent intent = new Intent(GameActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                        })
                        .setCancelable(false)   // 팝업창 외부를 눌러도 창이 닫히지 않게
                        .show();
            }
        });
    }

    public void gameOver() {
        Intent intent = new Intent(GameActivity.this, GameOverActivity.class);

        // 승자와 각 플레이어의 점수를 가져옴
        intent.putExtra("WINNER", player1.getScore() > (player2 != null ? player2.getScore() : 0)
                ? player1.getName() : (player2 != null ? player2.getName() : "Player 1"));
        intent.putExtra("PLAYER1_SCORE", player1.getScore());
        if (player2 != null) {
            intent.putExtra("PLAYER2_SCORE", player2.getScore());
        }
        intent.putExtra("MODE", isSingleMode);
        startActivity(intent);
    }

}