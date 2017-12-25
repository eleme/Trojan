package me.ele.trojan;

import android.content.Context;

import java.util.List;

import me.ele.trojan.config.TrojanConfig;
import me.ele.trojan.listener.WaitUploadListener;

/**
 * Created by Eric on 17/2/14.
 */

public class Trojan {

    private static Context sContext;

    public static void init(TrojanConfig config) {
        sContext = config.getContext();
        TrojanManager.getInstance().init(config);
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

    public static void log(String tag, String msg, boolean encryptFlag) {
        TrojanManager.getInstance().log(tag, msg, encryptFlag);
    }

    public static void log(String tag, List<String> msgList) {
        TrojanManager.getInstance().log(tag, msgList);
    }

    public static void log(String tag, List<String> msgList, boolean encryptFlag) {
        TrojanManager.getInstance().log(tag, msgList, encryptFlag);
    }

    public static void logToJson(String tag, Object src) {
        TrojanManager.getInstance().logToJson(tag, src, false);
    }

    public static void logToJson(String tag, Object src, boolean encryptFlag) {
        TrojanManager.getInstance().logToJson(tag, src, encryptFlag);
    }

    public static void prepareUploadLogFile(WaitUploadListener waitUploadListener) {
        TrojanManager.getInstance().prepareUploadLogFile(waitUploadListener);
    }

}
