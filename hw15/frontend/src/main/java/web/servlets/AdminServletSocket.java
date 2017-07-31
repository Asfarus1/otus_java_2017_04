package web.servlets;

import org.apache.catalina.websocket.StreamInbound;
import org.apache.catalina.websocket.WebSocketServlet;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;

public class AdminServletSocket extends WebSocketServlet {
    @Override
    protected StreamInbound createWebSocketInbound(String s) {
        return null;
    }
}
