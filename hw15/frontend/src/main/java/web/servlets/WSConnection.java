package web.servlets;

import org.apache.catalina.websocket.MessageInbound;
import org.apache.catalina.websocket.WsOutbound;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;

public class WSConnection extends MessageInbound {
    private final static Logger log = LogManager.getLogger(WSConnection.class);
    private WsOutbound outbound;

    @Override
    protected void onOpen(WsOutbound outbound) {
        super.onOpen(outbound);
        this.outbound = outbound;
    }

    @Override
    protected void onBinaryMessage(ByteBuffer byteBuffer) throws IOException {

    }

    @Override
    protected void onTextMessage(CharBuffer charBuffer) throws IOException {
        log.info("onTextMessage %", charBuffer);
        System.out.println("onTextMessage:" + charBuffer);
    }

    public void send(String data){
        try {
            outbound.writeTextMessage(CharBuffer.wrap(data));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
