package com.toucha.analytics.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.web.socket.WebSocketSession;

/**
 * Store webscoket client in hash map
 * 
 * @author senhui.li
 */
public class WebSocketSessionUtils {

    private static Map<String, WebSocketSession> clients = new ConcurrentHashMap<>();

    public static void add(String inquiryId, WebSocketSession session) {
        clients.put(inquiryId, session);
    }

    public static WebSocketSession get(String inquiryId) {
        return clients.get(inquiryId);
    }

    public static void remove(String inquiryId) {
        clients.remove(inquiryId);
    }

    public static Map<String, WebSocketSession> getClients() {
        return clients;
    }

    public static int getSize() {
        return clients.size();
    }

}
