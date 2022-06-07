package hk.hku.flight.util;

import android.os.Handler;
import android.os.Looper;

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
    private Handler mHandler = new Handler(Looper.getMainLooper());
    public ThreadManager() {
        executor = Executors.newCachedThreadPool();
    }

    public void submit(Runnable runnable) {
        executor.execute(runnable);
    }

    public void runOnUiThread(Runnable runnable) {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            runnable.run();
        } else {
            mHandler.post(runnable);
        }
    }
}
