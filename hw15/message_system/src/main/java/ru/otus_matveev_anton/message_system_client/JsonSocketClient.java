package ru.otus_matveev_anton.message_system_client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.otus_matveev_anton.genaral.*;
import ru.otus_matveev_anton.json_message_system.JsonMessage;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class JsonSocketClient extends MessageSystemClient<String> {

    private final static Logger log = LogManager.getLogger(JsonSocketClient.class);
    private static final int WORKERS_COUNT = 3;
    private static final String DEFAULT_PROP_FILE_PATH = "/message_system_client.properties";
    private static final int DEFAULT_OPER_DELAY_MS = 10;
    private static final int SO_TIMEOUT = 120_000;
    private static final int CONNECT_TIMEOUT = 2000;
    private static final int DEFAULT_CONNECT_TRY_COUNT = 5;

    private final Queue<String> out;
    private Socket socket;
    private final ExecutorService executor;
    private final String host;
    private final int port;
    private final int maxConnectTryCount;

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

    private JsonSocketClient(Properties props) {

        host = props.getProperty("host");
        Integer port = null;
        try{
            port = Integer.valueOf(props.getProperty("port"));
        }catch (NumberFormatException e){
            log.error("port parse error", e);
        }

        String groupName = props.getProperty("groupName");
        String clientId = props.getProperty("clientId");

        if (host == null || port == null || groupName == null){
            String msg = "Config files must contain properties: [host, port, groupName] and may contain [clientId, queueCapacity]";
            log.error(msg);
            throw new IllegalArgumentException(msg);
        }
        this.port = port;

        setAddressee(clientId == null
                ? new AddresseeImpl(SpecialAddress.GENERATE_NEW, groupName)
                : new AddresseeImpl(clientId, groupName));

        out = props.containsKey("queueCapacity") ? new LinkedBlockingQueue<>(Integer.valueOf(props.getProperty("queueCapacity"))) : new LinkedBlockingQueue<>();

        maxConnectTryCount = props.containsKey("maxConnectTryCount") ? Integer.valueOf(props.getProperty("maxConnectTryCount")) : DEFAULT_CONNECT_TRY_COUNT;

        executor = Executors.newFixedThreadPool(WORKERS_COUNT);
    }

    public void init(){
        executor.submit(this::connect);
    }

    @SuppressWarnings("InfiniteLoopStatement")
    private void connect() {
        int connectTryCount = 0;
        while (true) {
            if (socket == null || socket.isClosed()) {
                try {
                    socket = new Socket();
                    socket.connect(new InetSocketAddress(host, port), CONNECT_TIMEOUT);
                    socket.setSoTimeout(SO_TIMEOUT);
                    executor.submit(this::register);
                    connectTryCount = 0;
                } catch (IOException | RuntimeException e) {
                    log.error("Connection failure", e);
                    if (++connectTryCount == maxConnectTryCount) {
                        log.error("Number of connection attempts exceeded");
                        close();
                    }else {
                        closeSocket();
                        return;
                    }
                }
            }
            sleep();
        }
    }

    private void sleep() {
        try {
            Thread.sleep(DEFAULT_OPER_DELAY_MS);
        } catch (InterruptedException e) {
            log.error(e);
        }
    }

    private void sendingMessages(){
        try (OutputStream os = socket.getOutputStream()) {
            while (!socket.isClosed()) {
                if (!out.isEmpty()) {
                    String json = out.peek();
                    os.write(json.getBytes());
                    os.write(JsonMessage.MESSAGE_SEPARATOR);
                    os.flush();
                    out.poll();
                    log.debug("send msg:{}", json);
                }else {
                    sleep();
                }
            }
        } catch (IOException e) {
            log.error(e);
        }finally {
            closeSocket();
        }
    }

    private void register(){
        sendMessage(getAddressee(), null);
        try {
            OutputStream os = socket.getOutputStream();
            String msg = new JsonMessage(getAddressee(), getAddressee(), null).toPackedData();
            os.write(msg.getBytes());
            os.write(JsonMessage.MESSAGE_SEPARATOR);
            os.flush();

            log.debug("send authorization {}" + msg);

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
            executor.submit(this::receivingMessages);
            executor.submit(this::sendingMessages);
        } catch (IOException e) {
            log.error(e);
            closeSocket();
        }
    }

    private void receivingMessages(){
        String json;
        try (BufferedReader is = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            while (!socket.isClosed()) {
                json = readTextFromStream(is);
                if ("".equals(json)){
                    log.info("got ping");
                    continue;
                }

                log.debug("got msg:{}", json);
                JsonMessage message =  new JsonMessage();
                try {
                    message.loadFromPackagedData(json);
                    log.debug("unpack msg:{}", json);
                } catch (MessageFormatException e) {
                    log.error("Unpacking message error:", e);
                    continue;
                }
                if (message.getData() instanceof Throwable){
                    throw new IOException("Server error", (Throwable) message.getData());
                }
               onMessageReceive(message);
            }
        } catch (IOException e) {
            log.error(e);
        }finally {
            closeSocket();
        }
    }

    @Override
    public void sendMessage(Addressee to, Object data) {
        String msg = new JsonMessage(getAddressee(), to, data).toPackedData();
        log.debug("add message {}", msg);
        out.offer(msg);
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
        boolean fl = false;
        while (!socket.isClosed() && ((inputLine = br.readLine()) != null)) {
            stringBuilder.append(inputLine);
            if (inputLine.isEmpty() && fl) {
                    return stringBuilder.toString();
            }else {
                fl = true;
            }
        }
        throw new IOException("socked input closed");
    }

    public void close(){
        closeSocket();
        super.close();
        executor.shutdown();
        log.info("message client closed");
    }

    private void closeSocket() {
        if (socket != null && !socket.isClosed()) {
            try {
                socket.close();
                log.info("Socked closed");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
