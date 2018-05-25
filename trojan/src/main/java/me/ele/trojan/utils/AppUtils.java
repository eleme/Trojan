package me.ele.trojan.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;

import java.io.File;

import me.ele.trojan.config.TrojanConstants;

/**
 * Created by michaelzhong on 2017/12/22.
 */

public class AppUtils {

    public static final String getDeviceModel() {
        return Build.MODEL;
    }

    public static final int getSDKInt() {
        return Build.VERSION.SDK_INT;
    }

    public static final String getVersionName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (Exception ex) {
            ex.printStackTrace();
            return "UNKNOWN";
        }
    }

    /**
     * get the process name of the current process considering mutiprocess
     *
     * @param context
     * @return String
     */
    public static final String getCurProcessName(Context context) {
        if (context != null) {
            int pid = android.os.Process.myPid();
            ActivityManager mActivityManager = (ActivityManager) context
                    .getSystemService(Context.ACTIVITY_SERVICE);
            for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager
                    .getRunningAppProcesses()) {
                if (appProcess.pid == pid) {
                    return appProcess.processName;
                }
            }
        }
        return "UNKNOWN";
    }

    /**
     * 获取data目录下的可用空间，MB为单位
     *
     * @return
     */
    public static long getDataAvailableSize() {
        try {
            File path = Environment.getDataDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSize();
            long availableBlocks = stat.getAvailableBlocks();
            return (blockSize * availableBlocks) / TrojanConstants.FORMAT_MB;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 获取SD目录下的可用空间，MB为单位
     *
     * @return
     */
    public static long getSDAvailableSize() {
        try {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSize();
            long availableBlocks = stat.getAvailableBlocks();
            return (blockSize * availableBlocks) / TrojanConstants.FORMAT_MB;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

}
