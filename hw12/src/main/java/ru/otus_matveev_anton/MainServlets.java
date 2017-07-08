package ru.otus_matveev_anton;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import ru.otus_matveev_anton.web.filters.AuthorizationFilter;
import ru.otus_matveev_anton.web.servlets.AdminServlet;
import ru.otus_matveev_anton.web.servlets.LoginServlet;

import javax.servlet.DispatcherType;
import java.util.EnumSet;

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
        Thread.sleep(1000);


//        не работает если добавлять фильтры
//        ResourceHandler resourceHandler = new ResourceHandler();
//        resourceHandler.setResourceBase(PUBLIC_HTML);

        ServletHolder resourceHolder = new ServletHolder("default", DefaultServlet.class);
        resourceHolder.setInitParameter("resourceBase",PUBLIC_HTML);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.addFilter(new FilterHolder(new AuthorizationFilter()), "/*", EnumSet.of(DispatcherType.REQUEST
                , DispatcherType.ASYNC, DispatcherType.FORWARD, DispatcherType.INCLUDE, DispatcherType.ERROR));
        context.addServlet(resourceHolder , "/");
        context.addServlet(new ServletHolder(new AdminServlet()), "/cacheData");
        context.addServlet(new ServletHolder(new LoginServlet()), "/authorization");

        Server server = new Server(PORT);
        server.setHandler(new HandlerList(context));

        server.start();
        server.join();
    }
}

