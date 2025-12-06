<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    // 释放会话，注销用户
    session.invalidate();
    // 设置页面标题，供 header.jsp 使用
    request.setAttribute("pageTitle", "已退出登录");
%>
<jsp:include page="/fragments/header.jsp"/>

<div class="container my-5">
    <div class="card mx-auto" style="max-width: 400px;">
        <div class="card-body text-center">
            <h4 class="card-title mb-4">退出成功</h4>
            <p class="card-text">您已安全退出系统。</p>
            <a href="login.jsp" class="btn btn-primary w-100">重新登录</a>
        </div>
    </div>
</div>

<jsp:include page="/fragments/footer.jsp"/>
