package com.l4p;

public interface RejectHandle {

    void reject(Runnable rejectCommand, MyThreadPool threadPool);
}
