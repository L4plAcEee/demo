<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<nav class="navbar navbar-expand-lg navbar-light bg-white shadow-sm">
  <div class="container">
    <a class="navbar-brand" href="${pageContext.request.contextPath}/">首页</a>
    <div class="collapse navbar-collapse">
      <ul class="navbar-nav ms-auto">
        <li class="nav-item"><a class="nav-link" href="courseList.jsp">课程列表</a></li>
        <li class="nav-item"><a class="nav-link" href="enrolledCourses.jsp">已选课程</a></li>
        <li class="nav-item"><a class="nav-link" href="logout.jsp">退出</a></li>
      </ul>
    </div>
  </div>
</nav>
