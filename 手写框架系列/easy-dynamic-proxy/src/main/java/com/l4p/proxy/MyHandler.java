package com.l4p.proxy;

public interface MyHandler {
    String functionBody(String name);

    default void setProxy(MyInterface proxy) throws NoSuchFieldException, IllegalAccessException {

    }
}
