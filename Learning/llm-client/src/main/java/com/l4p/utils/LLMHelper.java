package com.l4p.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LLMHelper {

    private final String apiUrl;
    private final String apiKey;
    private final ObjectMapper objectMapper;
    private final List<Message> history = new ArrayList<>(); // 会话历史

    public LLMHelper(String apiUrl, String apiKey) {
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        // 初始化系统指令
        history.add(new Message("system", "You are a helpful assistant."));
    }

    /**
     * 发送一条用户消息并返回助手回复，同时追加到会话历史
     */
    public String chat(String model, String userPrompt) {
        history.add(new Message("user", userPrompt));
        String assistantReply = sendRequest(model);
        history.add(new Message("assistant", assistantReply));
        return assistantReply;
    }

    /**
     * 构建并发送 HTTP 请求，返回完整回复文本
     */
    private String sendRequest(String model) {
        try {
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            if (apiKey != null) {
                conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            }
            conn.setDoOutput(true);

            String requestBody = buildRequestBody(model);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(requestBody.getBytes(StandardCharsets.UTF_8));
            }

            if (conn.getResponseCode() == 200) {
                return parseStreamResponse(conn.getInputStream());
            } else {
                System.err.println("调用失败，状态码: " + conn.getResponseCode());
                return "";
            }
        } catch (Exception e) {
            System.err.println("请求异常: " + e.getMessage());
            return "";
        }
    }

    /**
     * 构建请求体，包含完整对话历史或单条Prompt
     */
    private String buildRequestBody(String model) throws Exception {
        if (apiKey == null) {
            // 本地 LLM：将历史拼接为单个prompt
            StringBuilder combined = new StringBuilder();
            for (Message m : history) {
                combined.append(m.role).append(": ").append(m.content).append("\n");
            }
            ChatRequest req = new ChatRequest(model, combined.toString());
            return objectMapper.writeValueAsString(req);
        } else {
            // 远程 LLM：完整 history
            Map<String, Object> request = new HashMap<>();
            request.put("model", model);
            request.put("stream", true);
            List<Map<String, String>> msgs = new ArrayList<>();
            for (Message m : history) {
                Map<String, String> map = new HashMap<>();
                map.put("role", m.role);
                map.put("content", m.content);
                msgs.add(map);
            }
            request.put("messages", msgs);
            return objectMapper.writeValueAsString(request);
        }
    }

    /**
     * 统一解析流式SSE或本地JSON行
     */
    private String parseStreamResponse(InputStream inputStream) throws Exception {
        StringBuilder result = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty()) continue;
            if (line.startsWith("data:")) {
                String jsonPart = line.substring(5).trim();
                if ("[DONE]".equals(jsonPart)) break;
                JsonNode root = objectMapper.readTree(jsonPart);
                JsonNode choices = root.path("choices");
                if (choices.isArray() && choices.size() > 0) {
                    JsonNode delta = choices.get(0).path("delta");
                    if (delta.has("content")) {
                        result.append(delta.get("content").asText());
                    }
                }
            } else {
                JsonNode root = objectMapper.readTree(line);
                if (root.has("response")) {
                    result.append(root.get("response").asText());
                }
            }
        }
        return result.toString();
    }

    // 本地请求体类
    public static class ChatRequest {
        public String model;
        public String prompt;
        public ChatRequest(String model, String prompt) {
            this.model = model;
            this.prompt = prompt;
        }
    }

    // 对话消息类
    public static class Message {
        public final String role;
        public final String content;
        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }

    /**
     * 示例：同时测试本地和远程多轮对话
     */
    public static void main(String[] args) {
        // 本地 LLM
        LLMHelper local = new LLMHelper("http://localhost:11434/api/generate", null);
        System.out.println("Local第一轮: " + local.chat("llama3.2", "你好，助手。"));
        System.out.println("Local第二轮: " + local.chat("llama3.2", "上一条你说了什么？"));
    }
}
