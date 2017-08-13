package ru.otus_matveev_anton.web.servlets;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;
import ru.otus_matveev_anton.MainMyOrmWithCache;

import javax.management.*;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

public class AdminServlet extends HttpServlet {
    private ObjectName cacheName;
    private static final String CACHE_MBEAN_NAME = "ru.otus_matveev_anton.my_cache:type=my_cache_users";
    private final Map<String, Function<Object, Attribute>> attrMaker = new HashMap<>();

    private enum Action {
        GET_READONLY,
        GET_ALL,
        SAVE;

        static Action getActionFromRequest(String action) {
            if (action != null) {
                switch (action) {
                    case "save":
                        return Action.SAVE;
                    case "getreadonly":
                        return Action.GET_READONLY;
                }
            }
            return Action.GET_ALL;
        }
    }

    @Override
    public void init() throws ServletException {
        super.init();


        WebApplicationContext ctx = ContextLoader.getCurrentWebApplicationContext();
        MainMyOrmWithCache testActions =ctx.getBean(MainMyOrmWithCache.class);
        Thread thread = new Thread(()-> {
            try {
                testActions.runTest();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        thread.start();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new ServletException(e);
        }

        try {
            cacheName = new ObjectName(CACHE_MBEAN_NAME);
        } catch (MalformedObjectNameException e) {
            throw new ServletException(e);
        }
        MBeanAttributeInfo[] attributes = getCacheAttributes();
        String type;
        Function<Object, Attribute> func;
        for (MBeanAttributeInfo attribute : attributes) {
            final String attrName = attribute.getName();
            type = attribute.getType();

            if ("int".equalsIgnoreCase(type)) {
                func = v -> new Attribute(attrName, Integer.valueOf((String) v));
            } else if ("long".equalsIgnoreCase(type)) {
                func = v -> new Attribute(attrName, Integer.valueOf((String) v));
            } else {
                func = v -> new Attribute(attrName, v);
            }
            attrMaker.put(attrName, func);
        }
    }

    private MBeanAttributeInfo[] getCacheAttributes() throws ServletException {
        try {
            return ManagementFactory.getPlatformMBeanServer().getMBeanInfo(cacheName).getAttributes();
        } catch (InstanceNotFoundException|IntrospectionException|ReflectionException e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        Action action = Action.getActionFromRequest(req.getParameter("action"));
        switch (action){
            case SAVE: setCacheParams(req, resp);
                break;
            default: addCacheJSON(resp, Action.GET_READONLY.equals(action));
        }

    }

    @SuppressWarnings("unchecked")
    private void addCacheJSON( HttpServletResponse resp, boolean readOnlyFields) throws IOException, ServletException {
        Stream<MBeanAttributeInfo> s = Stream.of(getCacheAttributes());
        JSONObject object = new JSONObject();
        if (readOnlyFields) {
            s = s.filter(a -> !a.isWritable());
        }
        s.map(MBeanFeatureInfo::getName).forEach(n -> object.put(n, getAttr(n)));

        object.writeJSONString(resp.getWriter());
        resp.setContentType("application/json;charset=utf-8");
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    @SuppressWarnings("unchecked")
    private void setCacheParams(HttpServletRequest req,  HttpServletResponse resp) throws ServletException{
        JSONParser parser = new JSONParser();
        try {
            JSONObject object = (JSONObject) parser.parse(req.getReader());
            object.forEach((k,v) -> setCacheAttribute((String) k, v));
            resp.setStatus(HttpServletResponse.SC_OK);
        } catch (IOException|ParseException|IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            throw new ServletException(e);
        }
    }

    private Object getAttr(String attrName){
        try {
            return ManagementFactory.getPlatformMBeanServer().getAttribute(cacheName, attrName);
        } catch (MBeanException|AttributeNotFoundException|InstanceNotFoundException|ReflectionException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private void setCacheAttribute(String name, Object value){
        try {
            Function<Object, Attribute> func = attrMaker.get(name);
            if (func == null){
                throw new IllegalArgumentException("unknown attribute " + name);
            }
            ManagementFactory.getPlatformMBeanServer().setAttribute(cacheName, func.apply(value));
        } catch (MBeanException|AttributeNotFoundException|InstanceNotFoundException|ReflectionException|InvalidAttributeValueException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }
}
