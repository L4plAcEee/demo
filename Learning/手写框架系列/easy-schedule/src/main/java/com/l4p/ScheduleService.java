package com.l4p;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.locks.LockSupport;

public class ScheduleService {

    Trigger trigger = new Trigger();

    ExecutorService executorService = Executors.newFixedThreadPool(6);

    void schedule(Runnable task, long delay) {
        Job job = new Job(task, System.currentTimeMillis() + delay, delay);
        trigger.jobs.offer(job);
        trigger.wakeup();
    }

    // 等待合适时间，把对应的任务扔到线程池中
    class Trigger{

//        List<Job> jobs = new ArrayList<Job>();
        PriorityBlockingQueue<Job> jobs = new PriorityBlockingQueue<Job>();

        Thread thread = new Thread(() -> {
            // 缺陷一：如果 Job池 为空，循环相当于一个死循环，非常耗费资源。
            // 缺陷二：我们希望 Job 可以按 等待时间 排序，让先执行的排在前面。
                // 解决一：为 Job 类实现 排序接口 -> 但是 排序的算法时间复杂度 为：O（log（n） * n），且可能有线程安全问题。
                // 解决二：在以上基础上，将 ArrayList 改为 PriorityBlockQueue。
            while (true) {
                if (jobs.isEmpty()) {
                    LockSupport.park();
                };
                Job lateJob = jobs.peek();
                if (lateJob.getStratTime() < System.currentTimeMillis()) {
                    // 为什么需要再次 poll ？
                    // 在多线程的环境下不能保证拿到的 job1 == job2，但是由于优先队列的问题，就算拿到的 job2 不是 job1，
                    // 也能保证 job2 必须比 job1 先执行。
                    lateJob =  jobs.poll();
                    executorService.execute(lateJob.getTask());
                    Job nextJob = new Job(lateJob.getTask(), System.currentTimeMillis() + lateJob.getDelayTime(), lateJob.getDelayTime());
                    jobs.offer(nextJob);
                } else {
                    // 注意 CPU 的 虚假唤醒机制
                    // 所以 不能写if 要写 while
                    LockSupport.parkUntil(lateJob.getStratTime());
                }
//                if (!jobs.isEmpty()) {
//
//                    Job job = jobs.poll();
//                    long waitTime = job.getStratTime() - System.currentTimeMillis();
//                    if (waitTime > 0) {
//                        // 被唤醒时，需要重新去拿最新最近的任务
//                        LockSupport.park();
//                    }
//                    executorService.execute(job.getTask());
//                }
//                for (Job job : jobs) {
//                    long waitTime = job.getStratTime() - System.currentTimeMillis();
//                    if (waitTime > 0) {
//                        try {
//                            Thread.sleep(waitTime);
//                        } catch (InterruptedException e) {
//                            throw new RuntimeException(e);
//                        }
//                    }
//                    executorService.execute(job.getTask());
//                }
            }
        });

        {
            thread.start();
            System.out.println("触发器启动");
        }
        void wakeup() {
            LockSupport.unpark(thread);
        }
    }
    // 实现一：采用sleep的方式，简单粗暴，但是有明显缺陷。
//    void schedule(Runnable task, long delay) {
//        ExecutorService executorService = Executors.newFixedThreadPool(6);
//        executorService.execute(() -> {
//            while(true){
//                try {
//                    Thread.sleep(delay);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                task.run();
//            }
//        });
//    }
}
