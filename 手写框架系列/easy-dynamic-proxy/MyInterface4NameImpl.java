package com.l4p;

import com.l4p.proxy.MyInterface;

public class MyInterface4NameImpl implements MyInterface {

    @Override
    public void func1() {
        System.out.println("func1");
    }

    @Override
    public void func2() {
        System.out.println("func2");
    }

    @Override
    public void func3() {
        System.out.println("func3");
    }
}
