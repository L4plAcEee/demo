<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="
    java.util.List,
    com.l4p.model.pojo.Enrollment,
    com.l4p.model.dao.EnrollmentDAO,
    com.l4p.model.pojo.Course,
    com.l4p.model.dao.CoursesDAO,
    com.l4p.model.pojo.Student
" %>
<%@ page import="com.l4p.model.dao.EnrollmentDAO" %>
<%

  Student student = (Student) session.getAttribute("student");
  List<Enrollment> enrollList;
  try {
    enrollList = new EnrollmentDAO().findByStudentId(student.getId());
  } catch (Exception ex) {
    throw new RuntimeException("查询已选课程记录失败", ex);
  }

  CoursesDAO courseDao = new CoursesDAO();
  request.setAttribute("pageTitle", "我的已选课程");
%>

<jsp:include page="fragments/header.jsp"/>

<div class="container my-5">
  <h2 class="text-center mb-4">我的已选课程</h2>
  <div class="table-responsive">
    <table class="table table-bordered table-striped table-hover align-middle">
      <thead class="table-dark">
      <tr>
        <th>课程ID</th>
        <th>课程名称</th>
        <th>学分</th>
        <th>选课时间</th>
        <th>操作</th>  <!-- 新加操作栏 -->
      </tr>
      </thead>
      <tbody>
      <% if (enrollList.isEmpty()) { %>
      <tr>
        <td colspan="5" class="text-center">暂无已选课程</td> <!-- 修改为5列 -->
      </tr>
      <% } else {
        for (Enrollment en : enrollList) {
          Course c = null;
          try {
            c = (Course) courseDao.find(en.getCourseId());
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
      %>
      <tr>
        <td><%= c.getId() %></td>
        <td><%= c.getCourseName() %></td>
        <td><%= c.getCredits() %></td>
        <td>
          <%
            if (en.getEnrollDate() != null) {
              out.print(
                      java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                              .format(en.getEnrollDate())
              );
            } else {
              out.print("-");
            }
          %>
        </td>
        <td>
          <!-- 退选按钮 -->
          <form action="dropCourse.jsp" method="post" style="display:inline;">
            <input type="hidden" name="courseId" value="<%= c.getId() %>">
            <button type="submit" class="btn btn-danger btn-sm"
                    onclick="return confirm('确认退选《<%= c.getCourseName() %>》吗？');">
              退选
            </button>
          </form>
        </td>
      </tr>
      <%  }
      } %>
      </tbody>
    </table>
  </div>
  <div class="d-flex justify-content-end mt-3">
    <a href="courseList.jsp" class="btn btn-secondary">返回课程列表</a>
  </div>
</div>

<jsp:include page="/fragments/footer.jsp"/>
