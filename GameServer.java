import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

// 서버 설정 및 주요 변수
public class GameServer {
    private static final int PORT = 4531; // 서버 포트 번호
    private static Map<String, ClientHandler> connectedClients = new ConcurrentHashMap<>();
    private static ExecutorService executor = Executors.newCachedThreadPool();

    public static void main(String[] args) {
        System.out.println("[서버 시작] 포트: " + PORT);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                executor.submit(new ClientHandler(clientSocket)); // 클라이언트 처리를 스레드로 실행
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class ClientHandler implements Runnable {
        private Socket socket;
        private String clientId;
        private PrintWriter out;
        private BufferedReader in;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // 클라이언트 ID 설정
                out.println("ID_REQUEST");
                clientId = in.readLine();
                if (clientId == null || clientId.isEmpty()) {
                    System.out.println("[에러] 클라이언트 ID가 null입니다.");
                    return;
                }

                synchronized (connectedClients) {
                    connectedClients.put(clientId, this); // 클라이언트 등록
                    // System.out.println("[새 연결] 클라이언트 ID: " + clientId);
                    // System.out.println("[디버그] 매칭 확인 전 isPaired(clientId): " +
                    // !isPaired(clientId));
                    // 매칭 여부 확인
                    if (!isPaired(clientId)) {
                        out.println("WAITING"); // 대기 메시지 전송
                    } else {
                        out.println("CONNECTED"); // 매칭 완료 메시지 전송
                        // System.out.println("[매칭 완료] 클라이언트 ID: " + clientId);
                        broadcastPairingInfo(clientId); // 매칭된 상대방 정보 전송
                    }
                    // System.out.println("[디버그] 매칭 확인 후 isPaired(clientId): " +
                    // !isPaired(clientId));
                }

                // out.println("CONNECTED");
                scheduleMessageCheck();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private boolean isPaired(String clientId) {
            // 연결된 클라이언트 중 상대방이 있는지 확인
            synchronized (connectedClients) {
                // System.out.println("[디버그] 연결된 클라이언트 수: " + connectedClients.size());
                // System.out.println("[디버그] 연결된 클라이언트 목록: " + connectedClients.keySet());
                return connectedClients.size() >= 2; // 2명 이상 연결된 경우 매칭된 상태로 간주
            }
        }

        private void broadcastPairingInfo(String clientId) {
            // 매칭된 두 클라이언트를 찾아서 상대방 ID를 전송
            connectedClients.forEach((id, handler) -> {
                if (!id.equals(clientId)) { // 현재 클라이언트를 제외한 상대방에게
                    handler.sendMessage("CONNECTED");
                }
            });
        }

        // 주기적으로 클라이언트의 메시지를 확인
        private void scheduleMessageCheck() {
            ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
            scheduler.scheduleAtFixedRate(() -> {
                try {
                    if (in.ready()) { // 메시지가 도착한 경우 처리
                        String message = in.readLine();
                        if (message.startsWith("GAME_OVER")) {
                            String clientId = message.split("\\|")[1];
                            connectedClients.remove(clientId);
                            System.out.println("[게임 종료] 클라이언트 ID: " + clientId);
                        }

                        if (message != null) {
                            System.out.println("[수신] " + clientId + " -> " + message);
                            broadcastMessage(clientId, message);
                        }
                    }
                } catch (IOException e) {
                    System.err.println("[에러] 클라이언트 메시지 확인 실패: " + clientId);
                    e.printStackTrace();
                    scheduler.shutdown(); // 연결 종료 시 스케줄러 중지
                }
            }, 0, 500, TimeUnit.MILLISECONDS); // 500ms 간격으로 메시지 확인
        }

        // 다른 클라이언트에게 메시지 전송
        private void broadcastMessage(String senderId, String message) {
            connectedClients.forEach((id, handler) -> {
                if (!id.equals(senderId)) { // 자신에게는 전송하지 않음
                    handler.sendMessage(message);
                }
            });
        }

        // 클라이언트로 메시지 전송
        private void sendMessage(String message) {
            out.println(message);
        }
    }
}
