package com.yychat.control;

import java.io.*;
import java.net.*;
import com.yychat.view.*;
import com.yychat.model.*;
import javax.swing.JOptionPane;

public class ClientRecevierThread extends Thread{
	Socket s;
	public ClientRecevierThread(Socket s) {
		this.s=s;
	}
	public void run() {
		while(true) {
			try {
				ObjectInputStream ois=new ObjectInputStream(s.getInputStream());
				Message mess=(Message)ois.readObject();
				if(mess.getMessageType().equals(MessageType.UESR_EXIT_CLIENT_THREAD_CLOSE)) {
					System.out.println("关闭"+mess.getSender()+"用户接收线程");
					s.close();
					break;
				}
				if(mess.getMessageType().equals(MessageType.FRIEND_REQUEST)) {
					int receiver = Integer.parseInt(mess.getRecevier());
					FriendList fl=(FriendList)ClientLogin.hmFriendList.get(receiver);
					String allFriendRequest=mess.getContent();
					fl.showAllRequest(allFriendRequest);
				}
				if(mess.getMessageType().equals(MessageType.USER_UPDATE_INFO_SUCCESS)) {
					System.out.println("修改用户信息成功!");
					User sender=mess.getUser();
					FriendList fl=(FriendList)ClientLogin.hmFriendList.get(sender.getUserID());
					fl.refreshPanel(sender);
				}
				if(mess.getMessageType().equals(MessageType.FRIEND_REQUEST_UNACCEPT)) {
					JOptionPane.showMessageDialog(null,"好友拒绝了你的申请！");
					int sender=Integer.parseInt(mess.getSender());
					FriendList fl=(FriendList)ClientLogin.hmFriendList.get(sender);
					fl.getFriendRequest();
				}
				if(mess.getMessageType().equals(MessageType.ADD_NEW_FRIEND_SUCCESS)) {
					JOptionPane.showMessageDialog(null,"添加好友成功！");
					int sender=Integer.parseInt(mess.getSender());
					FriendList fl=(FriendList)ClientLogin.hmFriendList.get(sender);
					String allFriend=mess.getContent();
					fl.showAllFriend(allFriend);
					fl.getFriendRequest();
				}
				
				if(mess.getMessageType().equals(MessageType.ADD_NEW_FRIEND_FAILURE_ALREADY_FRIEND)) {
					JOptionPane.showMessageDialog(null,"你们已经是好友了，添加失败！");
				}
				
				if(mess.getMessageType().equals(MessageType.ADD_NEW_FRIEND_FAILURE_NO_USER)) {
					JOptionPane.showMessageDialog(null,"新好友名字不存在，添加失败！");
				}
				
				if(mess.getMessageType().equals(MessageType.COMMON_CHAT_MESSAGE)) {
					String recevier=mess.getRecevier();
					String sender=mess.getSender();
					FriendChat fc=(FriendChat)FriendList.hmFriendChat.get(recevier+"to"+sender);
					if(fc!=null) {
						fc.append(mess);
					}else
						System.out.println("请打开"+recevier+"to"+sender+"的聊天界面");
				}
				
				if(mess.getMessageType().equals(MessageType.RESPONSE_ONLINE_FRIEND)) {
					FriendList fl=(FriendList)ClientLogin.hmFriendList.get(mess.getRecevier());
					fl.activeOnlineFriendIcon(mess);
				}
				
				if(mess.getMessageType().equals(MessageType.NEW_ONLINE_FRIEND)) {
					String recevier=mess.getRecevier();
					FriendList fl=(FriendList)ClientLogin.hmFriendList.get(recevier);
					String sender=mess.getSender();
					fl.activeNewOnlineFriendIcon(sender);
				}
			}catch(IOException e) {
				e.printStackTrace();
			}catch(ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

}
