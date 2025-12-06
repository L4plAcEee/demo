package com.l4p.filter;

import com.l4p.model.pojo.Student;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebFilter(filterName = "LoginFilter", urlPatterns = "/*")  // 拦截所有页面
public class LoginFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
        // 初始化配置，可留空
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession session = httpRequest.getSession(false);

        String requestURI = httpRequest.getRequestURI();

        if (requestURI.contains("login.jsp") || requestURI.contains("register.jsp") || requestURI.contains("logout.jsp") || requestURI.contains("api/")) {
            chain.doFilter(request, response);
        } else {
            // 判断用户是否已登录
            Student student = (session != null) ? (Student) session.getAttribute("student") : null;

            if (student == null) {
                // 未登录，重定向到登录页面
                httpResponse.sendRedirect(httpRequest.getContextPath() + "/login.jsp");
            } else {
                // 已登录，放行
                chain.doFilter(request, response);
            }
        }
    }

    @Override
    public void destroy() {
        // 销毁时的资源释放，可留空
    }
}
