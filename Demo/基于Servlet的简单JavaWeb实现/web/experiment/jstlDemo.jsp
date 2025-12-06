<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="zh">
<head><meta charset="UTF-8"><title>JSTL 核心标签演示</title></head>
<body>
<h2>二、JSTL 核心标签</h2>

<!-- c:set、c:out -->
<c:set var="msg" value="Hello, JSTL!" />
<p>c:out 输出：<c:out value="${msg}" /></p>

<!-- c:remove -->
<c:remove var="msg" scope="page" />
<p>移除 msg 后，empty 判定：${empty msg}</p>

<!-- c:if -->
<c:set var="num" value="8" />
<c:if test="${num % 2 == 0}">
  <p>${num} 是偶数</p>
</c:if>

<!-- c:choose/c:when/c:otherwise -->
<c:choose>
  <c:when test="${num > 10}">
    <p>数字大于 10</p>
  </c:when>
  <c:otherwise>
    <p>数字不大于 10</p>
  </c:otherwise>
</c:choose>

<!-- c:forEach -->
<c:set var="list" value="${{'苹果','香蕉','橘子'}}" />
<ul>
  <c:forEach var="item" items="${list}">
    <li><c:out value="${item}" /></li>
  </c:forEach>
</ul>

<!-- c:forTokens -->
<c:set var="names" value="Tom,Jack,Mary" />
<ul>
  <c:forTokens var="n" items="${names}" delims="," >
    <li>${n}</li>
  </c:forTokens>
</ul>

<!-- c:url + c:param -->
<c:url var="link" value="target.jsp">
  <c:param name="foo" value="bar" />
</c:url>
<p>动态 URL：<a href="${link}">${link}</a></p>

<!-- c:redirect -->
<!-- <c:redirect url="target.jsp?foo=bar" /> -->

<!-- c:import -->
<!-- <c:import url="footer.jsp" /> -->

</body>
</html>
