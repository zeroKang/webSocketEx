package org.zerock.web;


import lombok.extern.log4j.Log4j;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@ServerEndpoint("/chat")
@Log4j
public class ChatEnd {

    private static final Set<ChatEnd> connections = new CopyOnWriteArraySet<>();

    private Session session;

    @OnOpen
    public void open(Session session) {

        log.info("open..........");
        log.info(session);

        log.info("THIS: " + this);

        connections.add(this);

        this.session = session;
        broadcast("joined");

    }

    @OnClose
    public void onClose() {
        log.info("close..........");
    }

    @OnMessage
    public void onMessage(String msg)throws Throwable {
        log.info("message.........." + msg);
        broadcast(msg);
    }

    @OnError
    public void onError(Throwable t) throws Throwable {

        log.error("Chat Error: " + t.toString(), t);
    }

    private static void broadcast(String msg) {
        for (ChatEnd client : connections) {
            try {
                synchronized (client) {
                    client.session.getBasicRemote().sendText(msg);
                }
            } catch (IOException e) {
                log.debug("Chat Error: Failed to send message to client", e);
                connections.remove(client);
                try {
                    client.session.close();
                } catch (IOException e1) {
                    // Ignore
                }
                String message = String.format("* %s %s",
                        client, "has been disconnected.");
                broadcast(message);
            }
        }
    }
}
