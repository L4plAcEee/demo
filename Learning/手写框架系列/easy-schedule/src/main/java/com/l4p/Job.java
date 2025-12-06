package com.l4p;

/// 任务的封装
public class Job implements Comparable<Job> {
    private Runnable task;

    private long stratTime;

    private long delayTime;

    public Job(Runnable task, long stratTime, long delayTime) {
        this.task = task;
        this.stratTime = stratTime;
        this.delayTime = delayTime;
    }

    public long getDelayTime() {
        return delayTime;
    }

    public void setDelayTime(long delayTime) {
        this.delayTime = delayTime;
    }

    public long getStratTime() {
        return stratTime;
    }

    public void setStratTime(long stratTime) {
        this.stratTime = stratTime;
    }

    public Runnable getTask() {
        return task;
    }

    public void setTask(Runnable task) {
        this.task = task;
    }

    public Job(Runnable task, long stratTime) {
        this.task = task;
        this.stratTime = stratTime;
    }

    @Override
    public int compareTo(Job o) {
        return (int) (this.stratTime - o.stratTime);
    }
}
