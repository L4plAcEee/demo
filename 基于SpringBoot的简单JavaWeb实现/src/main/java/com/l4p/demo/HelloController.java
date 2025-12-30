package com.l4p.demo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller; // 注意：这里改为 @Controller
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller // 使用 @Controller，而不是 @RestController
public class HelloController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/hello")
    public String hello(@RequestParam(value = "name", defaultValue = "World") String name, Model model) {
        User user = new User();
        user.setName(name);
        userRepository.save(user);
        model.addAttribute("name", name); // 将 name 属性添加到 Model 中
        return "hello"; // 返回视图名称 "hello"
    }

    @GetMapping("/users")
    public String listUsers(Model model) {
        List<User> users = userRepository.findAll();
        model.addAttribute("users", users);
        return "users"; // 返回视图名称 "users"
    }
    
    @GetMapping("/greet/{username}")
    public String greet(@PathVariable("username") String username, Model model) {
        model.addAttribute("username", username);
        return "greet"; //返回视图名称 "greet"
    }
}
