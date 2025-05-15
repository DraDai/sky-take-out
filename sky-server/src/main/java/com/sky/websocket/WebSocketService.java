package com.sky.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
@ServerEndpoint("/ws/{sid}")
@Slf4j
public class WebSocketService {

    // 存储会话对象
    private static Map<String, Session> sessionMap = new HashMap<>();

    /**
     * 建立连接
     * @param session
     * @param sid
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("sid") String sid) {
        log.info("客户端: {}建立连接", sid);
        sessionMap.put(sid, session);
    }

    @OnMessage
    public void onMessage(String message, @PathParam("sid") String sid){
        log.info("收到来自客户端:{}的消息{}", sid, message);
    }

    @OnClose
    public void onClose(@PathParam("sid") String id){
        log.info("客户端:{}断开连接", id);
        sessionMap.remove(id);
    }

    /**
     * 给所有客户端发送消息
     * @param message 信息
     */
    public void sendToAllClient(String message){
        Collection<Session> values = sessionMap.values();
        values.forEach(session -> {
            try {
                session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
