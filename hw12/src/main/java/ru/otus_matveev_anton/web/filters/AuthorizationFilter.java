package ru.otus_matveev_anton.web.filters;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class AuthorizationFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpReq = (HttpServletRequest)request;
        if (!httpReq.getRequestURI().contains("/authorization")) {
            HttpSession session = httpReq.getSession();
            if (session == null || session.getAttribute("user") == null) {
                ((HttpServletResponse)response).sendRedirect( "authorization.html");
                return;
            }
        }
        chain.doFilter(request, response);
    }


    @Override
    public void destroy() {

    }
}
