package com.l4p;

import java.util.ArrayList;
import java.util.List;

// 案例：自实现 MyLock 实现 锁 功能
public class CaseAboutThreadSafeUsedMyLock {
    public static void main(String[] args) throws InterruptedException {
//        AtomicInteger count = new AtomicInteger(1000);
        int[] count = new int[]{1000}; // 原子化的一个方法
        List<Thread> threads = new ArrayList<Thread>();

        MyLock lock = new MyLock();

        for (int i = 0; i < 100; ++i) {
            threads.add(new Thread(() -> {
                lock.lock();
                // 注意：需要加 Try 捕获异常
                for (int j = 0; j < 10; ++j) {
                    count[0]--;
                }
                lock.unlock();
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