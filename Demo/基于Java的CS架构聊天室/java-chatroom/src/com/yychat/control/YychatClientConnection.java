package com.yychat.control;
import java.io.*;
import java.net.Socket;
import com.yychat.model.*;
import com.yychat.view.*;
public class YychatClientConnection {
	public static Socket s;
	public YychatClientConnection () {
		try {
			s=new Socket ("127.0.0.1",3456);
			System.out.println ("客户端连接成功: "+s) ;
			}
		catch (IOException e){
			e.printStackTrace ( );}
		}
	public Message loginValidate1 (User user) {
		Message mess=null;
		try {
			OutputStream os=s.getOutputStream();
			ObjectOutputStream oos=new ObjectOutputStream(os);
			oos.writeObject(user);
			
			ObjectInputStream ois=new ObjectInputStream(s.getInputStream());
			mess=(Message)ois.readObject();
			if(mess.getMessageType().equals(MessageType.LOGIN_VALIDATE_SUCCESS)) {
				ClientLogin.user=(User)ois.readObject();
				new ClientRecevierThread(s).start();
			}else
				s.close();
		}catch(IOException e) {
			e.printStackTrace();
		}catch(ClassNotFoundException e) {
			e.printStackTrace();
		}
		return mess;
	}
		public	static boolean registerUser(User user) {
			boolean registerSuccess=false;
			try {
				OutputStream os=s.getOutputStream();
				ObjectOutputStream oos=new ObjectOutputStream(os);
				oos.writeObject(user);
				 
				ObjectInputStream ois=new ObjectInputStream(s.getInputStream());
				Message mess=(Message)ois.readObject();
				
				if(mess.getMessageType().equals(MessageType.USER_REGISTER_SUCCESS)) {
					registerSuccess=true;
				}
				s.close();
				
			}catch(IOException e) {
				e.printStackTrace();
			}catch(ClassNotFoundException e) {
				e.printStackTrace();
			}	
			return registerSuccess;
		}
	}