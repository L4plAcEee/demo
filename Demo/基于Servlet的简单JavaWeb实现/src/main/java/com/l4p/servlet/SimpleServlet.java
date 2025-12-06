package com.l4p.servlet;

import javax.servlet.*;
import java.io.IOException;
import java.io.PrintWriter;

public class SimpleServlet implements Servlet {
    private ServletConfig config;  // 用于存储Servlet的配置信息

    /**
     * Servlet初始化方法，只调用一次
     */
    @Override
    public void init(ServletConfig config) throws ServletException {
        this.config = config;
        System.out.println("[init] Servlet 初始化完成");
    }

    /**
     * 返回Servlet配置信息
     */
    @Override
    public ServletConfig getServletConfig() {
        return config;
    }

    /**
     * 处理请求的核心方法，每次请求都会调用
     */
    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        System.out.println("[service] 开始处理请求");

        // 设置响应格式
        res.setContentType("text/html;charset=UTF-8");

        // 获取输出流，向客户端输出内容
        PrintWriter out = res.getWriter();
        out.println("<html><body>");
        out.println("<h2>Hello from MyFullServlet!</h2>");

        // 访问初始化参数
        String myParam = config.getInitParameter("myParam");
        out.println("<p>初始化参数 myParam = " + myParam + "</p>");

        // 访问ServletContext全局对象
        ServletContext context = config.getServletContext();
        out.println("<p>应用上下文路径：" + context.getContextPath() + "</p>");

        out.println("</body></html>");
    }

    /**
     * 返回Servlet的描述信息
     */
    @Override
    public String getServletInfo() {
        return "MyFullServlet - 完整手动实现Servlet接口的示例";
    }

    /**
     * 销毁Servlet，只在Servlet被卸载或服务器关闭时调用
     */
    @Override
    public void destroy() {
        System.out.println("[destroy] Servlet 被销毁");
    }
}
