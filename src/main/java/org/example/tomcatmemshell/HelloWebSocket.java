package org.example.tomcatmemshell;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint("/hello-websocket")
public class HelloWebSocket {
    @OnOpen
    public void onOpen(Session session) {
        System.out.println("连接建立：" + session.getId());
    }

    @OnMessage
    public void onMessage(String msg, Session session) {
        System.out.println("收到消息：" + msg);
        session.getAsyncRemote().sendText("你发送了：" + msg);
    }

    @OnClose
    public void onClose(Session session) {
        System.out.println("连接关闭：" + session.getId());
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        throwable.printStackTrace();
    }
}
