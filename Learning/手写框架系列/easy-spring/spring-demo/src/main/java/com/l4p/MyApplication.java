package com.l4p;

public class MyApplication {
    public static void main(String[] args) {
        ApplicationContext applicationContext = new ApplicationContext(MyConfig.class);
        MyService myService = (MyService) applicationContext.getBean("myService");
        myService.test();
    }
}
