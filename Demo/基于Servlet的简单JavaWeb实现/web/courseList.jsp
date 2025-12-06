<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, com.l4p.model.pojo.Course, com.l4p.model.dao.CoursesDAO, com.l4p.model.pojo.Student" %>
<%
  // 验证登录
  Student student = (Student) session.getAttribute("student");
  // 查询所有可选课程
  CoursesDAO ado = new CoursesDAO();
  List<Course> list = null;
  try {
    list = ado.findAll();
  } catch (Exception e) {
    throw new RuntimeException(e);
  }
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>可选课程列表</title>
  <!-- Bootstrap 5 CSS -->
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
  <style>
    body { background-color: #f0f2f5; }
    .course-card { max-width: 800px; margin: 60px auto; }
    .course-header { text-align: center; margin-bottom: 30px; font-weight: 600; color: #333; }
  </style>
</head>
<body>
<jsp:include page="/fragments/header.jsp"/>
<div class="container">
  <div class="card course-card shadow-sm">
    <div class="card-body">
      <h2 class="course-header">可选课程</h2>

      <div class="table-responsive">
        <table class="table table-bordered table-striped table-hover align-middle">
          <thead class="table-dark">
          <tr>
            <th scope="col">ID</th>
            <th scope="col">课程名称</th>
            <th scope="col">学分</th>
            <th scope="col">操作</th>
          </tr>
          </thead>
          <tbody>
          <% if (list == null || list.isEmpty()) { %>
          <tr>
            <td colspan="4" class="text-center">暂无可选课程</td>
          </tr>
          <% } else {
            for (Course c : list) {
          %>
          <tr>
            <td><%= c.getId() %></td>
            <td><%= c.getCourseName() %></td>
            <td><%= c.getCredits() %></td>
            <td>
              <a href="selectCourse.jsp?courseId=<%= c.getId() %>"
                 class="btn btn-sm btn-primary">
                选 课
              </a>
            </td>
          </tr>
          <%  }
          } %>
          </tbody>
        </table>
      </div>

      <div class="text-end mt-3">
        <span class="me-3">当前用户：<strong><%= student.getUsername() %></strong></span>
        <a href="logout.jsp" class="btn btn-outline-secondary btn-sm">退出登录</a>
      </div>
    </div>
  </div>
</div>

<jsp:include page="/fragments/footer.jsp" />
<!-- Bootstrap 5 JS Bundle（含 Popper）-->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
