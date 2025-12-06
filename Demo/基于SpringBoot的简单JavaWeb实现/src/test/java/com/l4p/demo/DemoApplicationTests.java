package com.l4p.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/* 
 * 
 * @AutoConfigureMockMvc: 自动配置MockMvc实例,MockMvc可以模拟HTTP请求。
	mockMvc.perform(get("/hello")): 模拟发送一个GET请求到/hello。
	.andExpect(status().isOk()): 期望HTTP状态码为200 OK。
	.andExpect(content().string("Hello, World!")): 期望响应内容为"Hello, World!"。
	运行mvn test进行测试。
 */
@SpringBootTest
@AutoConfigureMockMvc
class DemoApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void contextLoads() {
    }

    @Test
    void testHelloEndpoint() throws Exception {
        mockMvc.perform(get("/hello"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello, World!"));
    }
	
	@Test
    void testGreetEndpoint() throws Exception {
        this.mockMvc.perform(get("/greet/TestUser"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Greetings, TestUser!")));
    }
}