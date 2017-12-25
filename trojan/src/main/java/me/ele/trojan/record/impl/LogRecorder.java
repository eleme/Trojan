package me.ele.trojan.record.impl;

import android.os.Handler;
import android.os.Looper;

import java.io.File;
import java.util.List;

import me.ele.trojan.config.LogConstants;
import me.ele.trojan.config.TrojanConfig;
import me.ele.trojan.config.TrojanConstants;
import me.ele.trojan.executor.ExecutorDispatcher;
import me.ele.trojan.helper.FileHelper;
import me.ele.trojan.helper.PermissionHelper;
import me.ele.trojan.listener.PrepareUploadListener;
import me.ele.trojan.log.Logger;
import me.ele.trojan.record.ILogFormatter;
import me.ele.trojan.record.ILogRecorder;
import me.ele.trojan.record.ILogWriter;
import me.ele.trojan.utils.AppUtils;
import me.ele.trojan.utils.DateUtils;
import me.ele.trojan.utils.GsonUtils;

/**
 * Created by michaelzhong on 2017/11/7.
 */
public class LogRecorder implements ILogRecorder {

    private ILogFormatter logFormatter;

    /**
     * If the MmapLogWriter's initialization have problem, change to use NormalLogWriter
     */
    private ILogWriter logWriter;

    private Handler handler = new Handler(Looper.getMainLooper());

    private TrojanConfig config;
    private String dirPath;

    public LogRecorder(final TrojanConfig config) {
        this.logFormatter = new LogFormatter();
        this.config = config;

        if (!PermissionHelper.hasWriteAndReadStoragePermission(config.getContext())) {
            Logger.e("no permission for init");
            return;
        }

        ExecutorDispatcher.getInstance().executeRecord(new Runnable() {
            @Override
            public void run() {
                tryInitLogWriter();
            }
        });

    }


    private synchronized void tryInitLogWriter() {
        if (null != logWriter) {
            return;
        }
        dirPath = config.getLogDir();
        Logger.i("dirPath:" + dirPath);
        final String basicInfo = getBasicInfo(config);
        try {
            MmapLogWriter mmapLogWriter = new MmapLogWriter();
            mmapLogWriter.init(config.getContext(), logFormatter.format(LogConstants.BASIC,
                    basicInfo), dirPath, config.isEncryptBasicInfo(),
                    config.getEncryptMethod(), config.getKey());
            logWriter = mmapLogWriter;
        } catch (Throwable ex) {
            ex.printStackTrace();
            Logger.e("tryInitLogWriter:" + ex.getMessage());
            initNormalLogWriter();
        }

    }

    @Override
    public void refreshUser(String user) {
        config.setUserInfo(user);
        if (!PermissionHelper.hasWriteAndReadStoragePermission(config.getContext())) {
            Logger.e("no permission for refreshUser:" + user);
            return;
        }
        ExecutorDispatcher.getInstance().executeRecord(new Runnable() {
            @Override
            public void run() {
                final String basicInfo = getBasicInfo(config);
                try {
                    tryInitLogWriter();
                    logWriter.refreshBasicInfo(basicInfo);
                    logWriter.write(logFormatter.format(LogConstants.BASIC, basicInfo),
                            config.isEncryptBasicInfo());
                } catch (Throwable ex) {
                    ex.printStackTrace();
                    Logger.e("refreshUser exception:" + ex.getMessage());
                    if (!(logWriter instanceof NormalLogWriter)) {
                        initNormalLogWriter();
                        logWriter.refreshBasicInfo(basicInfo);
                        tryWriteLog(logFormatter.format(LogConstants.BASIC, basicInfo),
                                config.isEncryptBasicInfo());
                    }
                }

            }
        });
    }

    @Override
    public void log(final String tag, final String msg, final boolean encryptFlag) {
        if (!PermissionHelper.hasWriteAndReadStoragePermission(config.getContext())) {
            Logger.e("no permission for log");
            return;
        }
        ExecutorDispatcher.getInstance().executeRecord(new Runnable() {
            @Override
            public void run() {
                checkInitAndRecordSync(logFormatter.format(tag, msg), encryptFlag);
            }
        });
    }

    @Override
    public void log(final String tag, final List<String> msgFieldList, final boolean encryptFlag) {
        if (!PermissionHelper.hasWriteAndReadStoragePermission(config.getContext())) {
            Logger.e("no permission for log msgFieldList");
            return;
        }

        ExecutorDispatcher.getInstance().executeRecord(new Runnable() {
            @Override
            public void run() {
                //然后要判断是否logWriter是否已经初始化(因为可能在这之前都没权限),如果还没初始化的话，需要先初始化
                checkInitAndRecordSync(logFormatter.format(tag, msgFieldList), encryptFlag);
            }
        });
    }

    @Override
    public void logToJson(final String tag, final Object src, final boolean encryptFlag) {
        if (!PermissionHelper.hasWriteAndReadStoragePermission(config.getContext())) {
            Logger.e("no permission for log");
            return;
        }
        ExecutorDispatcher.getInstance().executeRecord(new Runnable() {
            @Override
            public void run() {
                checkInitAndRecordSync(logFormatter.format(tag, GsonUtils.toJson(src)), encryptFlag);
            }
        });
    }


    @Override
    public void prepareUpload(final PrepareUploadListener listener) {
        if (listener == null) {
            return;
        }
        if (!PermissionHelper.hasWriteAndReadStoragePermission(config.getContext())) {
            Logger.e("no permission for prepareUpload");
            listener.failToReady();
            return;
        }
        ExecutorDispatcher.getInstance().executeRecord(new Runnable() {
            @Override
            public void run() {
                tryInitLogWriter();
                logWriter.closeAndRenew();
                final String writeFileName = DateUtils.getDate() + (logWriter instanceof MmapLogWriter ? TrojanConstants.MMAP : "");
                // avoid to block write operation, we just rename except the writing log file, have not compress log file
                final List<File> waitUploadFileList = FileHelper.renameToUpAllLogFileIfNecessary(config.getUserInfo(), dirPath, writeFileName);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.readyToUpload(waitUploadFileList);
                    }
                });
            }
        });
    }

    private void initNormalLogWriter() {
        Logger.e("initNormalLogWriter");
        try {
            NormalLogWriter normalLogWriter = new NormalLogWriter();
            normalLogWriter.init(config.getContext(), logFormatter.format(LogConstants.BASIC,
                    getBasicInfo(config)), dirPath, config.isEncryptBasicInfo(), config.getEncryptMethod(),
                    config.getKey());
            logWriter = normalLogWriter;
        } catch (Throwable e) {
            e.printStackTrace();
            Logger.e("initNormalLogWriter:" + e);
        }
    }

    private void tryWriteLog(String content, boolean encryptFlag) {
        try {
            logWriter.write(content, encryptFlag);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private String getBasicInfo(TrojanConfig config) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(config.getDeviceInfo())
                .append(LogConstants.INTERNAL_SEPERATOR)
                .append(AppUtils.getCurProcessName(config.getContext()))
                .append(LogConstants.INTERNAL_SEPERATOR)
                .append(AppUtils.getVersionName(config.getContext()))
                .append(LogConstants.INTERNAL_SEPERATOR)
                .append(config.getUserInfo());
        return stringBuilder.toString();
    }

    /**
     * check whether logWriter is initialized or not firstly.
     * then write the content to log file.
     *
     * @param msgContent
     * @param encryptFlag
     */
    private void checkInitAndRecordSync(final String msgContent, boolean encryptFlag) {
        tryInitLogWriter();
        try {
            logWriter.write(msgContent, encryptFlag);
        } catch (Throwable ex) {
            ex.printStackTrace();
            Logger.e("checkInitAndRecordSync,ex:" + ex);
            if (!(logWriter instanceof NormalLogWriter)) {
                initNormalLogWriter();
                tryWriteLog(msgContent, encryptFlag);
            }
        }
    }

}
