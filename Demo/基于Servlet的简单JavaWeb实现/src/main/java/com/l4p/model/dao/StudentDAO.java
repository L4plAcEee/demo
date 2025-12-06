package com.l4p.model.dao;

import com.l4p.model.pojo.Student;

import java.sql.*;
import java.util.*;
public class StudentDAO extends DBO {

    @Override
    public int add(Object o) throws Exception {
        Student s = (Student)o;
        open();
        PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO students(username,password,name) VALUES(?,?,?)",
                Statement.RETURN_GENERATED_KEYS
        );
        ps.setString(1, s.getUsername());
        ps.setString(2, s.getPassword());
        ps.setString(3, s.getName());
        int cnt = ps.executeUpdate();
        ps.close();
        close();
        return cnt;
    }

    @Override
    public int update(Object o) throws Exception { /* 略 */ return 0; }
    @Override
    public int delete(int id)      throws Exception {
        open();
        PreparedStatement ps = conn.prepareStatement("DELETE FROM students WHERE id=?");
        ps.setInt(1, id);
        int cnt = ps.executeUpdate();
        ps.close();
        close();
        return cnt;
    }

    @Override
    public Object find(int id)     throws Exception {
        open();
        PreparedStatement ps = conn.prepareStatement("SELECT * FROM students WHERE id=?");
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        Student s = null;
        if (rs.next()) {
            s = new Student();
            s.setId(rs.getInt("id"));
            s.setUsername(rs.getString("username"));
            s.setPassword(rs.getString("password"));
            s.setName(rs.getString("name"));
        }
        rs.close(); ps.close(); close();
        return s;
    }

    /** 根据用户名查找，用于登录验证 **/
    public Student findByUsername(String username) throws Exception {
        open();
        PreparedStatement ps = conn.prepareStatement("SELECT * FROM students WHERE username=?");
        ps.setString(1, username);
        ResultSet rs = ps.executeQuery();
        Student s = null;
        if (rs.next()) {
            s = new Student();
            s.setId(rs.getInt("id"));
            s.setUsername(username);
            s.setPassword(rs.getString("password"));
            s.setName(rs.getString("name"));
        }
        rs.close(); ps.close(); close();
        return s;
    }

    @Override
    public List<Student> findAll() throws Exception {
        open();
        List<Student> list = new ArrayList<>();
        ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM students");
        while (rs.next()) {
            Student s = new Student();
            s.setId(rs.getInt("id"));
            s.setUsername(rs.getString("username"));
            s.setPassword(rs.getString("password"));
            s.setName(rs.getString("name"));
            list.add(s);
        }
        rs.close(); close();
        return list;
    }
}