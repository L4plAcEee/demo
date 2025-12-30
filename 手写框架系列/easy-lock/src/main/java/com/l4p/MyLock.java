package com.l4p;


import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

// 关键逻辑：只有拿到 钥匙 的 线程 才能运行，
// 没有拿到钥匙的 线程 需要被阻塞
public class MyLock {
    // ===实现：公平锁===
    // 知识点：CAS操作与原子变量
    AtomicBoolean key = new AtomicBoolean(false);

    Thread owner = null;
    // DummyHead
    // 原子引用 线程安全
    AtomicReference<Node> head = new AtomicReference<>(new Node());

    AtomicReference<Node> tail = new AtomicReference<>(head.get());

    void lock() {
        String Name = Thread.currentThread().getName();
//        if (key.compareAndSet(false, true)) {
//            System.out.println(Name + "直接拿到了锁");
//            owner = Thread.currentThread();
//            return;
//        }
        Node cur = new Node();
        cur.thread = Thread.currentThread();
        while (true) {
            // 直接 `tail = cur;` <- 线程不安全
            Node curTail = tail.get();
            if (tail.compareAndSet(curTail, cur)) {
                System.out.println(Name + "加入到了链表尾部");
                cur.pre = curTail;
                curTail.next = cur;
                break;
            }
        }
        while (true) {
            // 警惕虚假唤醒
//            LockSupport.park();
            if (cur.pre == head.get() && key.compareAndSet(false, true)) {
                owner = Thread.currentThread();
                // 不需要对 head 原子操作， 因为该线程已经持有 锁 了
                head.set(cur);
                cur.pre.next = null;
                cur.pre = null;
                System.out.println(Name + "被唤醒之后拿到了锁");
                return;
            }
            // 这样写 防止 没有线程唤醒 <- 自己唤醒自己
            LockSupport.park();
        }
    }

    void unlock() {
        if (Thread.currentThread() != owner) {
            throw new IllegalStateException(Thread.currentThread().getName() + "无锁，但尝试解锁");
        }
        // === 能解锁的 都是持有锁的 所以不需要 CAS操作===
        Node headNode = head.get();
        Node next = headNode.next;
        key.set(false);

        if (next != null) {
            System.out.println(Thread.currentThread().getName() + "唤醒了" + next.thread.getName());
            LockSupport.unpark(next.thread);
        }
    }

    class Node {
        Node pre;
        Node next;
        Thread thread;
    }

    // ===实现一：自旋锁===
//    void lock() {
//        // 持续竞争
//        while (true) {
//            if (key.compareAndSet(false, true)) {
//                return;
//            }
//        }
//    }
//
//    void unlock() {
//        while (true) {
//            if (key.compareAndSet(true, false)) {
//                return;
//            }
//        }
//    }
}
