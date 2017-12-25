package me.ele.trojan.upload.impl;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import me.ele.trojan.config.TrojanConfig;
import me.ele.trojan.executor.ExecutorDispatcher;
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
        if (PermissionHelper.hasWriteAndReadStoragePermission(context)) {
            ExecutorDispatcher.getInstance().executePrepareUpload(new Runnable() {
                @Override
                public void run() {
                    FileHelper.cleanUpLogFile(context, trojanConfig.getLogDir());
                }
            });
        }
    }

    @Override
    public void prepareUploadLogFile(final WaitUploadListener waitUploadListener) {
        if (logRecorder == null || waitUploadListener == null) {
            Logger.e("LogUploader-->prepareUploadLogFile,waitUploadListener null");
            return;
        }
        if (!PermissionHelper.hasWriteAndReadStoragePermission(context)) {
            Logger.e("LogUploader-->prepareUploadLogFile,no permission");
            waitUploadListener.onReadyFail();
            return;
        }
        // execute upload task after notify the LogRecorder module to close log file
        logRecorder.prepareUpload(new PrepareUploadListener() {
            @Override
            public void readyToUpload(final List<File> waitUploadFileList) {
                Logger.i("LogUploader-->readyToUpload");
                ExecutorDispatcher.getInstance().executePrepareUpload(new Runnable() {
                    @Override
                    public void run() {
                        final List<File> gzFileList = new LinkedList<>();
                        if (waitUploadFileList != null && waitUploadFileList.size() > 0) {
                            for (File logFile : waitUploadFileList) {
                                if (logFile == null || !logFile.isFile()) {
                                    continue;
                                }
                                try {
                                    File gz = FileHelper.save2GZIPFile(logFile);
                                    if (gz != null && gz.isFile()) {
                                        gzFileList.add(gz);
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                waitUploadListener.onReadyToUpload(trojanConfig.getUserInfo(), trojanConfig.getDeviceInfo(), gzFileList);
                            }
                        });
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

}
