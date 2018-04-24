package me.ele.trojan.utils;

import me.ele.trojan.config.LogConstants;

/**
 * Created by michaelzhong on 2018/3/30.
 */

public class TagUtil {

    public static int getVersionByTag(String tag) {
        if (tag != null) {
            switch (tag) {
                case LogConstants.BASIC_TAG:
                case LogConstants.KLOG_TAG:
                case LogConstants.HTTP_TAG:
                case LogConstants.VIEW_CLICK_TAG:
                case LogConstants.HTTP_REQUEST_TAG:
                case LogConstants.HTTP_RESPONSE_TAG:
                case LogConstants.ACTIVITY_LIFE_TAG:
                case LogConstants.FRAGMENT_LIFE_TAG:
                case LogConstants.DIALOG_TAG:
                case LogConstants.NETWORK_TAG:
                case LogConstants.BATTERY_TAG:
                case LogConstants.EXCEPTION_TAG:
                case LogConstants.MOTION_TAG:
                case LogConstants.KEY_TAG:
                case LogConstants.EDIT_TAG:
                    return LogConstants.LOG_VERSION1;
                case LogConstants.MEMORY_TAG:
                    return LogConstants.LOG_VERSION2;
            }
        }
        return LogConstants.LOG_VERSION1;
    }

}
