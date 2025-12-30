<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
  // 在各个域中预置几个属性，方便 EL 演示
  pageContext.setAttribute   ("pageAttr",    "这是 pageScope 属性");
  request.setAttribute       ("reqAttr",     "这是 requestScope 属性");
  session.setAttribute       ("sessAttr",    "这是 sessionScope 属性");
  application.setAttribute   ("appAttr",     "这是 applicationScope 属性");
  // 添加一个 Cookie
  javax.servlet.http.Cookie ck = new javax.servlet.http.Cookie("userCookie", "cookieValue");
  response.addCookie(ck);
%>

<!DOCTYPE html>
<html lang="zh">
<head><meta charset="UTF-8"><title>EL 内置对象演示</title></head>
<body>
<h2>一、EL 内置对象</h2>

<!-- pageContext -->
<p>pageContext 对象：${pageContext}</p>

<!-- 四个 Scope Map -->
<p>pageScope.pageAttr：${pageScope.pageAttr}</p>
<p>requestScope.reqAttr：${requestScope.reqAttr}</p>
<p>sessionScope.sessAttr：${sessionScope.sessAttr}</p>
<p>applicationScope.appAttr：${applicationScope.appAttr}</p>

<!-- 请求参数和参数数组 -->
<form action="elDemo.jsp" method="get">
  用户名：<input name="username" value="${param.username}" /><br/>
  选项（可多选）：<br/>
  <input type="checkbox" name="choice" value="A" /> A
  <input type="checkbox" name="choice" value="B" /> B
  <input type="checkbox" name="choice" value="C" /> C<br/>
  <button type="submit">提交</button>
</form>
<p>param.username：${param.username}</p>
<p>paramValues.choice：[${paramValues.choice[0]} , ${paramValues.choice[1]}]</p>

<!-- Header 和 HeaderValues -->
<p>header['User-Agent']：${header['User-Agent']}</p>
<p>headerValues['Accept']：${headerValues['Accept'][0]}</p>

<!-- Cookie -->
<p>cookie.userCookie.value：${cookie.userCookie.value}</p>

<!-- 初始化参数 -->
<p>initParam.siteName：${initParam.siteName}</p>

<!-- 运算和逻辑 -->
<p>算术：2 + 3 = ${2 + 3}</p>

<!-- 直接用原生符号 -->
<p>逻辑：${5 > 3 && 2 < 4}</p>

<!-- 或者，使用 EL 关键字 -->
<p>逻辑（关键字写法）：${5 gt 3 and 2 lt 4}</p>

<p>三目：${empty param.username ? '未填写用户名' : '欢迎，' + param.username}</p>

</body>
</html>
