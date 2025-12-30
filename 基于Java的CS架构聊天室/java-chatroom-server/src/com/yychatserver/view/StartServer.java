package com.yychatserver.view;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

import com.yychat.control.DBUtil;
import com.yychat.control.YychatServer;
import com.yychat.model.*;

public class StartServer extends JFrame implements ActionListener {
    JButton jb1, jb2, jb3;
    JTextField jt1;
    private YychatServer ys;
    private static final long serialVersionUID = 1L;
    
    public StartServer() {
      initComponents();
      setupUI();
    }

    private void initComponents() {
      jb1 = new JButton("启动服务器");
      jb1.setFont(new Font("宋体", Font.BOLD, 25));
      jb1.addActionListener(this);

      jb2 = new JButton("停止服务器");
      jb2.setFont(new Font("宋体", Font.BOLD, 25));
      jb2.addActionListener(this);

      jb3 = new JButton("查找用户");
      jb3.setFont(new Font("宋体", Font.BOLD, 25));
      jb3.addActionListener(this);

      jt1 = new JTextField();
      jt1.setFont(new Font("宋体", Font.PLAIN, 20));
    }

    private void setupUI() {
      this.setLayout(new GridLayout(2, 2));
      this.add(jb1);
      this.add(jb2);
      this.add(jt1);
      this.add(jb3);
      this.setSize(400, 200);
      this.setLocationRelativeTo(null);
      this.setIconImage(new ImageIcon("images/duck2.gif").getImage());
      this.setTitle("YYchat服务器");
      this.setVisible(true);
    }

    public void actionPerformed(ActionEvent arg0) {
      if (arg0.getSource() == jb1) {
        ys = new YychatServer();
        ys.startServer();
        jb1.setEnabled(false);
      }
      if (arg0.getSource() == jb2) {
        ys.closeServer();
        this.dispose();
      }
      if (arg0.getSource() == jb3) {
        String userID = jt1.getText();
        String user = DBUtil.searchUser(Integer.parseInt(userID));
        if (user != null) {
          JOptionPane.showMessageDialog(this, "找到用户: " + user+"#"+userID);
        } else {
          JOptionPane.showMessageDialog(this, "未找到用户: " + user+"#"+userID);
        }
      }
    }

    public static void main(String[] args) {
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          StartServer ss = new StartServer();
        }
      });
    }
}
