package me.ele.trojan.executor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by michaelzhong on 2017/11/7.
 */

public class ExecutorDispatcher {

    private static volatile ExecutorDispatcher sInstance;

    private ExecutorService recordExecutor;

    private ExecutorService uploadExecutor;

    private ExecutorDispatcher() {
        recordExecutor = Executors.newSingleThreadExecutor();
        uploadExecutor = Executors.newSingleThreadExecutor();
    }

    public static ExecutorDispatcher getInstance() {
        if (sInstance == null) {
            synchronized (ExecutorDispatcher.class) {
                if (sInstance == null) {
                    sInstance = new ExecutorDispatcher();
                }
            }
        }
        return sInstance;
    }

    public void executeRecord(Runnable runnable) {
        if (runnable == null) {
            return;
        }
        recordExecutor.execute(runnable);
    }


    public void executePrepareUpload(Runnable runnable) {
        if (runnable == null) {
            return;
        }
        uploadExecutor.execute(runnable);

    }


}
