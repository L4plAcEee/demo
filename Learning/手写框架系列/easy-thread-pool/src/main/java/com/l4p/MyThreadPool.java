package com.l4p;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class MyThreadPool {
    // 1. 线程什么时候创建？
    // 2. 线程的 runnable 是什么？ 是我们提交 command 吗？

    // 可能有人会想，我直接创建 N 个线程存进 List 里面，
    // 然后 如果有 command 就拉出来 执行。
    // 这样是不行的，线程的 生命周期 决定了不可以这样做。

    // 那么我们该如何实现？我们可以将 Command 存进 CommandList，
    // 将 Command 分配到已有的 Thread 中。

    // 示例三：
    private int corePoolSize = 10;

    private int maximumPoolSize = 16;

    private long keepAliveTime = 60;

    private TimeUnit timeUnit = TimeUnit.SECONDS;
    // 我们的线程池 应该有多少线程？
    // 判断 coreThreadList 中 一共有多少个元素， 如果没有触及 corePoolSize 那就创建。
    private final List<Thread> coreThreadList = new ArrayList<Thread>();

    private final List<Thread> supportThreadList = new ArrayList<Thread>();
    // 将拒绝策略封装，并灵活传入调用
    private final RejectHandle rejectHandle;

    BlockingQueue<Runnable> commandList = new ArrayBlockingQueue<>(1024) ;
    
    public MyThreadPool(int  corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit timeUnit, RejectHandle rejectHandle, BlockingQueue<Runnable> workQueue) {
        this.corePoolSize = corePoolSize;
        this.maximumPoolSize = maximumPoolSize;
        this.keepAliveTime = keepAliveTime;
        this.timeUnit = timeUnit;
        this.rejectHandle = rejectHandle;
        this.commandList = workQueue;
    }


//



    void execute(Runnable command){
        // 这段逻辑其实有 线程安全问题 但是可以通过 加锁 的措施解决，这里就先不解决了
        if (coreThreadList.size() < corePoolSize){
            Thread thread = new CoreThread();
            coreThreadList.add(thread);
            thread.start();
        }
        // Clean Code 原则：卫语句（提前退出）
        if (commandList.offer(command)) return;

        if (coreThreadList.size() +  supportThreadList.size() < maximumPoolSize){
            Thread thread = new SupportThread();
            supportThreadList.add(thread);
            thread.start();
        }
        if (!commandList.offer(command)) {
//             throw  new RuntimeException("阻塞队列满了");
            rejectHandle.reject(command, this);
        }
    };

    class CoreThread extends Thread{
        @Override
        public void run() {
            while (true){
                try {
                    Runnable command = commandList.take();
                    command.run();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    class SupportThread extends Thread{
        @Override
        public void run() {
            while (true){
                try {
                    // 采用 轮询 的方式，灵活结束辅助线程
                    Runnable command = commandList.poll(keepAliveTime, timeUnit);
                    if (command != null){
                        break;
                    }
                    command.run();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            System.out.println(Thread.currentThread().getName() + "线程结束!");
        }
    }
    // 提取为私有内部类
//    private final Runnable coreTask = () -> {
//        while (true){
//            try {
//                Runnable command = commandList.take();
//                command.run();
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//        }
//    };
//
//    private final Runnable supportTask = () -> {
//        while (true){
//            try {
//                // 采用 轮询 的方式，灵活结束辅助线程
//                Runnable command = commandList.poll(keepAliveTime, timeUnit);
//                if (command != null){
//                    break;
//                }
//                command.run();
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//        }
//        System.out.println(Thread.currentThread().getName() + "线程结束!");
//    };

    // 示例二：完成一个简单的只有单个线程的线程池
//    BlockingQueue<Runnable> commandList = new ArrayBlockingQueue<>(1024) ;
//
//    Thread thread = new Thread(()->{
//        while (true){
//            try {
//                Runnable command = commandList.take();
//                command.run();
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//        }
//    }, "唯一线程");
//
//    {
//        thread.start();
//    }
//
//    void execute(Runnable command){
////        commandList.add(command);
//        boolean offer = commandList.offer(command);
//    }

    // 示例一：
//    List<Runnable> commandList = new ArrayList<>();
//    Thread thread = new Thread(()->{
//        // *但是很明显，这样做会无谓的消耗CPU资源，
//        // 我们需要一个数据结构在空的时候可以阻塞线程，防止消耗资源
//        // -> **阻塞队列**
//        while (true){
//            if (!commandList.isEmpty()) {
//                Runnable command = commandList.remove(0);
//                command.run();
//            }
//            ;
//        }
//    });
//
//    void execute(Runnable command){
//        commandList.add(command);
//    }
    // 这样我们就达成了一个线程复用的效果
}
