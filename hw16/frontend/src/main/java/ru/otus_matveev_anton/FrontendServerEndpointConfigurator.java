package ru.otus_matveev_anton;

import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.otus_matveev_anton.genaral.Addressee;
import ru.otus_matveev_anton.genaral.AddresseeImpl;
import ru.otus_matveev_anton.genaral.MessageSystemClient;
import ru.otus_matveev_anton.genaral.SpecialAddress;
import ru.otus_matveev_anton.json_message_system.JsonSocketClient;
import ru.otus_matveev_anton.messages.CacheFullDataSet;
import ru.otus_matveev_anton.messages.CacheGetCurrentProps;
import ru.otus_matveev_anton.messages.CacheStatsDataSet;

import javax.websocket.server.ServerEndpointConfig;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class FrontendServerEndpointConfigurator extends ServerEndpointConfig.Configurator {
    private final MessageSystemClient<String> msClient;
    private final static Logger log = LogManager.getLogger(ServerEndpointConfig.Configurator.class);
    private final Addressee addresseeDB = new AddresseeImpl(SpecialAddress.ANYONE, "DBService");
    private final Set<CacheAdminWebSocketEndPoint> endPoints = ConcurrentHashMap.newKeySet();
    private volatile String curCacheFullStats;

    public FrontendServerEndpointConfigurator() {
        super();
        log.debug("constructor");
        msClient = JsonSocketClient.newInstance();
        msClient.init();
        msClient.addMessageReceiveListener(
                (m) -> {
                    Object data = m.getData();
                    String msg = new Gson().toJson(data);

                    if (data instanceof CacheFullDataSet) {
                        curCacheFullStats = msg;
                    } else if (!(data instanceof CacheStatsDataSet)) {
                        return false;
                    }

                    for (CacheAdminWebSocketEndPoint point : endPoints) {
                        point.sendToSessions(msg);
                    }
                    return false;
                }
        );

        final CacheGetCurrentProps msg = new CacheGetCurrentProps();
        new Thread(()->{
            while (curCacheFullStats == null){
                sendMessage(msg);
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    log.error("thread for sending get CacheGetCurrentProps", e);
                }
            }
        }).start();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException {
        log.debug("getEndpointInstance {}", endpointClass);
        if (CacheAdminWebSocketEndPoint.class.equals(endpointClass)){
            msClient.sendMessage(addresseeDB, new CacheGetCurrentProps());
            CacheAdminWebSocketEndPoint point = new CacheAdminWebSocketEndPoint(this);
            endPoints.add(point);
            return (T) point;
        }
        return super.getEndpointInstance(endpointClass);
    }

    void sendMessage(Object data) {
        msClient.sendMessage(addresseeDB, data);
    }

    String getCurCacheFullStats() {
        return curCacheFullStats;
    }
}
