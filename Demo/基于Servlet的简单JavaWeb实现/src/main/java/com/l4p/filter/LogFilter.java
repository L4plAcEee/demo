package com.l4p.filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogFilter implements Filter {

    private String logDir;
    private ServletContext servletContext;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // 保存 ServletContext 引用
        this.servletContext = filterConfig.getServletContext();

        // 计算 webapp 根目录下的 logs 目录
        String appRoot = servletContext.getRealPath("/");
        logDir = appRoot + File.separator + "logs";

        // 如果 logs 目录不存在，自动创建
        File dir = new File(logDir);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            System.out.println("日志目录创建：" + (created ? "成功" : "失败") + " -> " + logDir);
        }

        System.out.println("LogFilter 初始化完成，日志目录：" + logDir);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        // 获取客户端 IP
        String clientIp = request.getRemoteAddr();

        // 获取请求 URI（仅对 HttpServletRequest 有效）
        String uri = "";
        if (request instanceof HttpServletRequest) {
            uri = ((HttpServletRequest) request).getRequestURI();
        }

        // 时间戳与日期
        Date now = new Date();
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(now);
        String dateOnly  = new SimpleDateFormat("yyyyMMdd").format(now);

        // 构造日志内容和文件
        String logLine = String.format("[%s] IP: %s 访问: %s", timeStamp, clientIp, uri);
        String fileName = "tracking" + dateOnly + ".log";
        File logFile = new File(logDir, fileName);

        // 将日志追加到文件
        try (FileWriter fw = new FileWriter(logFile, true);
             BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write(logLine);
            bw.newLine();
        } catch (IOException e) {
            // 写入失败时输出堆栈，以便排查
            e.printStackTrace();
        }

        // 控制台输出（可留可删）
        System.out.println(logLine);

        // 继续后续处理
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        System.out.println("LogFilter 被销毁");
    }
}
