package com.l4p.model.dao;

import com.l4p.model.pojo.Course;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class CoursesDAO extends DBO {
    @Override
    public int add(Object o) throws Exception { /* 略 */ return 0; }
    @Override
    public int update(Object o) throws Exception { /* 略 */ return 0; }
    @Override
    public int delete(int id) throws Exception {
        open();
        PreparedStatement ps = conn.prepareStatement("DELETE FROM courses WHERE id=?");
        ps.setInt(1, id);
        int cnt = ps.executeUpdate();
        ps.close(); close();
        return cnt;
    }
    @Override
    public Object find(int id) throws Exception {
        open();
        PreparedStatement ps = conn.prepareStatement("SELECT * FROM courses WHERE id=?");
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        Course c = null;
        if (rs.next()) {
            c = new Course();
            c.setId(id);
            c.setCourseName(rs.getString("course_name"));
            c.setDescription(rs.getString("description"));
            c.setCredits(rs.getInt("credits"));
        }
        rs.close(); ps.close(); close();
        return c;
    }
    @Override
    public List<Course> findAll() throws Exception {
        open();
        List<Course> list = new ArrayList<>();
        ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM courses");
        while (rs.next()) {
            Course c = new Course();
            c.setId(rs.getInt("id"));
            c.setCourseName(rs.getString("course_name"));
            c.setDescription(rs.getString("description"));
            c.setCredits(rs.getInt("credits"));
            list.add(c);
        }
        rs.close(); close();
        return list;
    }
}