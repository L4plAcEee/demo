package com.l4p;



import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApplicationContext {

    private Map<String, BeanDefinition> beanDefinitionMap = new HashMap<String, BeanDefinition>();
    private Map<String, Object> singletonObjects = new HashMap<>();
    private List<BeanPostProcessor> beanPostProcessorList = new ArrayList<>();

    public ApplicationContext(Class configClass) {

        // Bean扫描
        scan(configClass);

        for (Map.Entry<String, BeanDefinition> entry : beanDefinitionMap.entrySet()) {
            String beanName = entry.getKey();
            BeanDefinition beanDefinition = entry.getValue();

            if (BeanPostProcessor.class.isAssignableFrom(beanDefinition.getType())) {
                beanPostProcessorList.add((BeanPostProcessor) createBean(beanName, beanDefinition));
            }
        }

        // 非懒加载的单例Bean
        for (Map.Entry<String, BeanDefinition> entry : beanDefinitionMap.entrySet()) {
            String beanName = entry.getKey();
            BeanDefinition beanDefinition = entry.getValue();

            if (beanDefinition.getScope().equals("singleton") && !beanDefinition.getLazy()) {
                // 创建Bean
                Object bean = createBean(beanName, beanDefinition);
                singletonObjects.put(beanName, bean);
            }
        }
    }

    public Object createBean(String beanName, BeanDefinition beanDefinition) {
        Class type = beanDefinition.getType();
        try {
            // 实例化前

            // 实例化（推断构造方法）
            Object instance = type.newInstance();

            // 实例化后

            // 依赖注入 <- 填充Bean <- 依赖查找
            for (Field field : instance.getClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(AutoWired.class)) {
                    // 通过反射赋值
                    String name = field.getName();

                    field.setAccessible(true);
                    field.set(instance, getBean(name));
                }
            }

            // 初始化前 <- @PostConstruct
            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                instance = beanPostProcessor.postProcessBeforeInitialization(instance, beanName);
            }

            // 初始化 <- InitializingBean
            if (instance instanceof InitializingBean initializingBean) {
                initializingBean.afterPropertiesSet();
            }

            // 初始化后 <- AOP BeanPostProcessor <- Bean的生命周期
            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                instance = beanPostProcessor.postProcessAfterInitialization(instance, beanName);
            }


            return instance;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private void scan(Class configClass) {
        // 解析配置类 -> @Bean @Import ...
        if (configClass.isAnnotationPresent(ComponentScan.class)) {
            // 扫描包路径
            ComponentScan componentScanAnnotation = (ComponentScan)  configClass.getAnnotation(ComponentScan.class);
            String path = componentScanAnnotation.value();
            path = path.replace(".", "/");

            ClassLoader classLoader = this.getClass().getClassLoader();
            URL resource = classLoader.getResource(path);

            try {
                File file = new File(resource.toURI());
                if (file.isDirectory()) {
                    for (File listFile : file.listFiles()) {

                        String filePath = listFile.getPath();
                        filePath = filePath.substring(filePath.indexOf("com"), filePath.indexOf(".class"));
                        // Windows 的 "\\"
                        filePath = filePath.replace("\\", ".");

                        // System.out.println(filePath);
                        Class<?> clazz = classLoader.loadClass(filePath);

                        if (clazz.isAnnotationPresent(Component.class)) {
                            // 是一个 Bean
                            // <- BeanDefinition
                            BeanDefinition beanDefinition = new BeanDefinition();
                            beanDefinition.setType(clazz);
                            beanDefinition.setLazy(false);
                            if (clazz.isAnnotationPresent(Scope.class)) {
                                beanDefinition.setScope(clazz.getAnnotation(Scope.class).value());
                            } else {
                                beanDefinition.setScope("singleton");
                            }

                            String beanName = clazz.getAnnotation(Component.class).value();

                            beanDefinitionMap.put(beanName, beanDefinition);
                        }
                    }
                }

            } catch (URISyntaxException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    // Bean的创建的生命周期
    public Object getBean(String beanName) {
        // beanName -> Bean定义 -> Bean
        if (!beanDefinitionMap.containsKey(beanName)) {
            System.out.println("[Debug]:");
            System.out.println("<UNK>beanName<UNK>" + beanName);
            for (Map.Entry<String, BeanDefinition> entry : beanDefinitionMap.entrySet()) {
                System.out.println(entry.getKey());
            }
            throw new RuntimeException();
        }

        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
        // 创建Bean
        if (beanDefinition.getScope().equals("prototype")) {

            return createBean(beanName, beanDefinition);
        } else {
            Object singletonObject = singletonObjects.get(beanName);
            if (singletonObject == null) {
                singletonObject = createBean(beanName, beanDefinition);
                singletonObjects.put(beanName, singletonObject);
            }

            return singletonObject;
        }
    }
}
