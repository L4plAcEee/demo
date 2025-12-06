package com.l4p.api;


import com.l4p.model.pojo.Item;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@WebServlet("/api/items/*") // 匹配 /api/items/ 和 /api/items/xxx
public class ItemServlet extends HttpServlet {

    // 使用线程安全的Map作为内存中的数据存储
    private final Map<Long, Item> itemStore = new ConcurrentHashMap<>();
    // 用于生成自增ID
    private final AtomicLong idCounter = new AtomicLong();
    // Jackson ObjectMapper 用于JSON转换 (线程安全，可以复用)
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void init() throws ServletException {
        // (可选) 注册Java 8 Date/Time模块
        // objectMapper.registerModule(new JavaTimeModule());
        // objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 初始化一些数据 (仅为示例)
        long id1 = idCounter.incrementAndGet();
        itemStore.put(id1, new Item(id1, "Initial Item 1"));
        long id2 = idCounter.incrementAndGet();
        itemStore.put(id2, new Item(id2, "Initial Item 2"));
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo(); // 获取 URL 中 /api/items 后面的部分

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                // --- GET /api/items --- 获取所有 items
                Collection<Item> items = itemStore.values();
                sendJsonResponse(resp, HttpServletResponse.SC_OK, items);

            } else {
                // --- GET /api/items/{id} --- 获取单个 item
                // 解析 ID (简单的路径分割)
                String[] pathParts = pathInfo.split("/");
                if (pathParts.length == 2) { // 期望路径是 "/{id}"
                    long id = Long.parseLong(pathParts[1]);
                    Optional<Item> itemOptional = Optional.ofNullable(itemStore.get(id));
                    // 使用 Java 8 Optional 处理
                    if (itemOptional.isPresent()) {
                        itemOptional.ifPresent(item -> { // ifPresent 是 Java 8 的方法
                            try {
                                sendJsonResponse(resp, HttpServletResponse.SC_OK, item);
                            } catch (IOException e) {
                                handleException(resp, "Error writing JSON response", e);
                            }
                        });
                    } else {
                        sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Item not found");
                    }
                } else {
                    sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid URL path format for getting an item.");
                }
            }
        } catch (NumberFormatException e) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid item ID format.");
        } catch (Exception e) {
            handleException(resp, "Error processing GET request", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();

        // 只允许 POST 到 /api/items 集合
        if (pathInfo == null || pathInfo.equals("/")) {
            try {
                // 从请求体中读取 JSON 并反序列化为 Item 对象
                // 注意：这里假设客户端发送的 Item 没有 id 字段，或者会忽略它
                String requestBody = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
                Item newItemRequest = objectMapper.readValue(requestBody, Item.class);

                if (newItemRequest.getName() == null || newItemRequest.getName().trim().isEmpty()) {
                    sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Item name cannot be empty.");
                    return;
                }

                // 创建新的 Item 并分配 ID
                long newId = idCounter.incrementAndGet();
                Item createdItem = new Item(newId, newItemRequest.getName());
                itemStore.put(newId, createdItem);

                // 设置 Location 头部
                String location = req.getRequestURL().append("/").append(newId).toString();
                resp.setHeader("Location", location);

                // 返回 201 Created 和创建的 Item
                sendJsonResponse(resp, HttpServletResponse.SC_CREATED, createdItem);

            } catch (IOException e) { // 包括 JsonProcessingException
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON format or error reading request body.");
            } catch (Exception e) {
                handleException(resp, "Error processing POST request", e);
            }
        } else {
            // 不允许 POST 到特定 ID，返回 405 Method Not Allowed
            resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "POST method is not allowed on individual items.");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Missing item ID for DELETE request.");
            return;
        }

        try {
            // 解析 ID
            String[] pathParts = pathInfo.split("/");
            if (pathParts.length == 2) {
                long id = Long.parseLong(pathParts[1]);
                // 尝试删除
                Item removedItem = itemStore.remove(id);

                if (removedItem != null) {
                    // 成功删除，返回 204 No Content
                    resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
                } else {
                    // 未找到要删除的资源
                    sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Item not found for deletion.");
                }
            } else {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid URL path format for deleting an item.");
            }
        } catch (NumberFormatException e) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid item ID format.");
        } catch (Exception e) {
            handleException(resp, "Error processing DELETE request", e);
        }
    }

    // --- Helper Methods ---

    /**
     * 将对象序列化为 JSON 并发送响应
     */
    private void sendJsonResponse(HttpServletResponse resp, int status, Object data) throws IOException {
        resp.setContentType("application/json; charset=UTF-8");
        resp.setStatus(status);
        resp.getWriter().write(objectMapper.writeValueAsString(data));
    }

    /**
     * 发送错误响应 (简单的文本)
     */
    private void sendError(HttpServletResponse resp, int status, String message) {
        try {
            // 也可以选择发送 JSON 格式的错误信息
            // resp.setContentType("application/json; charset=UTF-8");
            // resp.getWriter().write(objectMapper.writeValueAsString(Map.of("error", message)));
            resp.sendError(status, message);
        } catch (IOException e) {
            // Log the error, as sending the error response itself failed
            log("Error sending error response: " + e.getMessage(), e);
        }
    }

    /**
     * 处理未预料到的异常
     */
    private void handleException(HttpServletResponse resp, String logMessage, Exception e) {
        log(logMessage, e); // 使用 Servlet 的 log 方法记录异常
        sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An internal server error occurred.");
    }
}
