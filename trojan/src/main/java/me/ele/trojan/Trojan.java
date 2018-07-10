package me.ele.trojan;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.util.List;

import me.ele.trojan.config.TrojanConfig;
import me.ele.trojan.config.TrojanConstants;
import me.ele.trojan.executor.TrojanExecutor;
import me.ele.trojan.listener.WaitUploadListener;
import me.ele.trojan.utils.AppUtils;

/**
 * Created by Eric on 17/2/14.
 */

public class Trojan {

    private static Context sContext;

    public static void init(final TrojanConfig config) {

        sContext = config.getContext();

        TrojanExecutor.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                if (AppUtils.getDataAvailableSize() < TrojanConstants.MIN_FREE_SPACE_MB
                        || AppUtils.getSDAvailableSize() < TrojanConstants.MIN_FREE_SPACE_MB) {
                    Log.e("Trojan", "Trojan-->init,failed:space is not enough!");
                    return;
                }
                TrojanManager.getInstance().init(config);
            }
        });
    }

    public static Context getContext() {
        if (sContext == null) {
            throw new IllegalArgumentException("Trojan have not init");
        }
        return sContext.getApplicationContext();
    }

    /**
     * 更新用户信息时，应该使用这个方法来记录最新的用户信息
     *
     * @param user
     */
    public static void refreshUser(String user) {
        TrojanManager.getInstance().refreshUser(user);
    }

    public static void log(String tag, String msg) {
        TrojanManager.getInstance().log(tag, msg);
    }

    public static void log(String tag, int version, String msg) {
        TrojanManager.getInstance().log(tag, version, msg, false);
    }

    public static void log(String tag, int version, String msg, boolean encryptFlag) {
        TrojanManager.getInstance().log(tag, version, msg, encryptFlag);
    }

    public static void log(String tag, List<String> msgList) {
        TrojanManager.getInstance().log(tag, msgList);
    }

    public static void log(String tag, int version, List<String> msgList) {
        TrojanManager.getInstance().log(tag, version, msgList, false);
    }

    public static void log(String tag, int version, List<String> msgList, boolean encryptFlag) {
        TrojanManager.getInstance().log(tag, version, msgList, encryptFlag);
    }

    public static void logToJson(String tag, int version, Object src) {
        TrojanManager.getInstance().logToJson(tag, version, src, false);
    }

    public static void logToJson(String tag, int version, Object src, boolean encryptFlag) {
        TrojanManager.getInstance().logToJson(tag, version, src, encryptFlag);
    }

    public static void prepareUploadLogFileAsync(WaitUploadListener waitUploadListener) {
        TrojanManager.getInstance().prepareUploadLogFileAsync(waitUploadListener);
    }

    public static File prepareUploadLogFileSync(final String dateTime) {
        return TrojanManager.getInstance().prepareUploadLogFileSync(dateTime);
    }

    public static void destroy() {
        TrojanManager.getInstance().destroy();
    }

}
