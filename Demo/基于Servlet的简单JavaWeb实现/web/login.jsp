<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.l4p.model.pojo.Student, com.l4p.model.dao.StudentDAO" %>
<%
    String errorMsg = null;
    if ("POST".equalsIgnoreCase(request.getMethod())) {
        String user = request.getParameter("username");
        String pwd  = request.getParameter("password");
        StudentDAO ado = new StudentDAO();
        Student   s   = null;
        try {
            s = ado.findByUsername(user);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (s != null && s.getPassword().equals(pwd)) {
            session.setAttribute("student", s);
            response.sendRedirect("courseList.jsp");
            return;
        } else {
            errorMsg = "用户名或密码错误";
        }
    }
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>用户登录</title>
    <!-- Bootstrap CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body {
            background-color: #f0f2f5;
        }
        .login-card {
            max-width: 400px;
            margin: 80px auto 20px; /* 调整底部间距 */
        }
        .login-header {
            text-align: center;
            margin-bottom: 30px;
            font-weight: bold;
            color: #333;
        }
        .register-link {
            text-align: center;
            margin-top: 10px;
            font-size: 0.9rem;
        }
        .register-link a {
            text-decoration: underline;
            color: #0d6efd;
        }
        .register-link a:hover {
            color: #084298;
        }
    </style>
</head>
<body>
<div class="container">
    <div class="card login-card shadow-sm">
        <div class="card-body">
            <h3 class="login-header">欢迎登录</h3>

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
                    <label for="password" class="form-label">密码</label>
                    <input type="password" class="form-control" id="password" name="password"
                           placeholder="请输入密码" required>
                </div>
                <button type="submit" class="btn btn-primary w-100">登 录</button>
            </form>
        </div>
    </div>

    <!-- 注册提示链接 -->
    <div class="register-link">
        <a href="register.jsp">前往注册</a>
    </div>
</div>

<jsp:include page="/fragments/footer.jsp" />
<!-- Bootstrap JS Bundle（含 Popper） -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
