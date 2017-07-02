package ru.otus_matveev_anton.servlets;

import org.json.simple.JSONObject;

import javax.management.*;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.management.ManagementFactory;

public class AdminServlet extends HttpServlet {
    private MBeanServer beanServer;
    private ObjectName cacheName;
    private static final String CACHE_MBEAN_NAME = "ru.otus_matveev_anton.db.my_cache:type=my_cache_users";

    @Override
    public void init() throws ServletException {
        super.init();
        beanServer = ManagementFactory.getPlatformMBeanServer();
        try {
            cacheName = new ObjectName(CACHE_MBEAN_NAME);
        } catch (MalformedObjectNameException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        JSONObject object = new JSONObject();
        object.put("HitCount", getCacheAttribute("HitCount"));
        object.put("IdleTimeS", getCacheAttribute("IdleTimeS"));
        object.put("LifeTimeS", getCacheAttribute("LifeTimeS"));
        object.put("MaxElements", getCacheAttribute("MaxElements"));
        object.put("MissCount", getCacheAttribute("MissCount"));
        object.put("TimeThresholdS", getCacheAttribute("TimeThresholdS"));
        object.put("Size", getCacheAttribute("Size"));
        object.put("Eternal", getCacheAttribute("Eternal"));

        resp.getWriter().write(object.toJSONString());

        resp.setContentType("application/json;charset=utf-8");
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    private Object getCacheAttribute(String name){
        try {
            return beanServer.getAttribute(cacheName, name);
        } catch (MBeanException|AttributeNotFoundException|InstanceNotFoundException|ReflectionException e) {
            e.printStackTrace();
        }
        return null;
    }
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }
}
