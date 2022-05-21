package hk.hku.flight.util;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ThreadManager {
    private static class InstanceHolder {
        public volatile static ThreadManager instance = new ThreadManager();
    }
    public static ThreadManager getInstance() {
        return InstanceHolder.instance;
    }
    private Executor executor;
    public ThreadManager() {
        executor = Executors.newCachedThreadPool();
    }

    public void submit(Runnable runnable) {
        executor.execute(runnable);
    }
}
