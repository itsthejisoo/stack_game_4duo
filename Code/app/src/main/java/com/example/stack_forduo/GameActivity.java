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

import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class GameActivity extends AppCompatActivity {
    private GameView player1GameView; // player1 화면
    private GameView player2GameView; // player2 화면
    private Player player1, player2;
    private int whichMode;
    private Socket socket;
    private PrintWriter out;
    private Scanner in;
    public boolean isOpponentConnected = false; // 상대방 연결 여부 확인 변수
    public String clientId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 모드 확인
        Intent intent = getIntent();
        whichMode = intent.getIntExtra("MODE", 0);

        switch (whichMode) {
            case 0: // 싱글 모드
                setContentView(R.layout.activity_game_single);
                break;
            case 1: // 멀티 모드
                setContentView(R.layout.activity_game_multi);
                player2 = new Player(intent.getStringExtra("PLAYER2_NAME"));
                break;
            case 2: // 서버 모드
                setContentView(R.layout.activity_game_server);
                player2 = new Player(intent.getStringExtra("PLAYER2_NAME"));
                connectToServer(); // 서버 연결 로직 수행
                break;
            default:
                throw new IllegalArgumentException("Unknown mode");
        }

        // 플레이어 1 설정
        player1 = new Player(intent.getStringExtra("PLAYER1_NAME"));

        // 플레이어 1의 게임 화면 설정
        EditText player1ScoreView = findViewById(R.id.player1_score);
        FrameLayout player1Layout = findViewById(R.id.player1);
        player1GameView = new GameView(this, player1, player1ScoreView, out, null);
        player1Layout.addView(player1GameView);

        if (whichMode == 1 || whichMode == 2) {
            EditText player2ScoreView = findViewById(R.id.player2_score);
            FrameLayout player2Layout = findViewById(R.id.player2);
            // 멀티 모드일 경우 client ID를 null로 반환하고 서버 모드일 경우 clientID를 반환
            player2GameView = new GameView(this, player2, player2ScoreView, out, whichMode == 1 ? null : clientId);
            player2Layout.addView(player2GameView);
        }

        // 전체화면 설정
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        setupOnBackPressedDispatcher();

        // 상대방 연결 대기
        if (whichMode == 2) {
            checkOpponentConnection();
        }
    }

    private void setupOnBackPressedDispatcher() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // 게임 일시정지
                onPause();

                // 팝업창 표시
                new AlertDialog.Builder(GameActivity.this)
                        .setTitle("게임 일시정지")
                        .setMessage("계속하시겠습니까?")
                        // 게임 재개
                        .setNegativeButton("계속하기", (dialog, which) -> {
                            onResume();
                        })
                        // 메인 화면으로 이동
                        .setPositiveButton("종료", (dialog, which) -> {
                            Intent intent = new Intent(GameActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                        })
                        .setCancelable(false) // 팝업창 외부를 눌러도 창이 닫히지 않게
                        .show();
            }
        });
    }

    public void gameOver() {
        Intent intent = new Intent(GameActivity.this, GameOverActivity.class);

        // 승자와 각 플레이어의 점수를 가져옴
        intent.putExtra("WINNER", player1.getScore() > (player2 != null ? player2.getScore() : 0)
                ? player1.getName()
                : (player2 != null ? player2.getName() : player1.getName()));
        intent.putExtra("PLAYER1_NAME", player1.getName());
        intent.putExtra("PLAYER1_SCORE", player1.getScore());

        if (player2 != null) {
            intent.putExtra("PLAYER2_NAME", player2.getName());
            intent.putExtra("PLAYER2_SCORE", player2.getScore());
        }

        intent.putExtra("MODE", whichMode);
        startActivity(intent);
    }

    // whichMode 값을 반환하는 메서드 추가
    public int whichMode() {
        return whichMode;
    }

    // player1 객체를 반환하는 메서드 추가
    public Player getPlayer1() {
        return player1;
    }

    private void connectToServer() {
        new Thread(() -> {
            try {
                // 서버 소켓 연결
                socket = new Socket("10.0.2.2", 4531); // 10.0.2.2는 로컬 아이피
                out = new PrintWriter(socket.getOutputStream(), true); // 소켓 출력 스트림
                in = new Scanner(socket.getInputStream()); // 소켓 입력 스트림

                // 클라이언트 ID 생성 및 서버로 전송
                String clientId = "Client_" + System.currentTimeMillis(); // 고유 ID 생성
                sendClientIdToGameView(clientId); // GameView에 클라이언트 ID 전달

                // 서버로부터 메시지 수신 루프
                listenToServerMessages(clientId); // 서버 메시지 처리 메서드 호출
            } catch (Exception e) {
                Log.e("GameActivity", "서버 연결 실패", e);
                ServerConnectionFail(); // 서버 연결 실패 처리
            }
        }).start();
    }

    // 클라이언트 ID를 GameView에 전달
    private void sendClientIdToGameView(String clientId) {
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
    }

    // 서버로부터 메시지 수신 및 처리
    private void listenToServerMessages(String clientId) {
        while (in.hasNextLine()) {
            String message = in.nextLine();
            Log.d("GameActivity", "서버로부터 메시지 수신: " + message);
            handleServerMessage(message, clientId); // 메시지 처리 메서드 호출
        }
    }

    // 서버에서 수신한 메시지 처리
    private void handleServerMessage(String message, String clientId) {
        if (message.equals("ID_REQUEST")) {
            sendClientIdToServer(clientId); // 서버로 클라이언트 ID 전송
        } else if (message.equals("WAITING")) {
            showWaitingToast(); // 대기 상태 처리
        } else if (message.equals("CONNECTED")) {
            handleOpponentConnection(); // 상대방 연결 처리
        } else if (message.contains(",")) {
            processOpponentBlock(message); // 블록 정보 처리
        }
    }

    // 클라이언트 ID를 서버로 전송
    private void sendClientIdToServer(String clientId) {
        out.println(clientId);
        Log.d("GameActivity", "서버로 클라이언트 ID 전송: " + clientId);
    }

    // 대기 상태 표시
    private void showWaitingToast() {
        runOnUiThread(() -> Toast.makeText(this, "상대방을 기다리는 중...", Toast.LENGTH_SHORT).show());
    }

    // 상대방 연결 완료 처리
    private void handleOpponentConnection() {
        runOnUiThread(() -> {
            isOpponentConnected = true;
            Toast.makeText(this, "상대방과 연결되었습니다!", Toast.LENGTH_SHORT).show();
        });
    }

    // 상대방 블록 정보 처리
    private void processOpponentBlock(String message) {
        String[] parts = message.split(",");
        try {
            int receivedX = Integer.parseInt(parts[0]);
            int receivedColor = Integer.parseInt(parts[1]);
            updateOpponentBlockUI(receivedX, receivedColor); // UI 업데이트
        } catch (NumberFormatException e) {
            Log.e("GameActivity", "잘못된 블록 데이터 형식: " + message, e);
        }
    }

    // 상대방 블록을 UI에 반영
    private void updateOpponentBlockUI(int receivedX, int receivedColor) {
        runOnUiThread(() -> {
            if (player1GameView != null) {
                player1GameView.addOpponentBlock(receivedX, receivedColor);
            }
        });
    }

    // 서버 연결 실패 시 처리
    private void ServerConnectionFail() {
        runOnUiThread(() -> {
            Toast.makeText(this, "서버 연결 실패! 메인 화면으로 돌아갑니다.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(GameActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // GameActivity 종료
        });
    }

    // 상대방과 연결 확인.
    // 새로운 쓰레드를 생성한 이유: 상대방과 본인의 게임 정보를 서로에게 전송하고, 상대방과 연결이 되었는지 동시에 확인하기 위해
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
                        whichMode = 0;
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

}
