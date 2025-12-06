package com.l4p.servlet;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.l4p.model.pojo.TodoItem;

import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;

public class TodoServletTest {

    private static final String BASE_URL = "http://localhost:8080/api/todos";
    private static ObjectMapper objectMapper;

    @BeforeAll
    public static void setup() {
        objectMapper = new ObjectMapper();
    }

    @Test
    public void testCreateGetDeleteTodo() throws Exception {
        // 1. 创建新的 2do
        TodoItem newTodo = new TodoItem();
        newTodo.setDescription("Test item");
        newTodo.setPriority(2);
        newTodo.setIsCompleted(false);

        String newTodoJson = objectMapper.writeValueAsString(newTodo);
        HttpURLConnection postConn = (HttpURLConnection) new URL(BASE_URL).openConnection();
        postConn.setRequestMethod("POST");
        postConn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        postConn.setDoOutput(true);
        try (OutputStream os = postConn.getOutputStream()) {
            os.write(newTodoJson.getBytes("UTF-8"));
        }
        assertEquals(201, postConn.getResponseCode(), "Expected HTTP 201 Created");
        String location = postConn.getHeaderField("Location");
        assertNotNull(location, "Location header should be set");

        // Extract created ID
        String[] parts = location.split("/");
        int createdId = Integer.parseInt(parts[parts.length - 1]);

        // 2. 获取单个 2do
        HttpURLConnection getConn = (HttpURLConnection) new URL(location).openConnection();
        getConn.setRequestMethod("GET");
        assertEquals(200, getConn.getResponseCode(), "Expected HTTP 200 OK");
        TodoItem fetched = objectMapper.readValue(getConn.getInputStream(), TodoItem.class);
        assertEquals(createdId, fetched.getId());
        assertEquals("Test item", fetched.getDescription());

        // 3. 删除 2do
        HttpURLConnection delConn = (HttpURLConnection) new URL(location).openConnection();
        delConn.setRequestMethod("DELETE");
        assertEquals(204, delConn.getResponseCode(), "Expected HTTP 204 No Content");

        // 4. 验证删除
        HttpURLConnection getAfterDel = (HttpURLConnection) new URL(location).openConnection();
        getAfterDel.setRequestMethod("GET");
        assertEquals(404, getAfterDel.getResponseCode(), "Expected HTTP 404 Not Found after deletion");
    }

    @Test
    public void testGetAllTodos() throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(BASE_URL).openConnection();
        conn.setRequestMethod("GET");
        int code = conn.getResponseCode();
        assertTrue(code == 200 || code == 204, "Expected HTTP 200 or 204");
        if (code == 200) {
            TodoItem[] todos = objectMapper.readValue(conn.getInputStream(), TodoItem[].class);
            assertNotNull(todos, "Response should contain an array of TodoItem");
        }
    }
}
