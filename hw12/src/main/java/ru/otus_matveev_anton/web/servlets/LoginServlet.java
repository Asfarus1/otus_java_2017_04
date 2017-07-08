package ru.otus_matveev_anton.web.servlets;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class LoginServlet extends HttpServlet {

    private static final String LOGIN_PARAMETER_NAME = "user";
    private static final String PASSWORD_PARAMETER_NAME = "login";

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String user = request.getParameter(LOGIN_PARAMETER_NAME);
        HttpSession session = request.getSession();
        session.setAttribute(LOGIN_PARAMETER_NAME, user);
        session.setMaxInactiveInterval(5);
        response.sendRedirect("/cacheadmin.html");
    }
}