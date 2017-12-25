package me.ele.trojan;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.text.TextUtils;

import java.util.List;

import me.ele.trojan.config.TrojanConfig;
import me.ele.trojan.internal.ActivityLife;
import me.ele.trojan.internal.ExceptionHandler;
import me.ele.trojan.listener.WaitUploadListener;
import me.ele.trojan.log.Logger;
import me.ele.trojan.receiver.TrojanReceiver;
import me.ele.trojan.record.ILogRecorder;
import me.ele.trojan.record.impl.LogRecorder;
import me.ele.trojan.upload.ILogUploader;
import me.ele.trojan.upload.impl.LogUploader;

/**
 * Created by michaelzhong on 2017/11/7.
 */

public class TrojanManager {

    private static volatile TrojanManager sInstance;

    private Context context;

    private TrojanConfig trojanConfig;

    private TrojanReceiver trojanReceiver;

    private ILogRecorder logRecorder;

    private ILogUploader logUploader;

    private TrojanManager() {

    }

    public static TrojanManager getInstance() {
        if (sInstance == null) {
            synchronized (TrojanManager.class) {
                if (sInstance == null) {
                    sInstance = new TrojanManager();
                }
            }
        }
        return sInstance;
    }

    public void init(TrojanConfig trojanConfig) {
        Logger.i("TrojanManager-->init");
        if (trojanConfig == null) {
            throw new IllegalArgumentException("TrojanManager trojanConfig is null");
        }

        this.destroy();
        Logger.setLog(trojanConfig.isEnableLog());

        this.context = trojanConfig.getContext();
        this.trojanConfig = trojanConfig;

        // register activity life callback
        ActivityLife.init(this.context);
        // init exception collector
        ExceptionHandler.init();
        // register batter receiver
        this.registerBatteryReceiver(this.context);

        // init recorder module
        this.logRecorder = new LogRecorder(this.trojanConfig);

        // init uploader module
        this.logUploader = new LogUploader(this.trojanConfig, this.logRecorder);
    }

    public void refreshUser(String user) {
        if (trojanConfig != null) {
            trojanConfig.setUserInfo(user);
        }
        if (logRecorder != null) {
            logRecorder.refreshUser(user);
        }
    }

    public void log(String tag, String msg) {
        log(tag, msg, false);
    }

    public void log(String tag, List<String> msgList) {
        log(tag, msgList, false);
    }

    public void log(String tag, String msg, boolean cryptFlag) {
        if (logRecorder != null && !TextUtils.isEmpty(tag) && !TextUtils.isEmpty(msg)) {
            logRecorder.log(tag, msg, cryptFlag);
        }
    }

    public void log(String tag, List<String> msgList, boolean cryptFlag) {
        if (logRecorder != null && !TextUtils.isEmpty(tag) && msgList != null && msgList.size() > 0) {
            logRecorder.log(tag, msgList, cryptFlag);
        }
    }

    public void logToJson(String tag, Object obj) {
        logRecorder.logToJson(tag, obj, false);
    }

    public void logToJson(String tag, Object obj, boolean encryptFlag) {
        if (logRecorder != null && !TextUtils.isEmpty(tag) && obj != null) {
            logRecorder.logToJson(tag, obj, encryptFlag);
        }
    }

    public void prepareUploadLogFile(final WaitUploadListener listener) {
        if (logUploader != null && listener != null) {
            logUploader.prepareUploadLogFile(listener);
        }
    }

    public void destroy() {
        if (this.context != null && this.trojanReceiver != null) {
            this.context.unregisterReceiver(this.trojanReceiver);
            this.trojanReceiver = null;
        }
    }

    private void registerBatteryReceiver(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("TrojanManager have not init");
        }
        if (this.trojanReceiver != null) {
            return;
        }
        this.trojanReceiver = new TrojanReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        context.registerReceiver(this.trojanReceiver, filter);
    }
}
