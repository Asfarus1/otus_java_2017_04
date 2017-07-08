package ru.otus_matveev_anton.web.servlets;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

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

public class AdminServlet extends HttpServlet {
    private MBeanServer beanServer;
    private ObjectName cacheName;
    private static final String CACHE_MBEAN_NAME = "ru.otus_matveev_anton.my_cache:type=my_cache_users";
    private final Map<String, Function<Object, Attribute>> attrMaker = new HashMap<>();

    private enum Action {
        GET_READONLY,
        GET_ALL,
        SAVE;

        static Action getActionFromRequest(HttpServletRequest req) {
            String action = req.getParameter("action");
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
        beanServer = ManagementFactory.getPlatformMBeanServer();
        try {
            cacheName = new ObjectName(CACHE_MBEAN_NAME);
            MBeanAttributeInfo[] attributes = beanServer.getMBeanInfo(cacheName).getAttributes();
            String type;
            Function<Object, Attribute> func;
            for (MBeanAttributeInfo attribute : attributes) {
                final String attrName = attribute.getName();
                type = attribute.getType();

                if ("int".equalsIgnoreCase(type)) {
                    func = v -> new Attribute(attrName, Integer.valueOf((String) v));
                } else if ("long".equalsIgnoreCase(type)) {
                    func = v -> new Attribute(attrName, Integer.valueOf((String)v));
                } else {
                    func = v -> new Attribute(attrName, v);
                }
                attrMaker.put(attrName, func);
            }
        } catch (MalformedObjectNameException|ReflectionException|IntrospectionException|InstanceNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        Action action = Action.getActionFromRequest(req);
        switch (action){
            case SAVE: setCacheParams(req, resp);
                break;
            default: addCacheJSON(resp, Action.GET_READONLY.equals(action));
        }

    }

    @SuppressWarnings("unchecked")
    private void addCacheJSON( HttpServletResponse resp, boolean readOnlyFields) throws IOException {
        JSONObject object = new JSONObject();

        object.put("HitCount", getCacheAttribute("HitCount"));
        object.put("MissCount", getCacheAttribute("MissCount"));
        object.put("Size", getCacheAttribute("Size"));
        if (!readOnlyFields){
            object.put("IdleTimeS", getCacheAttribute("IdleTimeS"));
            object.put("LifeTimeS", getCacheAttribute("LifeTimeS"));
            object.put("MaxElements", getCacheAttribute("MaxElements"));
            object.put("TimeThresholdS", getCacheAttribute("TimeThresholdS"));
            object.put("Eternal", getCacheAttribute("Eternal"));
        }
        object.writeJSONString(resp.getWriter());
        resp.setContentType("application/json;charset=utf-8");
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    private void setCacheParams(HttpServletRequest req,  HttpServletResponse resp) throws IOException {
        JSONParser parser = new JSONParser();
        try {
            JSONObject object = (JSONObject) parser.parse(req.getReader());
            object.forEach((k,v) -> setCacheAttribute((String) k, v));
            resp.setStatus(HttpServletResponse.SC_OK);
        } catch (ParseException|IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            e.printStackTrace();
        }
    }

    private Object getCacheAttribute(String name){
        try {
            return beanServer.getAttribute(cacheName, name);
        } catch (MBeanException|AttributeNotFoundException|InstanceNotFoundException|ReflectionException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void setCacheAttribute(String name, Object value){
        try {
            Function<Object, Attribute> func = attrMaker.get(name);
            if (func == null){
                throw new IllegalArgumentException("unknown attribute " + name);
            }
            beanServer.setAttribute(cacheName, func.apply(value));
        } catch (MBeanException|AttributeNotFoundException|InstanceNotFoundException|ReflectionException|InvalidAttributeValueException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }
}
