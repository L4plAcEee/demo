package com.yychat.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import com.yychat.model.*;
import com.yychat.control.*;
import java.io.*;

public class FriendList extends JFrame implements ActionListener, MouseListener{
    // 声明各种组件和变量
	public static HashMap<String,FriendChat>hmFriendChat=new HashMap<String,FriendChat>();
	
    JPanel friendPanel; 
    JButton myFriendButton1;
    JButton friendRequestButton1;
    JButton userInfoButton1;
    JScrollPane friendListScrollPane;
    JPanel friendListPanel;
    final int MYFRIENDCOUNT = 50;
    JLabel friendLabel[] = new JLabel[MYFRIENDCOUNT];

    JPanel addFriendRequestPanel;
    JButton myFriendButton2;
    JButton friendRequestButton2;
    JButton userInfoButton2;
    JScrollPane requestScrollPane;
    JPanel requestListPanel;
    final int REQUESTCOUNT = 20;
    JLabel requestLabel[] = new JLabel[REQUESTCOUNT];

    CardLayout cardlayout; // 卡片布局管理器
    User user; // 用户
    
    JPanel addFriendJPanel;
    JButton addFriendButton;
    
    String allFriendRequest;
    
    private static final long serialVersionUID = 1L;

    public FriendList(User user, String allFriend) {
        this.user = user;
        initializeComponents(allFriend); // 初始化界面组件
        setWindowProperties(); // 设置窗口属性
    }

    private void initializeComponents(String allFriend) {
        // 初始化好友面板及其组件
        initializeFriendPanel(allFriend);
        // 初始化陌生人面板及其组件
        initializeRequestPanel();
    }

    private void initializeFriendPanel(String allFriend) {
        friendPanel = new JPanel(new BorderLayout());
        myFriendButton1 = new JButton("我的好友");
        
        addFriendJPanel = new JPanel(new GridLayout(2, 1));
        addFriendButton = new JButton("添加好友");
        addFriendButton.addActionListener(this);
        friendPanel.add(addFriendButton, BorderLayout.NORTH);
        
        friendListPanel = new JPanel();
        showAllFriend(allFriend);
        
        friendListScrollPane = new JScrollPane(friendListPanel);
        friendPanel.add(friendListScrollPane, BorderLayout.CENTER);
        
        friendRequestButton1 = new JButton("好友请求");
        friendRequestButton1.addActionListener(this);
        userInfoButton1 = new JButton("个人资料");
        userInfoButton1.addActionListener(this);
        JPanel stranger_BlackPanel = new JPanel(new GridLayout(2, 1));
        stranger_BlackPanel.add(friendRequestButton1);
        stranger_BlackPanel.add(userInfoButton1);
        friendPanel.add(stranger_BlackPanel, BorderLayout.SOUTH);
    }

    private void initializeRequestPanel() {
        addFriendRequestPanel = new JPanel(new BorderLayout());
        myFriendButton2 = new JButton("我的好友");
        myFriendButton2.addActionListener(this);
        friendRequestButton2 = new JButton("好友请求");
        JPanel friend_strangerPanel = new JPanel(new GridLayout(2, 1));
        friend_strangerPanel.add(myFriendButton2);
        friend_strangerPanel.add(friendRequestButton2);
        addFriendRequestPanel.add(friend_strangerPanel, BorderLayout.NORTH);
        
        requestListPanel = new JPanel(new GridLayout(REQUESTCOUNT, 1));
        String allFriendRequest = "";
		showAllRequest(allFriendRequest);
        requestScrollPane = new JScrollPane(requestListPanel);
        addFriendRequestPanel.add(requestScrollPane, BorderLayout.CENTER);
        
        userInfoButton2 = new JButton("个人资料");
        userInfoButton2.addActionListener(this);
        addFriendRequestPanel.add(userInfoButton2, BorderLayout.SOUTH);
    }

    private void setWindowProperties() {
        // 设置窗口属性
        cardlayout = new CardLayout(); // 创建卡片布局管理器
        this.setTitle(user.getUserName()+"的好友列表");
        this.setLayout(cardlayout); // 设置布局管理器
        this.add(friendPanel, "cardFriend"); // 添加好友面板到主面板，并指定卡片名称为 "card1"
        this.add(addFriendRequestPanel, "cardRequest"); // 添加陌生人面板到主面板，并指定卡片名称为 "card2"
        cardlayout.show(this.getContentPane(), "card1"); // 默认显示好友面板
        this.setIconImage(new ImageIcon("images/duck2.gif").getImage());
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setBounds(800, 600, 350, 250);
        setResizable(false);
        this.setVisible(true);
        this.addWindowListener(new WindowAdapter() {
        	public void windowClosing(WindowEvent arg0) {
            	System.out.println(user.getUserName()+"准备关闭客户端...");
            	Message mess=new Message();
            	mess.setUser(user);
            	mess.setSender(user.getUserName());
            	mess.setRecevier("Server");
            	mess.setMessageType(MessageType.UESR_EXIT_SERVER_THREAD_CLOSE);
            	ObjectOutputStream oos;
            	try {
            		oos=new ObjectOutputStream(YychatClientConnection.s.getOutputStream());
            		oos.writeObject(mess);
            	}catch(IOException e) {
            		e.printStackTrace();
            	}
            	System.exit(0);
            }
        });
        
    }
    public void showAllRequest(String allFriendRequest) {
        String[] myFriendRequest = allFriendRequest.split(" ");
        requestListPanel.removeAll();
        requestListPanel.setLayout(new GridLayout(myFriendRequest.length - 1, 1));
        requestLabel = new JLabel[myFriendRequest.length]; // 初始化数组

        for (int i = 1; i < myFriendRequest.length; i++) {
            requestLabel[i] = new JLabel(myFriendRequest[i], new ImageIcon("images/tortoise.gif"), JLabel.LEFT);
            requestLabel[i].setForeground(Color.black);
            
            requestLabel[i].addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) { // 双击检测
                        int response = JOptionPane.showConfirmDialog(null, 
                                "你是否同意 " + ((JLabel)e.getSource()).getText() + " 的好友请求？", 
                                "好友请求", 
                                JOptionPane.YES_NO_OPTION);
                        String newFriend = ((JLabel)e.getSource()).getText();
                        int index = newFriend.indexOf("#");
                        if (index != -1) {
                            String number = newFriend.substring(index + 1);
                            newFriend = number;
                        }

                        if (response == JOptionPane.YES_OPTION) {
                            // 用户同意好友请求的逻辑
                            System.out.println("用户同意了好友请求");
                            Message mess = new Message();
                            
                            mess.setMessageType(MessageType.FRIEND_REQUEST_ACCEPT);
                            mess.setUser(user);
                            mess.setSender(String.valueOf(user.getUserID()));
                            mess.setContent(newFriend);
                            mess.setRecevier("Server");
                            ObjectOutputStream oos = null;
                            try {
                                oos = new ObjectOutputStream(YychatClientConnection.s.getOutputStream());
                                oos.writeObject(mess);
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        } else {
                            // 用户拒绝好友请求的逻辑
                            System.out.println("用户拒绝了好友请求");
                            Message mess = new Message();
                            mess.setMessageType(MessageType.FRIEND_REQUEST_UNACCEPT);
                            mess.setUser(user);
                            mess.setSender(String.valueOf(user.getUserID()));
                            mess.setContent(newFriend);
                            mess.setRecevier("Server");
                            ObjectOutputStream oos = null;
                            try {
                                oos = new ObjectOutputStream(YychatClientConnection.s.getOutputStream());
                                oos.writeObject(mess);
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                }
            });

            requestListPanel.add(requestLabel[i]);
        }

        requestListPanel.revalidate();
    }

    public void showAllFriend(String allFriend) {
    	String[] myFriend=allFriend.split(" ");
    	friendListPanel.removeAll();
    	friendListPanel.setLayout(new GridLayout(myFriend.length-1, 1));
	      for(int i=1;i<myFriend.length;i++) {
	    	String imageStr="images/"+i%6+".jpg";
	    	ImageIcon imageIcon = new ImageIcon(imageStr);
	    	friendLabel[i] = new JLabel(myFriend[i], imageIcon, JLabel.LEFT);
	    	friendLabel[i].setForeground(Color.blue);
	    	friendLabel[i].addMouseListener(this);
	    	friendListPanel.add(friendLabel[i]);
	      }
	    friendListPanel.revalidate();
    }
    
    public void activeNewOnlineFriendIcon(String newOnlineFriend) {
    	this.friendLabel[Integer.valueOf(newOnlineFriend)].setEnabled(true);
    }
    
    public void activeOnlineFriendIcon(Message mess) {
    	String onlineFriend=mess.getContent();
    	String[] onlineFriendName=onlineFriend.split(" ");
    	for(int i=1;i<onlineFriendName.length;i++) {
    		this.friendLabel[Integer.valueOf(onlineFriendName[i])].setEnabled(true);
    	}
    }
    public void refreshPanel(User user) {
    	this.setTitle(user.getUserName()+"的好友列表");
    	this.repaint();
    }
    public void getFriendRequest() {
    	Message mess = new Message();
        mess.setUser(user);
        mess.setSender(String.valueOf(user.getUserID()));
        mess.setRecevier("Server");
        mess.setMessageType(MessageType.FRIEND_REQUEST);
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(YychatClientConnection.s.getOutputStream());
            oos.writeObject(mess);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void actionPerformed(ActionEvent arg0) {// 按钮点击事件处理方法
        if (arg0.getSource() == addFriendButton) {
            String newFriend = JOptionPane.showInputDialog("请输入新好友的ID：");
            System.out.println("newFriend:" + newFriend);
            if (newFriend != null) {
                Message mess = new Message();
                mess.setUser(user);
                mess.setSender(String.valueOf(user.getUserID()));
                mess.setRecevier(newFriend);
                mess.setContent(newFriend);
                mess.setMessageType(MessageType.ADD_NEW_FRIEND);
                ObjectOutputStream oos = null;
                try {
                    oos = new ObjectOutputStream(YychatClientConnection.s.getOutputStream());
                    oos.writeObject(mess);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
            
        if (arg0.getSource() == myFriendButton2) {
            cardlayout.show(this.getContentPane(), "cardFriend"); // 显示好友面板
        }
        if (arg0.getSource() == friendRequestButton1) {
        	getFriendRequest();
            cardlayout.show(this.getContentPane(), "cardRequest"); // 显示请求面板
        }
        if (arg0.getSource() == userInfoButton1||arg0.getSource() ==userInfoButton2) {
        	SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    UserInfoPanel uip = new UserInfoPanel(user);
                }
                });            
        }
    }

    public void mouseClicked(MouseEvent arg0) {// 鼠标点击事件处理方法
        if (arg0.getClickCount() == 2) {
            JLabel jl = (JLabel) arg0.getSource();
            String toName = jl.getText();
            int index = toName.indexOf("#");
            if (index != -1) {
                String number = toName.substring(index + 1);
                toName = number;
            }
            FriendChat fc=new FriendChat(String.valueOf(user.getUserID()),toName," 和 " + jl.getText() + " 的聊天界面");
            hmFriendChat.put(String.valueOf(user.getUserID())+"to"+toName, fc);
        }
    }

    public void mouseEntered(MouseEvent arg0) {// 鼠标进入事件处理方法
        JLabel jl = (JLabel) arg0.getSource();
        jl.setForeground(Color.red); // 鼠标进入时字体颜色改为红色
    }
    
    public void mouseExited(MouseEvent arg0) {// 鼠标退出事件处理方法
        JLabel jl = (JLabel) arg0.getSource();
        jl.setForeground(Color.blue); // 鼠标退出时字体颜色改为蓝色
    }

    public void mousePressed(MouseEvent arg0) {
    }

    public void mouseReleased(MouseEvent arg0) {
    }
}

    

