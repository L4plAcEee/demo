package com.yychat.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import com.yychat.control.YychatClientConnection;
import com.yychat.manager.LoginManager;
import com.yychat.model.*;
import java.io.*;
import java.util.HashMap;
import java.net.Socket;

public class ClientLogin extends JFrame implements ActionListener,ItemListener {
	// 定义全局内容
	public static HashMap<Integer,FriendList> hmFriendList=new HashMap<>();
	private static boolean loginRememberCheck = false;
	private static final long serialVersionUID = 1L;
    private static final String IMAGE_PATH = "images/";
    private Timer fadeInTimer;
    private Timer fadeOutTimer;
    private float opacity = 0.0f;
	private int initialX;
    private int initialY;
    public static User user;
    
    // 定义各种组件
    JLabel backGround;
    JPanel window;
    JPanel jHead;
    JButton jbLogin, jbRegister, jbCancel;
    JPanel jBottom;
    JLabel jl1, jl2, jl3, jl4;
    JTextField jtf_User;
    JPasswordField jpf_Password;
    JButton jbClear;
    JCheckBox jc1, jcRememberPwd;
    JPanel jBody,jBody1,jBody2,jBody3;
    
 
   
    public ClientLogin() { // 构造函数，创建客户端登录界面
        LoginManager lm=new LoginManager();
        initUI();
        setUIProperties();
        addUIComponents();
        setWindowProperties();
        
        jtf_User.setText(lm.getLocUserName());
        jpf_Password.setText(lm.getLocUserPwd());
        if(!jtf_User.getText().isEmpty())
        	jcRememberPwd.setSelected(true);
    }

    private void initUI() {//初始化UI
        //jl1 = new JLabel("YY号码：", JLabel.CENTER);
        //jl2 = new JLabel("YY密码：", JLabel.CENTER);
        //jl3 = new JLabel("忘记密码", JLabel.CENTER);
        //jl3.setForeground(Color.blue);
    	
        jbClear = new JButton(new ImageIcon(IMAGE_PATH + "clear.gif"));
        
        jtf_User = new JTextField();
        jtf_User.setPreferredSize(new Dimension(256,45));
        
        jpf_Password = new JPasswordField();
        jpf_Password.setPreferredSize(new Dimension(256,45));
        
        jcRememberPwd = new JCheckBox("记住密码");
      
        window=(JPanel)this.getContentPane();
        window.setOpaque(false);
        window.setLayout(new FlowLayout());
        
        backGround=new JLabel(new ImageIcon(IMAGE_PATH + "windowbackground.jpg"));
        backGround.setSize(320,448);
        
        jBody = new JPanel(new FlowLayout());
        jBody.setPreferredSize(new Dimension(320,260));
        jBody.setOpaque(false);
        
        jBody1 = new JPanel(new FlowLayout());
        jBody1.setPreferredSize(new Dimension(320,100));
        jBody1.setOpaque(false);
        
        jBody2 = new JPanel(new FlowLayout());
        jBody2.setOpaque(false);
        
        jBody3 = new JPanel();
        jBody3.setPreferredSize(new Dimension(320,100));
        jBody3.setOpaque(false);

        jHead = new JPanel(new BorderLayout());
        jHead.setPreferredSize(new Dimension(320,28));
        jHead.setOpaque(false);
        
        jbLogin = new JButton(new ImageIcon(IMAGE_PATH + "login.gif"));
        jbLogin.setPreferredSize(new Dimension(256, 50));
        
        jbRegister = new JButton(new ImageIcon(IMAGE_PATH + "register.gif"));
        jbRegister.setPreferredSize(new Dimension(256,50));
        
        jbCancel = new JButton(new ImageIcon(IMAGE_PATH + "cancel.gif"));
        jbCancel.setPreferredSize(new Dimension(28,28));
        
        jBottom = new JPanel();
        jBottom.setPreferredSize(new Dimension(320,160));
        jBottom.setOpaque(false);
    }

    private void setUIProperties() {//设置UI属性
        jHead.add(jbCancel, BorderLayout.EAST);
        
        jBody1.add(jtf_User);
        jBody1.add(jpf_Password);

        jBody2.add(jbClear);
        jBody2.add(jcRememberPwd);
        jBody.add(jBody3);
        jBody.add(jBody1);
        jBody.add(jBody2);
        
        
        jBottom.add(jbLogin);
        jBottom.add(jbRegister);
        jBottom.setBackground(null);

        jcRememberPwd.addItemListener(this);
        jbLogin.addActionListener(this);
        jbRegister.addActionListener(this);
        jbCancel.addActionListener(this);
        jbClear.addActionListener(this);
        jbLogin.setBackground(null);
        // UI动画逻辑
        jbCancel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
            	jbCancel.setIcon(new ImageIcon(IMAGE_PATH + "cancel2.gif"));
            }

            @Override
            public void mouseExited(MouseEvent e) {
            	jbCancel.setIcon(new ImageIcon(IMAGE_PATH + "cancel.gif")); 
            }
        });
    }

    @SuppressWarnings("removal")
	private void addUIComponents() {//设置UI布局
    	window.add(jHead);
        window.add(jBody);
        window.add(jBottom);
        this.getLayeredPane().add(backGround,new Integer(Integer.MIN_VALUE));
    }

    private void setWindowProperties() {//设置窗口属性
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setSize(320, 448);
        this.setTitle("YY聊天");
        this.setUndecorated(true); // 设置为无标题栏和边框
        this.setVisible(true);
        // 创建淡入定时器
        fadeInTimer = new Timer(50, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                opacity += 0.1f;
                if (opacity >= 1.0f) {
                    opacity = 1.0f;
                    fadeInTimer.stop();
                }
                setOpacity(opacity);
            }
        });

        // 创建淡出定时器
        fadeOutTimer = new Timer(50, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                opacity -= 0.1f;
                if (opacity <= 0.0f) {
                    opacity = 0.0f;
                    fadeOutTimer.stop();
                    dispose(); // 关闭窗口
                }
                setOpacity(opacity);
            }
        });

        // 淡入窗口
        fadeInTimer.start();
        // 添加鼠标事件监听器
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                initialX = e.getX();
                initialY = e.getY();
            }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int newX = getLocation().x + e.getX() - initialX;
                int newY = getLocation().y + e.getY() - initialY;
                setLocation(newX, newY);
            }
        });
    }


    // 主方法
    public static void main(String[] args) {
    	SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	
                new ClientLogin();

            }
            });            
    }
    
    public void itemStateChanged(ItemEvent e) {
    	if(e.getSource()==jcRememberPwd) {
    		if(e.getStateChange()==ItemEvent.SELECTED) {
    			loginRememberCheck = true;
    		}
    		if(e.getStateChange()==ItemEvent.DESELECTED) {
    			loginRememberCheck = false;
    		}
    	}
    }
    // 按钮点击事件处理方法
	public void actionPerformed(ActionEvent arg0) {
    	  int userID = 0;
    	  String password=null;
	      
	      if(arg0.getSource()==jbClear) {
	    	  jtf_User.setText(null);
	    	  jpf_Password.setText(null);
	      }
	      if(arg0.getSource()==jbCancel) {
	    	  fadeOutTimer.start();
	      }
          if(arg0.getSource()==jbRegister) {
        	  SwingUtilities.invokeLater(new Runnable() {
                  public void run() {
                  	
                      @SuppressWarnings("unused")
					RegisterWindow rw = new RegisterWindow();

                  }
                  });            
            	this.dispose();
            }  
            if (arg0.getSource() == jbLogin) {
	            if(!jtf_User.getText().isEmpty()){
	      	    	userID = Integer.parseInt(jtf_User.getText());
	      	    }else {
	      	    	JOptionPane.showMessageDialog(this, "账号为空，请输入账号！");
	      	    	return;
	      	    }
	      	    if(jpf_Password.getPassword().length!=0) {
	      	    	password = new String(jpf_Password.getPassword());
	      	    }else {
	      	    	JOptionPane.showMessageDialog(this, "密码为空，请输入密码！");
	      	    	return;
	      	    } 
            	user =new User();
      	      	user.setUserID(userID);
    	      	user.setPassword(password);
            	user.setUserType(UserType.USER_LOGIN_VALIDATE);
            	Message mess=new YychatClientConnection().loginValidate1(user);
            	if(loginRememberCheck) {
            		LoginManager.saveToFile(userID, password);
            	}else
            		LoginManager.saveToFile();
            	if(mess.getMessageType().equals(MessageType.LOGIN_VALIDATE_SUCCESS)) {
            		String allFriend=mess.getContent();
            		System.out.println(user.toString());
            		
            		FriendList fl=new FriendList(user,allFriend);
            		hmFriendList.put(user.getUserID(), fl);
            		this.dispose();
            	}else {
            		JOptionPane.showMessageDialog(this, "密码错误，请重新登录！");
            	}
        }
    }
    
    public void sendMessage(Socket s,Message mess) {
    	ObjectOutputStream oos;
    	try {
    		oos=new ObjectOutputStream(s.getOutputStream());
    		oos.writeObject(mess);
    	}catch(IOException e){
    		e.printStackTrace();
    	}
    }
    
}