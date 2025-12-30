package com.l4p;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {
//        MyThreadPool myThreadPool = new MyThreadPool(2, 4, 1, TimeUnit.SECONDS, new ThrowRejectHandle(), new ArrayBlockingQueue<>(2));
        MyThreadPool myThreadPool = new MyThreadPool(2, 4, 1, TimeUnit.SECONDS, new DiscardRejectHandle(), new ArrayBlockingQueue<>(2));
        for (int i = 0; i < 10; i++) {
            int finalI = i;
            myThreadPool.execute(()->{
                try {
                    Thread.sleep(1000);
                }  catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                System.out.println(Thread.currentThread().getName() + ' ' + finalI);
            });
        }
        System.out.println("主线程未被阻塞");
    }
}