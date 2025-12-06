package com.yychat.manager;

import java.io.*;

public class LoginManager {
	private static String PATH="pwd.txt";
	private String locUserName;
	private String locUserPwd;
	
    public LoginManager() {// 初始化登录凭证
    	loadFile();
    }
    public void loadFile() {// 从文件加载凭证
        try (BufferedReader reader = new BufferedReader(new FileReader(PATH))) {
            String line;
            String[] parts=new String[1];
            while ((line = reader.readLine()) != null) {
            	parts = line.split(":");
            }
            locUserName=parts[0];
        	locUserPwd=parts[1];
            
        }catch (IOException e) {
            System.err.println("无法加载凭证文件: " + e.getMessage());
        }catch(ArrayIndexOutOfBoundsException e){
        	locUserName="";
        	locUserPwd="";
        }
    }

    static public void saveToFile(int userID,String pwd) {// 将凭证保存到文件
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(PATH))) {
                writer.write(userID+":"+pwd);
                writer.newLine();
            
        } catch (IOException e) {
            System.err.println("无法保存凭证文件: " + e.getMessage());
        }
    }
    static public void saveToFile() {
    	try (BufferedWriter writer = new BufferedWriter(new FileWriter(PATH))) {
            writer.write(":");
            writer.newLine();
        
    } catch (IOException e) {
        System.err.println("无法保存凭证文件: " + e.getMessage());
    }
    }
	public String getLocUserPwd() {
		return locUserPwd;
	}

	public String getLocUserName() {
		return locUserName;
	}
}
