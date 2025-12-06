package com.l4p.servlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import javax.servlet.ServletContext;
import java.io.IOException;

//@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 1. 获取客户端提交的数据
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        // 2. 简单验证（实际开发中应连接数据库）
        if ("admin".equals(username) && "123456".equals(password)) {
            // 登录成功处理

            // 创建 Cookie 保存用户信息
            Cookie cookie = new Cookie("username", username);
            cookie.setMaxAge(60 * 60); // 1小时有效
            response.addCookie(cookie);

            // 使用 Session 保存用户登录状态
            HttpSession session = request.getSession();
            session.setAttribute("user", username);

            // 使用应用域（ServletContext）统计登录人数
            ServletContext application = getServletContext();
            Integer loginCount = (Integer) application.getAttribute("loginCount");
            if (loginCount == null) {
                loginCount = 1;
            } else {
                loginCount = loginCount + 1;
            }
            application.setAttribute("loginCount", loginCount);

            // 请求域（Request）保存提示信息
            request.setAttribute("msg", "登录成功，欢迎您：" + username);

            // 页面转发到 welcome.jsp
            request.getRequestDispatcher("/welcome.jsp").forward(request, response);
        } else {
            // 登录失败处理

            // 请求域保存错误提示
            request.setAttribute("msg", "用户名或密码错误，请重新登录！");

            // 页面转发回登录页
            request.getRequestDispatcher("/login.jsp").forward(request, response);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 直接重定向到登录页面
        response.sendRedirect(request.getContextPath() + "/login.jsp");
    }
}
