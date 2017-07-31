package web.servlets;

import org.apache.catalina.websocket.MessageInbound;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;

public class WSConnection extends MessageInbound {
    @Override
    protected void onBinaryMessage(ByteBuffer byteBuffer) throws IOException {

    }

    @Override
    protected void onTextMessage(CharBuffer charBuffer) throws IOException {

    }
}
