<%@ page isErrorPage="true"
         language="java"
         contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>

<!DOCTYPE html>
<html lang="zh">
<head>
    <meta charset="UTF-8">
    <title>发生错误</title>
</head>
<body>
<h1>系统错误</h1>
<p>异常类型：<%= exception.getClass().getName() %></p>
<p>异常信息：<%= exception.getMessage() %></p>
<hr/>
<p><a href="../index.jsp">返回首页</a></p>
</body>
</html>
