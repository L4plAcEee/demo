package com.l4p;

import com.l4p.ComponentScan;

@Component("myService")
@Scope("singleton")
public class MyService implements InitializingBean {

    @AutoWired
    private UserService userService;


    public void test(){
        System.out.println("hello myService!");
        System.out.println(userService);
    }

    @PostConstruct
    public void a(){
        System.out.println("a...");
    }

    @Override
    public void afterPropertiesSet() {
        System.out.println("afterPropertiesSet...");
    }
}
