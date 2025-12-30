package com.l4p.utils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class KimiLLMHelper {

    private static final String KIMI_API_URL = "https://api.moonshot.cn/v1/chat/completions"; // 可替换为本地地址
    private static final String API_KEY = ""; // 请填写你的 API KEY

    /**
     * 调用 Kimi API，返回结果文本
     */
    public String chat(String model, String prompt, double temperature) {
        HttpURLConnection connection = null;
        try {
            // 组装请求体
            String requestBody = buildRequestBody(model, prompt, temperature);

            // 构造请求
            URL url = new URL(KIMI_API_URL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + API_KEY);

            // 发送请求
            try (OutputStream os = connection.getOutputStream()) {
                os.write(requestBody.getBytes("utf-8"));
            }

            // 读取响应
            InputStream is = connection.getResponseCode() < HttpURLConnection.HTTP_BAD_REQUEST
                    ? connection.getInputStream() : connection.getErrorStream();

            StringBuilder responseBuilder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    responseBuilder.append(line.trim());
                }
            }

            // 简单提取返回文本
            String content = extractContent(responseBuilder.toString());
            return content != null ? content : "未找到有效回复";

        } catch (Exception e) {
            return "请求失败：" + e.getMessage();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * 构造请求 JSON 字符串
     */
    private String buildRequestBody(String model, String prompt, double temperature) {
        String systemMessage = "你是 Kimi，由 Moonshot AI 提供的人工智能助手，你更擅长中文和英文的对话。你会为用户提供安全，有帮助，准确的回答。同时，你会拒绝一切涉及恐怖主义，种族歧视，黄色暴力等问题的回答。Moonshot AI 为专有名词，不可翻译成其他语言。";

        return String.format(
                "{ \"model\": \"%s\", \"temperature\": %.2f, \"messages\": [" +
                        "{ \"role\": \"system\", \"content\": \"%s\" }, " +
                        "{ \"role\": \"user\", \"content\": \"%s\" } ] }",
                model, temperature, escapeJson(systemMessage), escapeJson(prompt));
    }

    /**
     * 从返回 JSON 中提取第一个回复内容
     * 这里只做简单字符串提取，建议生产环境用 JSON 解析库替换
     */
    private String extractContent(String json) {
        String target = "\"content\":\"";
        int start = json.indexOf(target);
        if (start == -1) return null;

        start += target.length();
        int end = json.indexOf("\"", start);
        if (end == -1) return null;

        // 处理转义字符
        return json.substring(start, end).replace("\\n", "\n").replace("\\\"", "\"");
    }

    /**
     * 简单转义 JSON 特殊字符
     */
    private String escapeJson(String text) {
        if (text == null) return null;
        return text.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
    }
}
