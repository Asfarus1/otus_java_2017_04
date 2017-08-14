package ru.otus_matveev_anton.message_system_client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.otus_matveev_anton.genaral.*;
import ru.otus_matveev_anton.json_message_system.JsonMessage;

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
    private static final int WORKERS_COUNT = 2;
    private static final String DEFAULT_PROP_FILE_PATH = "/message_system_client.properties";

    private final Queue<String> out;
    private final Socket socket;
    private final ExecutorService executor;

    public static JsonSocketClient newInstance(){
        return fromConfigFiles(DEFAULT_PROP_FILE_PATH);
    }
    public static JsonSocketClient fromConfigFiles(String... configFiles) throws ConnectException {
        if (configFiles.length == 0){
            throw new IllegalArgumentException("no config files");
        }

        Properties props = new Properties();

        try {
            Class aClass = JsonSocketClient.class;
            for (String file : configFiles) {
                try (InputStream is = aClass.getResourceAsStream(file)) {
                    props.load(is);
                }
            }
            log.info("properties from {} : {}", Arrays.toString(configFiles), props);
        } catch (IOException e) {
            String msg = "Failure to setting from config files";
            log.error(msg, e);
            throw new IllegalArgumentException(msg, e);
        }
        return new JsonSocketClient(props);
    }

    public JsonSocketClient(Properties props) {

        String host = props.getProperty("host");
        Integer port = null;
        try{
            port = Integer.valueOf(props.getProperty("port"));
        }catch (NumberFormatException e){
            log.error("port parse error", e);
        }

        String groupName = props.getProperty("groupName");
        String clientId = props.getProperty("clientId");

        if (props.containsKey("queueCapacity")) {
            out = new LinkedBlockingQueue<>(Integer.valueOf(props.getProperty("queueCapacity")));
        } else {
            out = new LinkedBlockingQueue<>();
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
        register();
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

    private void register() throws IOException {
        sendMessage(getAddressee(), null);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String json = readTextFromStream(in);
        Message<String> message = new JsonMessage();
        message.loadFromPackagedData(json);
        Object data = message.getData();
        if (data instanceof Throwable){
            Throwable e = (Throwable) data;
            log.error("Registration failure", e);
            throw new IOException("Registration failure", e);
        }else {
            setAddressee(message.getTo());
            log.info("client registered {}", getAddressee());
        }
    }

    private void saveClientId(String fileName) throws IOException {
        Properties props = new Properties();
        try (InputStream is = new FileInputStream(fileName)) {
            props.load(is);
        }
        Address address = getAddressee().getAddress();
        props.put("clientId", address.toString());
        try (OutputStream os = new FileOutputStream(fileName)) {
            props.store(os, null);
        }
        log.info("clientId {} save to file {}", address, fileName);
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
