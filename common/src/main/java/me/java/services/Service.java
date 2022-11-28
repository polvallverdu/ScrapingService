package me.java.playwrightservice.services;

import java.util.ArrayList;
import java.util.List;

public abstract class Service {

    protected final List<Thread> threads = new ArrayList<>();
    private int threadCount = 0;

    protected void updateThreads() {
        int dif = threadCount - threads.size();
        if (dif > 0) {
            for (int i = 0; i < dif; i++) {
                Thread thread = this.createThread();
                thread.start();
                threads.add(thread);
            }
        } else if (dif < 0) {
            for (int i = 0; i < -dif; i++) {
                threads.remove(threads.size() - 1).interrupt();
            }
        }
    }

    public void setThreads(int threadCount) {
        this.threadCount = threadCount;
        this.updateThreads();
    }

    protected abstract Thread createThread();

}
