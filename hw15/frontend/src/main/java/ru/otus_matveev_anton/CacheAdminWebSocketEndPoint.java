package ru.otus_matveev_anton;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.otus_matveev_anton.genaral.Addressee;
import ru.otus_matveev_anton.genaral.MessageSystemClient;
import ru.otus_matveev_anton.messages.CacheGetCurrentProps;
import ru.otus_matveev_anton.messages.CachePropsDataSet;

import javax.servlet.http.HttpServlet;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint(value = "/cacheAdmin", configurator = FrontendServerEndpointConfigurator.class)
public class CacheAdminWebSocketEndPoint extends HttpServlet{
    private final static Logger log = LogManager.getLogger(CacheAdminWebSocketEndPoint.class);
    private final Set<Session> sessions = ConcurrentHashMap.newKeySet();
    private  MessageSystemClient<String> msClient;
    private Addressee addresseeDB;
    private volatile boolean hasCacheProps = false;

    public CacheAdminWebSocketEndPoint(MessageSystemClient<String> msClient, Addressee addresseeDB) {
        this.msClient = msClient;
        this.addresseeDB = addresseeDB;

        msClient.addMessageReceiveListener(
                (m)->{
                    Object data = m.getData();
                    if (data instanceof CachePropsDataSet){
                        hasCacheProps = true;
                    }else if (!hasCacheProps){
                        msClient.sendMessage(addresseeDB, new CacheGetCurrentProps());
                    }
                    String msg = new Gson().toJson(data);
                    for (Session session : sessions) {
                        log.debug("Send to session {} {}", session, msg);
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
            msClient.sendMessage(addresseeDB, dataSet);
        } catch (JsonSyntaxException e) {
            log.error("Session {} parsing message {} error :", session, message, e);
        }
    }

    @OnError
    public void onError(Throwable e){
        log.error("on error", e);
    }
}
