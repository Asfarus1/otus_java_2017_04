package ru.otus_matveev_anton;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import ru.otus_matveev_anton.servlets.AdminServlet;

public class MainServlets {
    private final static int PORT = 8080;
    private final static String PUBLIC_HTML = "public_html";


    public static void main(String[] args) throws Exception {

        Thread cacheMain = new Thread(() -> {
            try {
                new MainMyOrmWithCache().runTest();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        cacheMain.setDaemon(true);
        cacheMain.start();
        Thread.sleep(5000);

        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setResourceBase(PUBLIC_HTML);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.addServlet(new ServletHolder(new AdminServlet()),"/cacheData");

        Server server = new Server(PORT);
        server.setHandler(new HandlerList(resourceHandler, context));
        server.start();
        server.join();
    }
}
