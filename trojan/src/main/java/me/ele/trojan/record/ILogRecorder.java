package me.ele.trojan.record;

import java.util.List;

import me.ele.trojan.listener.PrepareUploadListener;

/**
 * Created by michaelzhong on 2017/11/7.
 */

public interface ILogRecorder {
    //由于是用户信息，这个就默认要加密
    void refreshUser(String user);

    void prepareUpload(PrepareUploadListener listener);

    void log(String tag, String msg, boolean cryptFlag);

    void log(String tag, List<String> msgFieldList, boolean cryptFlag);

    void logToJson(String tag, Object o, boolean encryptFlag);
}
