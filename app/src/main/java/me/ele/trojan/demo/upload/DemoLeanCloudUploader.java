package me.ele.trojan.demo.upload;

import android.content.Context;
import android.text.TextUtils;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVOSCloud;

import java.io.File;
import java.io.FileNotFoundException;

import me.ele.trojan.config.TrojanConstants;
import me.ele.trojan.log.Logger;

/**
 * Created by michaelzhong on 2017/11/7.
 */

public class DemoLeanCloudUploader {

    private static final String APPLICATION_ID = "gtzw5LbEtLc3OyXBodNKdsnU-gzGzoHsz";

    private static final String CLIENT_KEY = "QC72vlbV7m06lsfuJeCz0uiB";

    public DemoLeanCloudUploader(Context context) {
        AVOSCloud.initialize(context, APPLICATION_ID, CLIENT_KEY);
    }

    public void uploadLogFile(String user, String device, File gzFile) throws Exception {
        Logger.i("DemoLeanCloudUploader-->uploadLogFile,gzFile:" + gzFile);
        if (gzFile == null || !gzFile.isFile()) {
            Logger.e("DemoLeanCloudUploader-->uploadLogFile,not exists");
            return;
        }
        String leanCloudFileName = getLeanCloudFileName(user, device, gzFile.getName());
        Logger.i("DemoLeanCloudUploader-->uploadLogFile,leanCloudFileName:" + leanCloudFileName);
        try {
            AVFile avFile = AVFile.withAbsoluteLocalPath(leanCloudFileName, gzFile.getAbsolutePath());
            avFile.save();
            gzFile.delete();
            Logger.i("DemoLeanCloudUploader-->uploadLogFile,success");
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            throw ex;
        } catch (AVException avException) {
            avException.printStackTrace();
            throw avException;
        }
    }

    private String getLeanCloudFileName(String user, String device, String logFileName) {
        StringBuilder fileNameBuilder = new StringBuilder();
        if (!TextUtils.isEmpty(user)) {
            fileNameBuilder.append(user);
            fileNameBuilder.append("-");
        }
        fileNameBuilder.append(logFileName.replace(TrojanConstants.GZ, ""));
        if (!TextUtils.isEmpty(device)) {
            fileNameBuilder.append("-");
            fileNameBuilder.append(device);
        }
        fileNameBuilder.append(TrojanConstants.GZ);
        return fileNameBuilder.toString();
    }

}
