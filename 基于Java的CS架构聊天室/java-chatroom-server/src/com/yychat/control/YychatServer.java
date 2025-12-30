package com.yychat.control;

import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.yychat.model.*;

public class YychatServer implements Runnable {
    private ServerSocket ss;
    private boolean running = false;
    private ExecutorService executorService;
    public static HashMap<User, Socket> hmSocket = new HashMap<>();
    public YychatServer() {
        this.executorService = Executors.newCachedThreadPool(); // 线程池
    }

    @Override
    public void run() {
        try {
            ss = new ServerSocket(3456);
            System.out.println(LocalDateTime.now() + " 服务器消息：" + "服务器线程已启动，正在监听3456端口...");

            while (running) {
                Socket s = ss.accept();
                System.out.println(LocalDateTime.now() + " 服务器消息：" + "连接成功: " + s);
                handleClient(s);
            }
            ss.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void handleClient(Socket s) {
        try {
            ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
            ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());

            User user = (User) ois.readObject();
            int userID = user.getUserID();
            String password = user.getPassword();
            System.out.println(LocalDateTime.now() + " 服务器消息：" + "服务器端接收到的客户端登录信息 userName : " + userID + " password: " + password);

            Message mess = new Message();

            if (user.getUserType().equals(UserType.USER_REGISTER)) {
                if (DBUtil.seekUser(user)) {
                    mess.setMessageType(MessageType.USER_REGISTER_FAILURE);
                } else {
                    DBUtil.insertIntoUser(user);
                    mess.setUser(user);
                    mess.setMessageType(MessageType.USER_REGISTER_SUCCESS);
                    oos.writeObject(mess);
                    s.close();
                    return;
                }
            }

            if (user.getUserType().equals(UserType.USER_LOGIN_VALIDATE)) {
                boolean loginSuccess = DBUtil.loginValidate(user);
                if (loginSuccess) {
                    System.out.println(LocalDateTime.now() + " 服务器消息：" + "密码验证通过！");

                    String allFriend = DBUtil.seekAllFriend(userID, 1);
                    mess.setContent(allFriend);
                    mess.setMessageType(MessageType.LOGIN_VALIDATE_SUCCESS);
                    oos.writeObject(mess);
                    user = DBUtil.setUserInfo(userID);
                    hmSocket.put(user, s);

                    oos.writeObject(user);
                    executorService.execute(new ServerRecevierThread(s));//线程池
                    System.out.println(LocalDateTime.now() + " 服务器消息：" + "启动线程成功！");
                } else {
                    System.out.println(LocalDateTime.now() + " 服务器消息：" + "密码验证失败！");
                    mess.setMessageType(MessageType.LOGIN_VALIDATE_FAILURE);
                    oos.writeObject(mess);
                    s.close();
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    public void startServer() {
        if (!running) {
            running = true;
            new Thread(this).start();
        }
    }

    public void closeServer() {
        running = false;
        executorService.shutdown(); // 线程池
    }
}
