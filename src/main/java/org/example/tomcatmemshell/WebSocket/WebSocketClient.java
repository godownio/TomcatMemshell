package org.example.tomcatmemshell.WebSocket;

import javax.websocket.*;
import java.net.URI;
import java.util.Scanner;

@ClientEndpoint
public class WebSocketClient {

    @OnOpen
    public void onOpen(Session session) {
        System.out.println("✅ Connected to server");
    }

    @OnMessage
    public void onMessage(String message) {
        System.out.println("📥 Received: " + message);
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        System.out.println("❌ Connection closed: " + reason);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.out.println("⚠️ Error: " + throwable.getMessage());
    }

    public static void main(String[] args) {
        String uri = "ws://127.0.0.1:8080/TomcatMemshell_war_exploded/evilWebSocket";

        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            Session session = container.connectToServer(WebSocketClient.class, new URI(uri));

            // 控制台输入发送消息
            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.print("📤 Send: ");
                String msg = scanner.nextLine();
                if ("exit".equalsIgnoreCase(msg)) break;
                session.getAsyncRemote().sendText(msg);
            }

            session.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}