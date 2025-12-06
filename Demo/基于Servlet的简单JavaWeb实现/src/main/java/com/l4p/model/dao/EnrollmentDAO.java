package com.l4p.model.dao;

import com.l4p.model.pojo.Enrollment;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * 专门用于查询学生 enrollments 表的 DAO
 */
public class EnrollmentDAO extends DBO {

    @Override
    public int add(Object obj) throws Exception {
        throw new UnsupportedOperationException("不支持的操作");
    }

    @Override
    public int update(Object obj) throws Exception {
        throw new UnsupportedOperationException("不支持的操作");
    }

    @Override
    public int delete(int id) throws Exception {
        throw new UnsupportedOperationException("不支持的操作");
    }

    @Override
    public Object find(int id) throws Exception {
        throw new UnsupportedOperationException("不支持的操作");
    }

    @Override
    public List<?> findAll() throws Exception {
        throw new UnsupportedOperationException("不支持的操作");
    }

    /**
     * 给学生选课（如果已选过，则不再插入）
     * @param studentId 学生ID
     * @param courseId  课程ID
     * @return true: 成功插入 / false: 已存在，不插入
     * @throws Exception
     */
    public boolean enrollCourse(int studentId, int courseId) throws Exception {
        open();

        // 1. 先查询是否已存在该学生选了该课程
        String checkSql = "SELECT COUNT(*) FROM enrollments WHERE student_id = ? AND course_id = ?";
        PreparedStatement checkPs = conn.prepareStatement(checkSql);
        checkPs.setInt(1, studentId);
        checkPs.setInt(2, courseId);
        ResultSet rs = checkPs.executeQuery();

        boolean exists = false;
        if (rs.next()) {
            exists = rs.getInt(1) > 0;
        }
        rs.close();
        checkPs.close();

        if (exists) {
            close();
            return false; // 已存在，不插入
        }

        // 2. 不存在就插入
        String insertSql = "INSERT INTO enrollments (student_id, course_id, enroll_date) VALUES (?, ?, NOW())";
        PreparedStatement insertPs = conn.prepareStatement(insertSql);
        insertPs.setInt(1, studentId);
        insertPs.setInt(2, courseId);
        insertPs.executeUpdate();
        insertPs.close();

        close();
        return true; // 插入成功
    }

    /**
     * 查询指定学生在 enrollments 表中的所有记录（只映射 id, student_id, course_id, enroll_date）
     */
    public List<Enrollment> findByStudentId(int studentId) throws Exception {
        List<Enrollment> list = new ArrayList<>();
        open();
        String sql = "SELECT id, student_id, course_id, enroll_date "
                + "FROM enrollments WHERE student_id = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, studentId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            Enrollment e = new Enrollment();
            e.setId(rs.getInt("id"));
            e.setStudentId(rs.getInt("student_id"));
            e.setCourseId(rs.getInt("course_id"));
            e.setEnrollDate(rs.getTimestamp("enroll_date").toLocalDateTime());
            list.add(e);
        }
        rs.close();
        ps.close();
        close();
        return list;
    }

    /**
     * 删除指定学生选的指定课程
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @throws Exception
     */
    public void deleteByStudentAndCourse(int studentId, int courseId) throws Exception {
        open(); // 打开数据库连接
        String sql = "DELETE FROM enrollments WHERE student_id = ? AND course_id = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, studentId);
        ps.setInt(2, courseId);
        ps.executeUpdate(); // 执行删除
        ps.close();
        close(); // 关闭数据库连接
    }
}
