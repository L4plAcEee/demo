package com.yychat.view;

import java.awt.Color;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.yychat.model.*;
import com.yychat.control.YychatClientConnection;
import java.io.OutputStream;
import java.io.ObjectOutputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class FriendChat extends JFrame implements ActionListener {
    JTextArea jta;
    JScrollPane jsp;
    JTextField jtf;
    JButton jb_send;
    String sender;
    String recevier,title;
    private static final long serialVersionUID = 1L;
  
    public FriendChat(String sender, String recevier, String title) {// 构造函数，创建聊天界面
    	this.sender=sender;
    	this.recevier=recevier;
    	this.title = title;
        // 初始化界面组件
        initUIComponents();
        // 设置窗口属性
        setWindowProperties(sender, recevier);
    }

    private void initUIComponents() {
        // 设置文本域
        jta = new JTextArea();
        jta.setForeground(Color.red);
        jta.setEditable(false);
        jta.setAutoscrolls(true);
        jsp = new JScrollPane(jta);
        this.add(jsp, "Center");
        
        // 设置文本框和发送按钮
        jtf = new JTextField(20);
        jtf.grabFocus();
        jb_send = new JButton("发送");
        jb_send.addActionListener(this);  
        jb_send.setForeground(Color.blue);
        JPanel jp = new JPanel();
        jp.add(jtf);
        jp.add(jb_send);
        
        this.add(jp, "South");
        
        // 设置窗口大小
        this.setSize(400, 300);
    }

    private void setWindowProperties(String sender, String recevier) {
        this.setLocationRelativeTo(null); 
        this.setTitle(title);
        this.setIconImage(new ImageIcon("images/duck2.gif").getImage());
        this.setVisible(true);
    }

    public void append(Message mess) {//实现append方法
    	jta.append(mess.getSendTime().toString()+"\r\n"+mess.getSender()+"对你说："+mess.getContent()+"\r\n");
    }

    public void actionPerformed(ActionEvent e) {// 发送按钮点击事件处理方法
    	if(e.getSource() == jb_send) {
    		String str = jtf.getText();
			// 文本框内容为空
			if("".equals(str)) {
				jtf.grabFocus();  // 设置焦点（可行）
				// 弹出消息对话框（警告消息）
				JOptionPane.showMessageDialog(this, "输入为空，请重新输入！", "提示", JOptionPane.WARNING_MESSAGE);
				return;
			}
            Message mess=new Message();
            mess.setUser(null);
            mess.setSender(sender);
            mess.setRecevier(recevier);
            mess.setContent(jtf.getText());
            mess.setSendTime();
            mess.setMessageType(MessageType.COMMON_CHAT_MESSAGE);
            jta.append(mess.getSendTime().toString()+"\r\n"+jtf.getText()+"\r\n");
            jtf.setText("");  // 清空文本框
			jtf.grabFocus();  // 设置焦点（可行）
            try {
            	OutputStream os=YychatClientConnection.s.getOutputStream();
            	ObjectOutputStream oos=new ObjectOutputStream(os);
            	oos.writeObject(mess);
            }catch(IOException e1) {
            	e1.printStackTrace();
            }
       }
}   
}
