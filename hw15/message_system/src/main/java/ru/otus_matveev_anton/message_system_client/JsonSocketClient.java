package ru.otus_matveev_anton.message_system_client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.otus_matveev_anton.Message;
import ru.otus_matveev_anton.MessageFormatException;
import ru.otus_matveev_anton.MessageSystem;
import ru.otus_matveev_anton.message_system.JsonMessage;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class JsonSocketClient extends MessageSystemClient<String> {

    private final static Logger log = LogManager.getLogger(JsonSocketClient.class);
    private final static String DEFAULT_CLIENT_ID_FILE_NAME = "/message_system_client.properties";
    private static final int WORKERS_COUNT = 2;

    private String clientId;
    private final Queue<String> out;
    private final Socket socket;
    private final ExecutorService executor;

    public JsonSocketClient(String... configFiles) throws ConnectException {
        Properties props = new Properties();
        String host;
        int port;

        try {
            try (InputStream is = new FileInputStream(DEFAULT_CLIENT_ID_FILE_NAME)) {
                props.load(is);
            }
            for (String file : configFiles) {
                try (InputStream is = new FileInputStream(file)) {
                    props.load(is);
                }
            }

            if (log.isInfoEnabled()) {
                StringWriter writer = new StringWriter();
                PrintWriter pw = new PrintWriter(writer);
                props.forEach((k, v) -> pw.printf("%s=%s,", k, v));
                log.info("properties from %1 : %2", Arrays.toString(configFiles), writer.getBuffer().toString());
            }

            host = props.getProperty("host");
            port = Integer.valueOf(props.getProperty("port"));
            clientId = props.getProperty("clientId");

            if (props.containsKey("queueCapacity")) {
                out = new LinkedBlockingQueue<>(Integer.valueOf(props.getProperty("queueCapacity")));
            } else {
                out = new LinkedBlockingQueue<>();
            }

        } catch (IOException | NumberFormatException e) {
            log.error("Failure to setting from config files", e);
                throw new IllegalArgumentException("Failure to load settings from config files. Files must contain properties: [host, port] and may contain [clientId, queueCapacity]", e);
        }

        try {
            socket = new Socket(host, port);
            if (clientId == null || clientId.isEmpty()) {
                register();
                saveClientId();
            } else {
                signIn(clientId);
            }
        } catch (IOException | RuntimeException e) {
            log.error("Connection failure", e);
            close();
            throw new ConnectException("Connection failure", e);
        }

        executor = Executors.newFixedThreadPool(WORKERS_COUNT);
    }

    public void init(){
        executor.submit(this::receivingMessages);
        executor.submit(this::sendingMessages);
    }

    private void receivingMessages(){
        String json = null;
        try (BufferedReader is = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            while (socket.isConnected()) {
                json = readTextFromStream(is);
                JsonMessage message =  new JsonMessage();
                try {
                    message.loadFromPackagedData(json);
                } catch (MessageFormatException e) {
                    log.error("Unpacking message error:" + json, e);
                    message = null;
                }
                for (MessageReceiveListener listener : listeners) {
                    if (listener.onMessageReceive(message)){
                        break;
                    }
                }
            }
        } catch (IOException e) {
            log.error(e);
        }finally {
            close();
        }
    }

    private void sendingMessages(){
        try (PrintWriter os = new PrintWriter(socket.getOutputStream(), true)) {
            while (socket.isConnected()) {
                String json = out.peek();
                os.println(json);
                os.println();
            }
        } catch (IOException e) {
            log.error(e);
        }finally {
            close();
        }
    }

    @Override
    public boolean sendMessage(Message<String> message) {
       return out.offer(message.toPackagedData());
    }

    @Override
    protected void register() throws IOException {
        PrintStream outputStream;
        outputStream = new PrintStream(socket.getOutputStream());
        outputStream.println(MessageSystem.MESSAGE_GENERATE_NEW_ID);
        outputStream.println();

        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        clientId = readTextFromStream(in);
        if (clientId.isEmpty()){
            throw new IOException("Register failure, clientId from message server response is empty");
        }
    }

    private void saveClientId() throws IOException {
        Properties props = new Properties();
        try (InputStream is = new FileInputStream(DEFAULT_CLIENT_ID_FILE_NAME)) {
            props.load(is);
        }
        props.put("clientId", clientId);
        try (OutputStream os = new FileOutputStream(DEFAULT_CLIENT_ID_FILE_NAME)) {
            props.store(os, null);
        }
    }

    @Override
    protected void signIn(String clientId) throws IOException {
        PrintStream outputStream;
        outputStream = new PrintStream(socket.getOutputStream());
        outputStream.println(clientId);
        outputStream.println();

        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        String resp = readTextFromStream(in);
        if (!MessageSystem.MESSAGE_SIGN_IN_SUCCESS.equals(resp)){
            throw new IOException("Authorization failed with message server response: " + resp);
        }
    }

    private String readTextFromStream(BufferedReader br) throws IOException {
        String inputLine;
        StringBuilder stringBuilder = new StringBuilder();

        while ((inputLine = br.readLine()) != null) {
            stringBuilder.append(inputLine);
            if (inputLine.isEmpty() && !stringBuilder.toString().isEmpty()) {
                return stringBuilder.toString();
            }
        }
        throw new IOException("socked input closed");
    }

    public void close(){
        shutdownRegistrations.forEach(Runnable::run);
        shutdownRegistrations.clear();
        if (socket != null && socket.isConnected()) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        executor.shutdown();
    }
}
