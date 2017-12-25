package me.ele.trojan.utils;

/**
 * Created by michaelzhong on 17/6/14.
 */

public final class StringUtils {

    public static boolean equals(String str1, String str2) {
        if (null == str1) {
            if (null == str2) {
                return true;
            }
            return false;
        } else {
            return str1.equals(str2);
        }
    }

}
