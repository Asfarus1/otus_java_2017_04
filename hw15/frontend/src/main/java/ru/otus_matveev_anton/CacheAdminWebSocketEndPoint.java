package ru.otus_matveev_anton;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.otus_matveev_anton.genaral.Addressee;
import ru.otus_matveev_anton.genaral.MessageSystemClient;

import javax.servlet.http.HttpServlet;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint(value = "/cacheAdmin", configurator = FrontendServerEndpointConfigurator.class)
public class CacheAdminWebSocketEndPoint extends HttpServlet{
    private final static Logger log = LogManager.getLogger(CacheAdminWebSocketEndPoint.class);
    private final Set<Session> sessions = ConcurrentHashMap.newKeySet();
    private  MessageSystemClient<String> messageClient;
    private Addressee addresseeDB;

    public CacheAdminWebSocketEndPoint(MessageSystemClient<String> messageClient, Addressee addresseeDB) {
        this.messageClient = messageClient;
        this.addresseeDB = addresseeDB;

        messageClient.addMessageReceiveListener(
                (m)->{
                    String msg = new Gson().toJson(m.getData());
                    for (Session session : sessions) {
                        log.debug("Send to session {} {}", session, m);
                        session.getAsyncRemote().sendText(msg);
                    }
                    return false;
                }
        );

    }

    @OnOpen
    public void onOpen(Session session, EndpointConfig config){
        log.info("Open session {} with user properties {}", session, config.getUserProperties());
        sessions.add(session);
    }

    @OnClose
    public void onClose(Session session, CloseReason reason){
        log.info("Session {} closed due to {}", session, reason);
        sessions.remove(session);
    }

    @OnMessage
    public void onMessage(String message, Session session){
        log.debug("Session {} received message: {} ", session, message);
        try {
            CachePropsDataSet dataSet = new Gson().fromJson(message, CachePropsDataSet.class);
            messageClient.sendMessage(addresseeDB, dataSet);
        } catch (JsonSyntaxException e) {
            log.error("Session {} parsing message {} error :", session, message, e);
        }
    }

    @OnError
    public void onError(Throwable e){
        log.error("on error", e);
    }
}
