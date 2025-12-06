package com.l4p;

import com.l4p.proxy.MyHandler;
import com.l4p.proxy.MyInterface;
import com.l4p.proxy.MyInterfaceFactory;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

//TIP 要<b>运行</b>代码，请按 <shortcut actionId="Run"/> 或
// 点击装订区域中的 <icon src="AllIcons.Actions.Execute"/> 图标。
public class Main {
    public static void main(String[] args) throws IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, NoSuchFieldException {
        MyInterface proxyObj = MyInterfaceFactory.createProxyObj(new PrintFunctionName());
        proxyObj.func1();
        proxyObj.func2();
        proxyObj.func3();

        System.out.println("===========");
        proxyObj = MyInterfaceFactory.createProxyObj(new PrintFunctionName1());
        proxyObj.func1();
        proxyObj.func2();
        proxyObj.func3();

        System.out.println("===========");
        proxyObj = MyInterfaceFactory.createProxyObj(new LoggerHandler(proxyObj));
        proxyObj.func1();
        proxyObj.func2();
        proxyObj.func3();

    }
    static class PrintFunctionName implements MyHandler {

        @Override
        public String functionBody(String name) {
            String ret = "动态代理实现：打印函数名：" + name;
            return "System.out.println(\"" + ret + "\");";
        }
    }

    static class PrintFunctionName1 implements MyHandler {
        @Override
        public String functionBody(String name) {
            StringBuilder sb = new StringBuilder();
            sb.append("System.out.println(1);").append("System.out.println(\"").append(name).append("\");");
            return  sb.toString();
        }
    }

    static class LoggerHandler implements MyHandler {

        MyInterface myInterface;

        public LoggerHandler(MyInterface myInterface) {
            this.myInterface = myInterface;
        }

        @Override
        public void setProxy(MyInterface proxy) throws NoSuchFieldException, IllegalAccessException {
            Class<?> aClass = proxy.getClass();
            Field field = aClass.getDeclaredField("myInterface");
            field.setAccessible(true);
            field.set(proxy, myInterface);
        }

        @Override
        public String functionBody(String name) {
            return "        System.out.println(\"[LOG] Before\");\n" +
                    "        myInterface." + name + "();\n" +
                    "        System.out.println(\"[LOG] After\");";
        }
    }
}