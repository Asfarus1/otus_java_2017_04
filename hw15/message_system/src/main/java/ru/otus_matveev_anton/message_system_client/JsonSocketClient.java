package ru.otus_matveev_anton.message_system_client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.otus_matveev_anton.genaral.*;
import ru.otus_matveev_anton.json_message_system.JsonMessage;

import java.io.*;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class JsonSocketClient extends MessageSystemClient<String> {

    private final static Logger log = LogManager.getLogger(JsonSocketClient.class);
    private final static String DEFAULT_CLIENT_ID_FILE_NAME = "message_system_client.properties";
    private static final int WORKERS_COUNT = 2;
    private final String groupName;
    private final String configClientIdFileName;
    private final String clientId;

    private final Queue<String> out;
    private final Socket socket;
    private final ExecutorService executor;

    public JsonSocketClient(String configDir, String... configFiles) throws ConnectException {
        Properties props = new Properties();
        configClientIdFileName = configDir +  DEFAULT_CLIENT_ID_FILE_NAME;
        String host;
        Integer port;

        try {
            try (InputStream is = new FileInputStream(configClientIdFileName)) {
                props.load(is);
            }
            for (String file : configFiles) {
                try (InputStream is = new FileInputStream(file)) {
                    props.load(is);
                }
            }
            log.info("properties from {} : {}", Arrays.toString(configFiles), props);

            host = props.getProperty("host");
            port = Integer.valueOf(props.getProperty("port"));
            groupName = props.getProperty("groupName");
            clientId = props.getProperty("clientId");

            if (props.containsKey("queueCapacity")) {
                out = new LinkedBlockingQueue<>(Integer.valueOf(props.getProperty("queueCapacity")));
            } else {
                out = new LinkedBlockingQueue<>();
            }

        } catch (IOException | NumberFormatException e) {
            String msg = "Failure to setting from config files";
            log.error(msg, e);
            throw new IllegalArgumentException(msg, e);
        }

        if (host == null || port == null || groupName == null){
            String msg = "Config files must contain properties: [host, port, groupName] and may contain [clientId, queueCapacity]";
            log.error(msg);
            throw new IllegalArgumentException(msg);
        }

        setAddressee(clientId == null
                    ? new AddresseeImpl(SpecialAddress.GENERATE_NEW, groupName)
                    : new AddresseeImpl(clientId, groupName));

        try {
            socket = new Socket(host, port);
        } catch (IOException | RuntimeException e) {
            log.error("Connection failure", e);
            close();
            throw new ConnectException("Connection failure", e);
        }

        executor = Executors.newFixedThreadPool(WORKERS_COUNT);
    }

    public void init() throws IOException {
        executor.submit(this::sendingMessages);
        try {
            if (clientId == null || clientId.isEmpty()) {
                register(null, groupName);
                saveClientId();
            } else {
                register(clientId, groupName);
            }
        } catch (IOException | RuntimeException e) {
            log.error("Registration failure", e);
            close();
            throw new ConnectException("Registration failure", e);
        }
        executor.submit(this::receivingMessages);
    }

    private void receivingMessages(){
        String json;
        try (BufferedReader is = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            while (socket.isConnected()) {
                json = readTextFromStream(is);
                log.debug("got msg:{}", json);
                JsonMessage message =  new JsonMessage();
                try {
                    message.loadFromPackagedData(json);
                } catch (MessageFormatException e) {
                    log.error("Unpacking message error:" + json, e);
                    message = null;
                }
               onMessageReceive(message);
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
                if (!out.isEmpty()) {
                    String json = out.poll();
                    os.println(json);
                    os.println();
                    log.debug("send msg:{}", json);
                }
            }
        } catch (IOException e) {
            log.error(e);
        }finally {
            close();
        }
    }

    @Override
    public boolean sendMessage(Addressee to, Object data) {
        return out.offer(new JsonMessage(getAddressee(), to, data).toPackedData());
    }

    @Override
    protected void register(String clientId, String groupName) throws IOException {
        sendMessage(getAddressee(), null);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String json = readTextFromStream(in);
        Message<String> message = new JsonMessage();
        message.loadFromPackagedData(json);
        Object data = message.getData();
        if (data instanceof Throwable){
            throw new IOException("Server error",(Throwable)data);
        }else {
            setAddressee(message.getTo());
            log.info("client registered {}", getAddressee());
        }
    }

    private void saveClientId() throws IOException {
        Properties props = new Properties();
        try (InputStream is = new FileInputStream(configClientIdFileName)) {
            props.load(is);
        }
        Address address = getAddressee().getAddress();
        props.put("clientId", address.toString());
        try (OutputStream os = new FileOutputStream(configClientIdFileName)) {
            props.store(os, null);
        }
        log.info("clientId {} save to file {}", address, configClientIdFileName);
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
        super.close();
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
