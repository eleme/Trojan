package me.ele.trojan.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

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

}
