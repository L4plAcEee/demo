package com.l4p.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.l4p.model.pojo.TodoItem;
import com.l4p.model.dao.TodoDAO;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@WebServlet("/api/todos/*")  // 匹配 /api/todos/ 和 /api/todos/{id}
public class TodoServlet extends HttpServlet {

    private final TodoDAO todoDAO = new TodoDAO();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();  // "/{id}" or null

        try {
            if (pathInfo == null || "/".equals(pathInfo)) {
                // GET /api/todos
                List<TodoItem> todos = todoDAO.findAll();
                sendJson(resp, HttpServletResponse.SC_OK, todos);

            } else {
                // GET /api/todos/{id}
                String[] parts = pathInfo.split("/");
                if (parts.length == 2) {
                    long id = Long.parseLong(parts[1]);
                    Optional<TodoItem> opt = Optional.ofNullable(todoDAO.find((int) id));
                    if (opt.isPresent()) {
                        sendJson(resp, HttpServletResponse.SC_OK, opt.get());
                    } else {
                        sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Todo not found");
                    }
                } else {
                    sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid URL format");
                }
            }
        } catch (NumberFormatException e) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid ID format");
        } catch (Exception e) {
            log("Error processing GET", e);
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();

        if (pathInfo == null || "/".equals(pathInfo)) {
            try {
                String body = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
                TodoItem newTodo = objectMapper.readValue(body, TodoItem.class);

                if (newTodo.getDescription() == null || newTodo.getDescription().trim().isEmpty()) {
                    sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Description cannot be empty");
                    return;
                }

                int result = todoDAO.add(newTodo);
                if (result > 0) {
                    // location header with newly generated ID (assumes add() sets newTodo.id or you fetch it back)
                    String location = req.getRequestURL().append("/").append(newTodo.getId()).toString();
                    resp.setHeader("Location", location);
                    sendJson(resp, HttpServletResponse.SC_CREATED, newTodo);
                } else {
                    sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to create todo");
                }

            } catch (IOException e) {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Malformed JSON");
            } catch (Exception e) {
                log("Error processing POST", e);
                sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
            }
        } else {
            sendError(resp, HttpServletResponse.SC_METHOD_NOT_ALLOWED, "POST not allowed on individual resource");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();

        if (pathInfo == null || "/".equals(pathInfo)) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Missing todo ID");
            return;
        }

        try {
            String[] parts = pathInfo.split("/");
            if (parts.length == 2) {
                long id = Long.parseLong(parts[1]);
                int result = todoDAO.delete((int) id);
                if (result > 0) {
                    resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
                } else {
                    sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Todo not found");
                }
            } else {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid URL format");
            }
        } catch (NumberFormatException e) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid ID format");
        } catch (Exception e) {
            log("Error processing DELETE", e);
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
        }
    }

    // Utility methods
    private void sendJson(HttpServletResponse resp, int status, Object obj) throws IOException {
        resp.setContentType("application/json; charset=UTF-8");
        resp.setStatus(status);
        resp.getWriter().write(objectMapper.writeValueAsString(obj));
    }

    private void sendError(HttpServletResponse resp, int status, String msg) throws IOException {
        resp.sendError(status, msg);
    }
}
