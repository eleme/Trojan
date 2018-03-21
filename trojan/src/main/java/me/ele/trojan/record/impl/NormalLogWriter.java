package me.ele.trojan.record.impl;

import android.content.Context;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.concurrent.atomic.AtomicBoolean;

import me.ele.trojan.config.TrojanConstants;
import me.ele.trojan.log.Logger;
import me.ele.trojan.record.ILogWriter;
import me.ele.trojan.utils.DateUtils;
import me.ele.trojan.utils.IOUtil;

/**
 * Created by allen on 2017/11/7.
 */

/**
 * 这个类的加密还没实现
 */
public class NormalLogWriter implements ILogWriter {

    private Context context;
    private String basicInfoContent;
    private String logFileDir;
    private String buildDate;
    private File logFile;

    private final AtomicBoolean initFlag = new AtomicBoolean(false);

    private BufferedWriter bufferedWriter;

    @Override
    public void init(Context context, String basicInfoContent, String dir, String key) {
        Logger.i("NormalLogWriter-->init");
        this.context = context;
        this.basicInfoContent = basicInfoContent;
        this.logFileDir = dir;
        buildStream();
        initFlag.set(true);
    }

    @Override
    public void write(String content, boolean encryptFlag) {
        try {
            if (null == bufferedWriter) {
                return;
            }
            // 判断写入的时候日期是否是当天，判断日志文件是否存在
            if (!DateUtils.getDate().equals(buildDate) || !isLogFileExist()) {
                // 确保文件目录存在，以防被手动删除
                File dir = new File(logFileDir);
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                closeAndRenew();
            }
            bufferedWriter.write(content);
            bufferedWriter.flush();

        } catch (IOException e) {
            e.printStackTrace();
            Logger.e("write exception:" + e.getMessage());
        }
    }

    @Override
    public void refreshBasicInfo(String basicInfo) {
        this.basicInfoContent = basicInfo;
    }

    @Override
    public void closeAndRenew() {
        //首先关闭文件
        IOUtil.closeQuietly(bufferedWriter);
        //然后重命名文件
        String upFilePath = logFileDir + File.separator + buildDate + TrojanConstants.UP;
        File upFile = new File(upFilePath);
        if (upFile.exists()) {
            upFile.delete();
        }

        File file = new File(logFileDir, buildDate);
        file.renameTo(upFile);
        //最后新建输入流
        buildStream();
    }

    @Override
    public boolean isLogFileExist() {
        return logFile != null && logFile.exists();
    }

    private void buildStream() {
        Logger.i("buildStream");
        File dir = new File(logFileDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String date = DateUtils.getDate();
        File file = new File(dir, date);
        boolean exist = file.exists();
        try {
            if (!exist) {
                file.createNewFile();
            }
            logFile = file;
            FileOutputStream fos = new FileOutputStream(file, exist);
            OutputStreamWriter writer = new OutputStreamWriter(fos);
            bufferedWriter = new BufferedWriter(writer);
            buildDate = date;

            if (!exist) {
                writeBasicInfo();
            }
        } catch (IOException e) {
            Logger.e("buildStream exception:" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void writeBasicInfo() {
        try {
            bufferedWriter.write(basicInfoContent);
            bufferedWriter.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
            Logger.e("writeBasicInfo exception:" + ex.getMessage());
        }
    }

}
