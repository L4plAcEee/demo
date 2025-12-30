package com.yychat.control;

import java.sql.*;
import java.time.LocalDateTime;

import com.yychat.model.*;
import java.util.Date;

public class DBUtil {
	public static String db_url="jdbc:mysql://127.0.0.1:3306/yychat2022s?useUnicode=true&characterEncoding=utf-8";
	public static String db_username="root";
	public static String db_password="123456";
	public static Connection conn=getConnection();
	public static void deleteFriendRequest(int userID,int requestID) {
		String str = "DELETE FROM request WHERE userid = ? AND requestid = ?";
		PreparedStatement psmt = null;
		try {
			psmt = conn.prepareStatement(str);
            psmt.setInt(1, userID);
            psmt.setInt(2, requestID);
            int rowsUpdated = psmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println(LocalDateTime.now() + " 删除好友请求成功 -");
            } else {
                System.out.println(LocalDateTime.now() + " 删除好友请求失败 - 没有任何行受影响。");
            }
		}catch (SQLException e) {
            e.printStackTrace();
            System.out.println(LocalDateTime.now() + " 数据库更新异常 - " + e.getMessage());
        } finally {
            if (psmt != null) {
                try {
                    psmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
	}
	
	public static String seekFriendRequest(int userID) {
		String allFriendRequest = "";
		String str = "select * from request where userid=?";
		PreparedStatement psmt = null;
		try {
			psmt = conn.prepareStatement(str);
            psmt.setInt(1, userID);
            ResultSet rs=psmt.executeQuery();
			while(rs.next()) 
				allFriendRequest=allFriendRequest+" "+searchUser(rs.getInt("requestid"))+"#"+rs.getInt("requestid");
			System.out.println(userID+"全部好友请求"+allFriendRequest);
		}catch (SQLException e) {
            e.printStackTrace();
            System.out.println(LocalDateTime.now() + " 数据库更新异常 - " + e.getMessage());
        } finally {
            if (psmt != null) {
                try {
                    psmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
		return allFriendRequest;
	}
	public static void saveFriendRequest(int userid,int requestid) {
		String str = "insert into request(userid,requestid) value(?,?)";
		PreparedStatement psmt = null;
		try {
			psmt = conn.prepareStatement(str);
            psmt.setInt(1, userid);
            psmt.setInt(2, requestid);
            int rowsUpdated = psmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println(LocalDateTime.now() + " 存储好友请求成功 -");
            } else {
                System.out.println(LocalDateTime.now() + " 存储好友请求失败 - 没有任何行受影响。");
            }
		}catch (SQLException e) {
            e.printStackTrace();
            System.out.println(LocalDateTime.now() + " 数据库更新异常 - " + e.getMessage());
        } finally {
            if (psmt != null) {
                try {
                    psmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
	}
	public static void userUpdateInfo(User user) {
	    if (seekUser(user)) {
	        String userUpdateInfo_str =
	                  "UPDATE user \r\n"
	                + "SET username = ?, password = ?, email = ?, telnumber = ? \r\n"
	                + "WHERE id = ?";
	        PreparedStatement psmt = null;
	        try {
	            System.out.println(LocalDateTime.now() + " 正在准备更新用户信息 - " + user.getUserName());
	            
	            // 禁用外键约束
	            conn.createStatement().execute("SET FOREIGN_KEY_CHECKS=0");
	            
	            // 更新用户信息
	            psmt = conn.prepareStatement(userUpdateInfo_str);
	            psmt.setString(1, user.getUserName());
	            psmt.setString(2, user.getPassword());
	            psmt.setString(3, user.getEmail());
	            psmt.setString(4, user.getTelNubmer());
	            psmt.setInt(5, user.getUserID());
	            int rowsUpdated = psmt.executeUpdate();

	            if (rowsUpdated > 0) {
	                System.out.println(LocalDateTime.now() + " 用户信息更新成功 - " + user.getUserName());
	            } else {
	                System.out.println(LocalDateTime.now() + " 用户信息更新失败 - " + user.getUserName() + "。没有任何行受影响。");
	            }

	            // 启用外键约束
	            conn.createStatement().execute("SET FOREIGN_KEY_CHECKS=1");
	        } catch (SQLException e) {
	            e.printStackTrace();
	            System.out.println(LocalDateTime.now() + " 数据库更新异常 - " + e.getMessage());
	        } finally {
	            if (psmt != null) {
	                try {
	                    psmt.close();
	                } catch (SQLException e) {
	                    e.printStackTrace();
	                }
	            }
	        }
	    } else {
	        System.out.println(LocalDateTime.now() + " 用户不存在 - " + user.getUserName());
	    }
	}


	public static void saveMessage(Message mess) {
		String message_insertInto_str="insert into message(sender,receiver,content,sendtime) values(?,?,?,?)";
		PreparedStatement psmt;
		try {
			psmt=conn.prepareStatement(message_insertInto_str);
        	psmt.setString(1, mess.getSender());
         	psmt.setString(2, mess.getRecevier());
         	psmt.setString(3, mess.getContent());
         	Date sendTime=mess.getSendTime();
         	psmt.setTimestamp(4, new java.sql.Timestamp(sendTime.getTime()));
         	psmt.execute();
		}catch(SQLException e) {
			e.printStackTrace();
		}
	}
	public static int insertIntoFriend(int senderID,int newFriendID,int friendType) {
		int count=0;
		String userrelation_insertInto_str="insert into userrelation(masteruserid,slaveuserid,relation) values(?,?,?)";
		PreparedStatement psmt;
		try {
			psmt=conn.prepareStatement(userrelation_insertInto_str);
        	psmt.setInt(1, senderID);
//        	psmt.setString(2,sender);
         	psmt.setInt(2, newFriendID);
//         	psmt.setString(4, newFriend);
         	psmt.setInt(3, friendType);
        	count=psmt.executeUpdate();
		}catch(SQLException e) {
			e.printStackTrace();
		}
		return count;
	}
	public static boolean seekFriend(int senderID,int newFriendID,int friendType) {
		boolean seekSuccess=false;
		String userrelation_query_str="select * from userrelation where masteruserid=? and slaveuserid=? and relation=?";
		PreparedStatement psmt;
		try {
			psmt=conn.prepareStatement(userrelation_query_str);
			psmt.setInt(1, senderID);
			psmt.setInt(2, newFriendID);
			psmt.setInt(3, friendType);
			ResultSet rs=psmt.executeQuery();
			seekSuccess=rs.next();
		}catch(SQLException e) {
			e.printStackTrace();
		}
		return seekSuccess;
	}
	public static String seekAllFriend(int userID,int FriendType) {
		String allFriend="";
		String userrelation_query_str="select slaveuserID from userrelation where masteruserID=? and relation=?";
		PreparedStatement psmt;
		try {
			psmt=conn.prepareStatement(userrelation_query_str);
			psmt.setInt(1, userID);
			psmt.setInt(2, FriendType);
			ResultSet rs=psmt.executeQuery();
			while(rs.next()) 
				allFriend=allFriend+" "+searchUser(rs.getInt(1))+"#"+rs.getInt(1);
			System.out.println(userID+"全部好友"+allFriend);
		}catch(SQLException e) {
			e.printStackTrace();
		}
		return allFriend;
	}
	public static int insertIntoUser(User user) {
		int count=0;
		String user_insert_into_str="insert into user(username,password) values(?,?)";
		PreparedStatement psmt;
		try {
			psmt=conn.prepareStatement(user_insert_into_str);
        	psmt.setString(1, user.getUserName());
         	psmt.setString(2, user.getPassword());
        	count=psmt.executeUpdate();
		}catch(SQLException e) {
			e.printStackTrace();
		}
		return count;
	}
	
	public static Connection getConnection() {
		Connection conn=null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn=DriverManager.getConnection(db_url,db_username,db_password);
		}catch(ClassNotFoundException|SQLException e){
			e.printStackTrace();
		}
		return conn;
	}
	public static boolean loginValidate(User user) {
		boolean loginSuccess=false;
		String user_query_str="select * from user where id=? and password=?";
		PreparedStatement psmt;
		try {
			psmt=conn.prepareStatement(user_query_str);
        	psmt.setInt(1, user.getUserID());
        	psmt.setString(2, user.getPassword());
        	ResultSet rs=psmt.executeQuery();
        	loginSuccess=rs.next();
		}catch(SQLException e){
			e.printStackTrace();
		}
		return loginSuccess;
	}
	public static boolean seekUser(User user) {
		boolean seekSuccess=false;
		String user_query_str="select * from user where id=?";
		PreparedStatement psmt;
		try {
			psmt=conn.prepareStatement(user_query_str);
        	psmt.setInt(1, user.getUserID());
        	ResultSet rs=psmt.executeQuery();
        	seekSuccess=rs.next();
		}catch(SQLException e) {
			e.printStackTrace();
		}
		if(seekSuccess) {
			System.out.println("查找用户 - 用户"+user.getUserID()+"成功！");
		}else {
			System.out.println("查找用户出现问题！");
		}
		return seekSuccess;
	}
	public static User setUserInfo(int userID) {
		User user=new User();
		user.setUserID(userID);
		user.setUserType(UserType.USER_LOGIN_VALIDATE);
		PreparedStatement psmt;
		try {
			psmt=conn.prepareStatement("SELECT * FROM user WHERE id = ?;");
			psmt.setInt(1, userID);
			ResultSet rs=psmt.executeQuery();
			if (rs.next()) {
			    user.setUserName(rs.getString("username"));
			    user.setPassword(rs.getString("password"));
			    user.setEmail(rs.getString("email"));
			    user.setTelNumber(rs.getString("telnumber"));
			    System.out.println(user.toString());
			}
		}catch(SQLException e) {
			e.printStackTrace();
		}
		return user;
	}
	public static User setUserInfo(String userName) {
		User user=new User();
		user.setUserName(userName);
		user.setUserType(UserType.USER_LOGIN_VALIDATE);
		PreparedStatement psmt;
		try {
			psmt=conn.prepareStatement("SELECT * FROM user WHERE username = ?;");
			psmt.setString(1, userName);
			ResultSet rs=psmt.executeQuery();
			if (rs.next()) {
			    user.setUserID(rs.getInt("id"));
			    user.setPassword(rs.getString("password"));
			    user.setEmail(rs.getString("email"));
			    user.setTelNumber(rs.getString("telnumber"));
			    System.out.println(user.toString());
			}
		}catch(SQLException e) {
			e.printStackTrace();
		}
		return user;
	}
	public static String searchUser(int userID) {
		String userName = "";
		PreparedStatement psmt;
		try {
			psmt=conn.prepareStatement("SELECT username FROM user WHERE id = ?;");
			psmt.setInt(1, userID);
			ResultSet rs=psmt.executeQuery();
			if (rs.next()) {
			    userName = rs.getString("username");
			    System.out.println("Found username: " + userName + " for userID: " + userID);
			}
		}catch(SQLException e) {
			e.printStackTrace();
		}
		return userName;
	}
}
