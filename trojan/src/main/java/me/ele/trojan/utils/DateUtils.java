package me.ele.trojan.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by michaelzhong on 2017/11/7.
 */

public class DateUtils {

    private static final SimpleDateFormat dfYMD = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);

    public static String getDate() {
        return dfYMD.format(Calendar.getInstance().getTime());
    }

}
