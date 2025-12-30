<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.l4p.model.pojo.Student, com.l4p.model.dao.EnrollmentDAO" %>
<%@ page import="java.sql.PreparedStatement" %>
<%@ page import="com.l4p.model.dao.EnrollmentDAO" %>
<%
  // 验证登录
  Student s = (Student) session.getAttribute("student");
  // 获取参数并尝试选课
  int courseId = Integer.parseInt(request.getParameter("courseId"));
  String message;
  EnrollmentDAO enrollmentsADO = new EnrollmentDAO();
    boolean success = false;
    try {
        success = enrollmentsADO.enrollCourse(s.getId(), courseId);
    } catch (Exception e) {
        throw new RuntimeException(e);
    }
    if (success) {
    // 插入成功
    System.out.println("选课成功！");
    message = "选课成功！";
    } else {
      // 已存在，未插入
      System.out.println("该学生已经选过此课程，跳过插入。");
      message = "选课失败：该学生已经选过此课程。";
    }
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>选课结果</title>
  <!-- 引入 Bootstrap 5 -->
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
  <style>
    body {
      background-color: #f8f9fa;
    }
    .result-card {
      max-width: 500px;
      margin: 100px auto;
    }
    .result-header {
      text-align: center;
      margin-bottom: 20px;
      font-weight: 600;
      color: #333;
    }
  </style>
</head>
<body>
<jsp:include page="/fragments/header.jsp"/>
<div class="container">
  <div class="card result-card shadow-sm">
    <div class="card-body">
      <h4 class="result-header">选课提示</h4>

      <% if (success) { %>
      <div class="alert alert-success" role="alert">
        <%= message %>
      </div>
      <% } else { %>
      <div class="alert alert-danger" role="alert">
        <%= message %>
      </div>
      <% } %>

      <div class="d-grid gap-2 mt-3">
        <a href="courseList.jsp" class="btn btn-primary">返回课程列表</a>
      </div>
    </div>
  </div>
</div>

<jsp:include page="/fragments/footer.jsp" />
<!-- Bootstrap JS Bundle（含 Popper） -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
