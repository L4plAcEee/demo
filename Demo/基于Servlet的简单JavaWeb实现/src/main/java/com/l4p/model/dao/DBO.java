package com.l4p.model.dao;

import java.sql.*;
public abstract class DBO {
    private static final String URL  = "jdbc:mysql://localhost:3306/course_system?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PWD  = "123456";

    protected Connection conn;
    protected void open() throws Exception {
        Class.forName("com.mysql.jdbc.Driver");
        conn = DriverManager.getConnection(URL, USER, PWD);
    }
    protected void close() {
        try { if (conn != null) conn.close(); } catch(Exception e) {}
    }

    // 通用 CRUD 抽象方法
    public abstract int     add(Object obj)      throws Exception;
    public abstract int     update(Object obj)   throws Exception;
    public abstract int     delete(int id)       throws Exception;
    public abstract Object  find(int id)         throws Exception;
    public abstract java.util.List<?> findAll()  throws Exception;
}