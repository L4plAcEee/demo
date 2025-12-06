package com.l4p;

public class DiscardRejectHandle implements RejectHandle{
    @Override
    public void reject(Runnable rejectCommand, MyThreadPool threadPool) {
        threadPool.commandList.poll();
        threadPool.execute(rejectCommand);
    };
}
