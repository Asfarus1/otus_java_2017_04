package ru.otus_matveev_anton;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.otus_matveev_anton.genaral.Addressee;
import ru.otus_matveev_anton.genaral.AddresseeImpl;
import ru.otus_matveev_anton.genaral.MessageSystemClient;
import ru.otus_matveev_anton.genaral.SpecialAddress;
import ru.otus_matveev_anton.message_system_client.JsonSocketClient;

import javax.websocket.server.ServerEndpointConfig;
import java.io.IOException;

public class FrontendServerEndpointConfigurator extends ServerEndpointConfig.Configurator {
    private final MessageSystemClient<String> msClient;
    private final static Logger log = LogManager.getLogger(ServerEndpointConfig.Configurator.class);
    private final Addressee addresseeDB;

    public FrontendServerEndpointConfigurator() {
        super();
        msClient = JsonSocketClient.newInstance();
        try {
            msClient.init();
        } catch (IOException e) {
            log.error(e);
            throw new RuntimeException(e);
        }
        addresseeDB = new AddresseeImpl(SpecialAddress.ANYONE, "DBService");
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException {
        if (CacheAdminWebSocketEndPoint.class.equals(endpointClass)){
            return (T) new CacheAdminWebSocketEndPoint(msClient, addresseeDB);
        }
        return super.getEndpointInstance(endpointClass);
    }
}
