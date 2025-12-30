package com.l4p;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

// 案例：线程安全操作。虽然变慢了，但是结果是正常的。
public class CaseAboutThreadSafe {
    public static void main(String[] args) throws InterruptedException {
//        AtomicInteger count = new AtomicInteger(1000);
        int[] count = new int[]{1000}; // 原子化的一个方法
        List<Thread> threads = new ArrayList<Thread>();

        Lock lock = new ReentrantLock();

        for (int i = 0; i < 100; ++i) {
            threads.add(new Thread(() -> {
                lock.lock();
                // 注意：需要加 Try 捕获异常
                for (int j = 0; j < 10; ++j) {
                    try {
                        Thread.sleep(2);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
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