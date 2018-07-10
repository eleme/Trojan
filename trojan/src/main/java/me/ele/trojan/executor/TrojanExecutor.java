package me.ele.trojan.executor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by michaelzhong on 2017/11/7.
 */

public class TrojanExecutor {

    private static volatile TrojanExecutor sInstance;

    private ExecutorService recordExecutor;

    private ExecutorService uploadExecutor;

    private TrojanExecutor() {
        recordExecutor = Executors.newSingleThreadExecutor();
        uploadExecutor = Executors.newSingleThreadExecutor();
    }

    public static TrojanExecutor getInstance() {
        if (sInstance == null) {
            synchronized (TrojanExecutor.class) {
                if (sInstance == null) {
                    sInstance = new TrojanExecutor();
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


    public void execute(Runnable runnable) {
        if (runnable == null) {
            return;
        }
        uploadExecutor.execute(runnable);
    }


}
