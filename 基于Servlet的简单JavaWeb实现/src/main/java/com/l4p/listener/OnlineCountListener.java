package com.l4p.listener;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

public class OnlineCountListener implements HttpSessionListener {

    // 在线人数（注意：实际项目中应该考虑并发安全，这里为了示例简单化）
    private static int onlineCount = 0;

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        onlineCount++;
        System.out.println("[监听器] 新会话创建，当前在线人数：" + onlineCount);
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        onlineCount--;
        System.out.println("[监听器] 会话销毁，当前在线人数：" + onlineCount);
    }

    // 提供一个公共方法，供其他地方调用
    public static int getOnlineCount() {
        return onlineCount;
    }
}
