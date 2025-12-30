package com.yychat.model;
import java.io.Serializable;
public class User implements Serializable {
	private static final long serialVersionUID = 1L;
	int userID;
	String userName;
	String password;
	String email;
	String telNumber;
	String userType;
	
	public boolean isEqual(User otherUser) {
        // 检查userName和userID是否相等
        return this.userName.equals(otherUser.userName) && this.userID == otherUser.userID;
}
	public String getUserType() {
		return userType;
	}
	public void setUserType(String userType) {
		this.userType=userType;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName (String userName) {
			this.userName=userName;
	}
	public String getPassword() {
			return password;
	}
	public void setPassword (String password){
			this.password = password;
	}
	public String getEmail() {

		return email;		
	}
	public void setEmail(String email) {
		this.email=email;
	}
	public String getTelNubmer() {
		return telNumber;
	}
	public void setTelNumber(String telNumber) {
		this.telNumber=telNumber;
	}
	public int getUserID() {
		return userID;
	}
	public void setUserID(int userID) {
		this.userID=userID;
	}
	public String toString() {
		String str="ID:"+userID+"名字："+userName+"密码："+password+"email："+email+"telNumbel："+telNumber+"";
		return str;
	}
}