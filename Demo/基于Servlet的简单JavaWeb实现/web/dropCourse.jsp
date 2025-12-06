<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.l4p.model.dao.EnrollmentDAO, com.l4p.model.pojo.Student" %>
<%@ page import="com.l4p.model.dao.EnrollmentDAO" %>
<%
    Student student = (Student) session.getAttribute("student");
    String courseIdStr = request.getParameter("courseId");
    if (courseIdStr != null) {
        int courseId = Integer.parseInt(courseIdStr);
        try {
            EnrollmentDAO enrollmentDAO = new EnrollmentDAO();
            enrollmentDAO.deleteByStudentAndCourse(student.getId(), courseId);
        } catch (Exception e) {
            throw new RuntimeException("退选失败", e);
        }
    }
    // 退选完毕，回到已选课程页
    response.sendRedirect("enrolledCourses.jsp");
%>
