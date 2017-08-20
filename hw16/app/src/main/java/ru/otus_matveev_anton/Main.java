package ru.otus_matveev_anton;

import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    private final static int frontendsCount = 2;

    private final static String cRunMessageSystem = "java -jar message_system/target/message_system.jar";
    private final static String cRunDBService = "java -jar DBService/target/DBService.jar";
//    private final static String cCopyFrontend = "copy frontend\\target\\frontend.war %%CATALINA_HOME%%\\webapps\\frontend%d.war";
//    private final static String cCopyFrontend = "cp frontend/target/frontend.war ~/apps/jetty-distribution-9.4.6.v20170531/webapps/frontend%d.war";

    public static void main(String[] args) throws IOException {
        new Main().start();
    }

    private void start() throws IOException {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        executor.execute(()->startProcess(cRunMessageSystem, true, "Message System"));
        executor.execute(()->startProcess(cRunDBService, true, "Emulator DBService"));
//        for (int i = 0; i < frontendsCount; i++) {
//            executor.execute(()->startProcess(String.format(cCopyFrontend, i), false, null));
//        }
    }

    private void startProcess(String command, boolean isRedirectOut, String name) {
        try {
        System.out.println("run command:" + command);
            ProcessBuilder pb = new ProcessBuilder(command.split(" "));
            Process process = pb.start();

        if (isRedirectOut) {
            StreamListener out = new StreamListener(process.getInputStream(),name);
            StreamListener error = new StreamListener(process.getErrorStream(), name);
            out.start();
            error.start();
        }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private class StreamListener extends Thread {

        private final InputStream is;
        private final String name;

        private StreamListener(InputStream is, String name) {
            this.is = is;
            this.name = name;
        }

        @Override
        public void run() {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                String line;
                while ((line = br.readLine()) != null) {
                    System.out.println(name + "=>" + line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
