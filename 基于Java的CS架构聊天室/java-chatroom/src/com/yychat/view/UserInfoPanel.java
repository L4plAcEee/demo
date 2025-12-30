package com.yychat.view;

import javax.swing.*;

import com.yychat.control.YychatClientConnection;
import com.yychat.model.*;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.regex.Pattern;

public class UserInfoPanel extends JFrame {
    private JPanel cards;
    private JTextField idField, nicknameField, emailField, phoneField,pwdField;
    private JLabel idLabel, nicknameLabel, emailLabel, phoneLabel, avatarLabel;
    private JButton editButton, saveButton;
    private User user;

    public UserInfoPanel(User user) {
    	this.user = user;
        // 设置窗体基本属性
        setTitle("个人信息");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // 初始化CardLayout和容器Panel
        CardLayout cardLayout = new CardLayout();
        cards = new JPanel(cardLayout);

        // 第一个card: 显示个人信息
        JPanel viewPanel = new JPanel();
        viewPanel.setLayout(new GridLayout(6, 3, 10, 10)); // 使用网格布局
        
        avatarLabel = new JLabel(new ImageIcon("./images/0.jpg")); // 你可以替换为实际的图片路径
        idLabel = new JLabel("ID: "+user.getUserID());
        nicknameLabel = new JLabel("昵称: "+user.getUserName());
        emailLabel = new JLabel("邮箱: "+user.getEmail());
        phoneLabel = new JLabel("电话: "+user.getTelNubmer());
        
        editButton = new JButton("编辑信息");
        
        viewPanel.add(new JLabel());
        viewPanel.add(avatarLabel);
        viewPanel.add(new JLabel());
        viewPanel.add(new JLabel());
        viewPanel.add(idLabel);
        viewPanel.add(new JLabel());
        viewPanel.add(new JLabel());
        viewPanel.add(nicknameLabel);
        viewPanel.add(new JLabel());
        viewPanel.add(new JLabel());
        viewPanel.add(emailLabel);
        viewPanel.add(new JLabel());
        viewPanel.add(new JLabel());
        viewPanel.add(phoneLabel);
        viewPanel.add(new JLabel());
        viewPanel.add(new JLabel());
        viewPanel.add(editButton);
        viewPanel.add(new JLabel());

        // 第二个card: 填写个人信息
        JPanel editPanel = new JPanel();
        editPanel.setLayout(new GridLayout(6, 4, 10, 10));
        
        editPanel.add(new JLabel());
        editPanel.add(new JLabel("ID:"+ user.getUserID()));
        editPanel.add(new JLabel());
        editPanel.add(new JLabel());
        
        editPanel.add(new JLabel());
        editPanel.add(new JLabel("密码: ")); 
        pwdField = new JTextField(user.getPassword());editPanel.add(pwdField);
        editPanel.add(new JLabel());
        
        editPanel.add(new JLabel());
        editPanel.add(new JLabel("昵称:"));
        nicknameField = new JTextField(user.getUserName());editPanel.add(nicknameField);
        editPanel.add(new JLabel());
        
        editPanel.add(new JLabel());
        editPanel.add(new JLabel("邮箱:"));
        emailField = new JTextField(user.getEmail());editPanel.add(emailField);
        editPanel.add(new JLabel());
        
        editPanel.add(new JLabel());
        editPanel.add(new JLabel("电话:"));
        phoneField = new JTextField(user.getTelNubmer());editPanel.add(phoneField);
        editPanel.add(new JLabel());
        
        editPanel.add(new JLabel());
        saveButton = new JButton("保存信息");editPanel.add(saveButton);
        
        cards.add(viewPanel, "View");
        cards.add(editPanel, "Edit");
        
        // 添加按钮事件监听器
        editButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(cards, "Edit");
            }
        });

        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String nickname = nicknameField.getText().trim();
                String password = pwdField.getText().trim();
                String email = emailField.getText().trim();
                String phone = phoneField.getText().trim();

                // 定义邮箱和电话号码的正则表达式
                Pattern emailPattern = Pattern.compile("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$");
                Pattern phonePattern = Pattern.compile("^\\d{11}$");

                // 验证输入是否为空
                if (nickname.isEmpty() || password.isEmpty() || email.isEmpty() || phone.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "请填写所有信息", "警告", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // 验证邮箱格式
                if (!emailPattern.matcher(email).matches()) {
                    JOptionPane.showMessageDialog(null, "邮箱格式不正确", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // 验证电话号码格式
                if (!phonePattern.matcher(phone).matches()) {
                    JOptionPane.showMessageDialog(null, "电话号码格式不正确", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // 设置用户信息
                user.setUserName(nickname);
                user.setPassword(password);
                user.setEmail(email);
                user.setTelNumber(phone);

                // 创建消息对象并设置其属性
                Message mess = new Message();
                mess.setUser(user);
                mess.setSender(user.getUserName());
                mess.setRecevier("Server");
                mess.setMessageType(MessageType.USER_UPDATE_INFO);

                // 发送消息
                try {
                    ObjectOutputStream oos = new ObjectOutputStream(YychatClientConnection.s.getOutputStream());
                    oos.writeObject(mess);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }

                // 切换视图
                JOptionPane.showMessageDialog(null, "修改信息成功！", "修改信息", JOptionPane.CLOSED_OPTION);
                cardLayout.show(cards, "View");
                nicknameLabel.setText("昵称: "+user.getUserName());
                emailLabel.setText("邮箱: "+user.getEmail());
                phoneLabel.setText("电话: "+user.getTelNubmer());
                viewPanel.repaint();
            }
        });
        add(cards);
        setVisible(true);
    }
}
