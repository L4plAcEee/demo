package com.l4p;

import java.util.ArrayList;
import java.util.List;

// 案例：线程不安全操作：可以看到打印结果总是不为0
public class CaseAboutThreadNotSafe {
    public static void main(String[] args) throws InterruptedException {
//        AtomicInteger count = new AtomicInteger(1000);
        int[] count = new int[]{1000}; // 原子化的一个方法
        List<Thread> threads = new ArrayList<Thread>();

        for (int i = 0; i < 100; ++i) {
            threads.add(new Thread(() -> {
                for (int j = 0; j < 10; ++j) {
                    try {
                        Thread.sleep(2);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    count[0]--;
                }
            }));
        }
        for (Thread thread : threads) {
            thread.start();
        }
//        Thread.sleep(1000);
        for (Thread thread : threads) {
            thread.join();
        }

        System.out.println(count[0]);
    }
}