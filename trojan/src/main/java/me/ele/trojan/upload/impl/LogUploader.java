package me.ele.trojan.upload.impl;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.io.File;
import java.util.List;

import me.ele.trojan.config.TrojanConfig;
import me.ele.trojan.executor.TrojanExecutor;
import me.ele.trojan.helper.FileHelper;
import me.ele.trojan.helper.PermissionHelper;
import me.ele.trojan.listener.PrepareUploadListener;
import me.ele.trojan.listener.WaitUploadListener;
import me.ele.trojan.log.Logger;
import me.ele.trojan.record.ILogRecorder;
import me.ele.trojan.upload.ILogUploader;

/**
 * Created by michaelzhong on 2017/11/7.
 */

public class LogUploader implements ILogUploader {

    private final Handler handler = new Handler(Looper.getMainLooper());

    private Context context;

    private TrojanConfig trojanConfig;

    private ILogRecorder logRecorder;

    public LogUploader(final TrojanConfig trojanConfig, ILogRecorder logRecorder) {
        if (trojanConfig == null || logRecorder == null) {
            throw new IllegalArgumentException("trojanConfig or logRecorder can not be null");
        }
        this.context = trojanConfig.getContext();
        this.trojanConfig = trojanConfig;
        this.logRecorder = logRecorder;

        // should check upload file when init
        TrojanExecutor.getInstance().executeUpload(new Runnable() {
            @Override
            public void run() {
                if (!PermissionHelper.hasWriteAndReadStoragePermission(context)) {
                    Logger.e("no permission for cleanUp");
                    return;
                }
                FileHelper.cleanUpLogFile(context, trojanConfig.getLogDir());
            }
        });
    }

    @Override
    public void prepareUploadLogFileAsync(final WaitUploadListener waitUploadListener) {
        if (logRecorder == null || waitUploadListener == null) {
            Logger.e("LogUploader-->prepareUploadLogFileAsync,waitUploadListener null");
            return;
        }
        if (!PermissionHelper.hasWriteAndReadStoragePermission(context)) {
            Logger.e("LogUploader-->prepareUploadLogFileAsync,no permission");
            waitUploadListener.onReadyFail();
            return;
        }
        // execute upload task after notify the LogRecorder module to close log file
        logRecorder.prepareUploadAsync(new PrepareUploadListener() {
            @Override
            public void readyToUpload() {
                Logger.i("LogUploader-->readyToUpload");
                TrojanExecutor.getInstance().executeUpload(new Runnable() {
                    @Override
                    public void run() {
                        final List<File> gzFileList = FileHelper.cleanUpLogFile(context, trojanConfig.getLogDir());
                        notifyPrepareListener(waitUploadListener, true, gzFileList);
                    }
                });

            }

            @Override
            public void failToReady() {
                Logger.e("LogUploader-->failToReady");
                waitUploadListener.onReadyFail();
            }
        });
    }

    @Override
    public File prepareUploadLogFileSync(String dateTime) {
        if (logRecorder == null) {
            return null;
        }
        if (!PermissionHelper.hasWriteAndReadStoragePermission(context)) {
            Logger.e("LogUploader-->prepareUploadLogFileSync,no permission");
            return null;
        }
        return logRecorder.prepareUploadSync(dateTime);
    }

    private void notifyPrepareListener(final WaitUploadListener waitUploadListener, final boolean isSuccess, final List<File> gzFileList) {
        if (waitUploadListener == null) {
            return;
        }
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (isSuccess && gzFileList != null && gzFileList.size() > 0) {
                    waitUploadListener.onReadyToUpload(trojanConfig.getUserInfo(), trojanConfig.getDeviceId(), gzFileList);
                } else {
                    waitUploadListener.onReadyFail();
                }
            }
        });
    }

}
