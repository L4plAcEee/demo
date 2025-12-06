<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.l4p.model.pojo.Student, com.l4p.model.dao.StudentDAO" %>
<%
  String errorMsg = null;
  if ("POST".equalsIgnoreCase(request.getMethod())) {
    String username = request.getParameter("username");
    String realname = request.getParameter("realname");
    String password = request.getParameter("password");

    if (username == null || username.isEmpty() || password == null || password.isEmpty() || realname == null || realname.isEmpty()) {
      errorMsg = "用户名和密码不能为空";
    } else {
      StudentDAO ado = new StudentDAO();
      try {
        Student existing = ado.findByUsername(username);
        if (existing != null) {
          errorMsg = "用户名已存在，请选择其他用户名";
        } else {
          Student newStudent = new Student();
          newStudent.setUsername(username);
          newStudent.setPassword(password);
          newStudent.setName(realname);
          ado.add(newStudent);
          response.sendRedirect("login.jsp"); // 注册成功跳回登录
          return;
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>用户注册</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
  <style>
    body {
      background-color: #f0f2f5;
    }
    .register-card {
      max-width: 400px;
      margin: 80px auto 20px;
    }
    .register-header {
      text-align: center;
      margin-bottom: 30px;
      font-weight: bold;
      color: #333;
    }
    .login-link {
      text-align: center;
      margin-top: 10px;
      font-size: 0.9rem;
    }
    .login-link a {
      text-decoration: underline;
      color: #0d6efd;
    }
    .login-link a:hover {
      color: #084298;
    }
  </style>
</head>
<body>
<div class="container">
  <div class="card register-card shadow-sm">
    <div class="card-body">
      <h3 class="register-header">用户注册</h3>

      <% if (errorMsg != null) { %>
      <div class="alert alert-danger" role="alert">
        <%= errorMsg %>
      </div>
      <% } %>

      <form method="post" novalidate>
        <div class="mb-3">
          <label for="username" class="form-label">用户名</label>
          <input type="text" class="form-control" id="username" name="username"
                 placeholder="请输入用户名" required>
        </div>
        <div class="mb-3">
          <label for="realname" class="form-label">姓名</label>
          <input type="text" class="form-control" id="realname" name="realname"
                 placeholder="请输入真实姓名" required>
        </div>
        <div class="mb-3">
          <label for="password" class="form-label">密码</label>
          <input type="password" class="form-control" id="password" name="password"
                 placeholder="请输入密码" required>
        </div>
        <button type="submit" class="btn btn-success w-100">注 册</button>
      </form>
    </div>
  </div>

  <!-- 登录提示链接 -->
  <div class="login-link">
    <a href="login.jsp">已有账号？去登录</a>
  </div>
</div>

<jsp:include page="/fragments/footer.jsp"/>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
