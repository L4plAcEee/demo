package com.l4p.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class OllamaHelper {

    private final String baseUrl;

    public OllamaHelper() {
        this("http://localhost:11434");
    }

    public OllamaHelper(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /**
     * 非流式调用，返回完整响应
     */
    public String chat(String model, String prompt) throws Exception {
        URL url = new URL(baseUrl + "/api/generate");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        String requestBody = String.format("{\"model\":\"%s\",\"prompt\":\"%s\"}", model, prompt);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(requestBody.getBytes(StandardCharsets.UTF_8));
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }
        conn.disconnect();
        return response.toString();
    }

    /**
     * 流式调用，实时打印返回结果
     */
    public void chatStream(String model, String prompt) throws Exception {
        URL url = new URL(baseUrl + "/api/generate");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        String requestBody = String.format("{\"model\":\"%s\",\"prompt\":\"%s\",\"stream\":true}", model, prompt);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(requestBody.getBytes(StandardCharsets.UTF_8));
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    System.out.print(parseStreamChunk(line));
                }
            }
        }
        conn.disconnect();
    }

    /**
     * 解析 Ollama 流式返回的单行 JSON
     */
    private String parseStreamChunk(String jsonLine) {
        int start = jsonLine.indexOf("\"response\":\"") + 12;
        int end = jsonLine.indexOf("\"", start);
        if (start >= 12 && end > start) {
            return jsonLine.substring(start, end);
        }
        return "";
    }
}
