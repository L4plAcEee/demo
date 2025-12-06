package com.l4p.servlet;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 一个结合 HttpServletRequest 和 HttpServletResponse 的示例
 */
public class MySimpleHttpServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // 设置响应类型和编码
        resp.setContentType("text/html;charset=UTF-8");

        // 获取请求参数
        String name = req.getParameter("name"); // 比如浏览器传 ?name=Tom

        // 获取输出流
        PrintWriter out = resp.getWriter();
        out.println("<html><body>");

        // 判断是否有传参数
        if (name != null && !name.isEmpty()) {
            out.println("<h2>Hello, " + name + "!</h2>");
        } else {
            out.println("<h2>Hello from MySimpleHttpServlet!</h2>");
        }

        out.println("</body></html>");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // 设置响应类型和编码
        resp.setContentType("text/html;charset=UTF-8");

        // 获取请求参数
        String username = req.getParameter("username");
        String password = req.getParameter("password");

        // 获取输出流
        PrintWriter out = resp.getWriter();
        out.println("<html><body>");

        if (username != null && password != null) {
            out.println("<h2>Received POST data</h2>");
            out.println("<p>Username: " + username + "</p>");
            out.println("<p>Password: " + password + "</p>");
        } else {
            out.println("<h2>No POST data received</h2>");
        }

        out.println("</body></html>");
    }
}
