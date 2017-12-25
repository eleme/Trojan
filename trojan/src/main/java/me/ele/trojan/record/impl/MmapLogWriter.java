package me.ele.trojan.record.impl;

import android.content.Context;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

import me.ele.trojan.config.TrojanConstants;
import me.ele.trojan.encrypt.EncryptMethod;
import me.ele.trojan.log.Logger;
import me.ele.trojan.record.ILogWriter;
import me.ele.trojan.utils.DateUtils;

/**
 * mmap方式写入，主要是封装了jni操作
 * Created by allen on 2017/11/7.
 */

public class MmapLogWriter implements ILogWriter {

    static {
        try {
            System.loadLibrary("trojan-lib");
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    private native long nativeInit(String basicInfo, String dir,
                                   boolean encryptBasic, String encryptMethod, String key);

    private native long nativeWrite(long logWriterObject, String msgContent, boolean encryptFlag);

    //刷新用户后，需要更新基础信息
    private native void nativeRefreshBasicInfo(long logWriterObject, String basicInfo);

    private native void nativeCloseAndRenew(long logWriterObject);

    private final AtomicBoolean initFlag = new AtomicBoolean(false);

    //C++ LogWriter对象的句柄
    private long nativeLogWriter;

    private String logFileDir;
    private String buildDate;
    private File logFile;

    @Override
    public void init(Context context, final String basicInfoContent, final String dir,
                     boolean encryptBasic, EncryptMethod encryptMethod,
                     String key) throws Throwable {
        Logger.i("MmapWriter", "MMapLogWriter-->init");
        logFileDir = dir;
        buildDate = DateUtils.getDate();
        final String encryptMethodName = null == encryptMethod ? "" : encryptMethod.getMethodName();
        nativeLogWriter = nativeInit(basicInfoContent, dir, encryptBasic, encryptMethodName, key);
        initFlag.set(true);
        logFile = new File(logFileDir + File.separator + buildDate + TrojanConstants.MMAP);
    }

    @Override
    public void write(String content, boolean encryptFlag) throws Exception {
        if (nativeLogWriter <= 0) {
            Logger.i("nativeLogWriter is null,return");
            return;
        }
        if (!initFlag.get()) {
            Logger.i("has not been init");
            return;
        }
        // 判断写入的时候日期是否是当天，判断日志文件是否存在
        if (!DateUtils.getDate().equals(buildDate) || !isLogFileExist()) {
            // 确保文件目录存在，以防被手动删除
            File dir = new File(logFileDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            buildDate = DateUtils.getDate();
            closeAndRenew();
            logFile = new File(logFileDir + File.separator + buildDate + TrojanConstants.MMAP);
        }
        nativeWrite(nativeLogWriter, content, encryptFlag);
    }


    @Override
    public void refreshBasicInfo(String basicInfo) {
        Logger.i("MmapWriter", "MmapLogWriter-->refreshBasicInfo");
        if (nativeLogWriter <= 0) {
            return;
        }
        nativeRefreshBasicInfo(nativeLogWriter, basicInfo);
    }

    /**
     * 这个其实是有两个用处，第一个用处当然是上传时;
     * 第二个用处是如果发现当前日期和现在日期不一样，也要进行这样的操作。
     */
    @Override
    public void closeAndRenew() {
        if (nativeLogWriter <= 0) {
            return;
        }
        nativeCloseAndRenew(nativeLogWriter);
    }

    @Override
    public boolean isLogFileExist() {
        return logFile != null && logFile.exists();
    }

}
