package ru.otus_matveev_anton;

import org.eclipse.jetty.security.*;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.util.security.Credential;
import ru.otus_matveev_anton.web.servlets.AdminServlet;

public class MainServlets {
    private final static int PORT = 8080;
    private final static String PUBLIC_HTML = "public_html";
    private static final String USER_NAME = "admin";
    private static final String USER_PASS = "admin";
    private static final String REALM_NAME = "myrealm";


    public static void main(String[] args) throws Exception {
        new MainServlets().startServer();
    }

    private void startServer() throws Exception {
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

        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setResourceBase(PUBLIC_HTML);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.addServlet(new ServletHolder(new AdminServlet()), "/cacheData");
        context.setSecurityHandler(basicAuth(USER_NAME, USER_PASS, REALM_NAME));

        Server server = new Server(PORT);
        server.setHandler(new HandlerList(resourceHandler, context));
        server.start();
        server.join();
    }

    private SecurityHandler basicAuth(String username, String password, String realm) {

        HashLoginService loginService = new HashLoginService();
        UserStore us = new UserStore();
        us.addUser(username, Credential.getCredential(password), new String[] {"user"});
        loginService.setUserStore(us);
        loginService.setName(realm);

        Constraint constraint = new Constraint();
        constraint.setName(Constraint.__BASIC_AUTH);
        constraint.setRoles(new String[]{"user"});
        constraint.setAuthenticate(true);

        ConstraintMapping cm = new ConstraintMapping();
        cm.setConstraint(constraint);
        cm.setPathSpec("/*");

        ConstraintSecurityHandler csh = new ConstraintSecurityHandler();
        csh.setAuthenticator(new BasicAuthenticator());
        csh.setRealmName(realm);
        csh.addConstraintMapping(cm);
        csh.setLoginService(loginService);

        return csh;
    }
}

