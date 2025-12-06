package com.l4p.proxy;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicInteger;

public class MyInterfaceFactory {

    private static final AtomicInteger count = new AtomicInteger();

    private static File createJavaFile(String className, MyHandler myHandler) throws IOException {
        String func1Body = myHandler.functionBody("func1");
        String func2Body = myHandler.functionBody("func2");
        String func3Body = myHandler.functionBody("func3");
        String content = "package com.l4p.proxy;\n" +
                "\n" +
                "import com.l4p.proxy.MyInterface;\n" +
                "\n" +
                "public class " + className + " implements MyInterface {\n" +
                "    MyInterface myInterface;\n" +
                "\n" +
                "    @Override\n" +
                "    public void func1() {\n" +
                "        " + func1Body +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public void func2() {\n" +
                "        " + func2Body +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public void func3() {\n" +
                "        " + func3Body +
                "    }\n" +
                "}\n";
        File javaFile =  new File(className + ".java");
        Files.writeString(javaFile.toPath(), content);
        return javaFile;
    }

    private static String getClassName() {
        return "MyInterface$proxy" + count.incrementAndGet();
    }

//    private static String functionBody(String functionName) {
//        return "System.out.println(\"" + functionName + "\");";
//    }

    private static MyInterface newInstance(String className, MyHandler myHandler) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchFieldException {
        Class<?> aClass = MyInterfaceFactory.class.getClassLoader().loadClass(className);
        Constructor<?> constructor = aClass.getConstructor();
        MyInterface proxy =  (MyInterface) constructor.newInstance();
        myHandler.setProxy(proxy);
        return proxy;
    }

    public static MyInterface createProxyObj(MyHandler myHandler) throws IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, NoSuchFieldException {
        String className = getClassName();
        File javaFile = createJavaFile(className, myHandler);
        Compiler.compile(javaFile);
        return newInstance("com.l4p.proxy."+ className, myHandler);
    }
}
