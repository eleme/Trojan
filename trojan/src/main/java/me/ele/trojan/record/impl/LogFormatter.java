package me.ele.trojan.record.impl;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import me.ele.trojan.config.LogConstants;
import me.ele.trojan.record.ILogFormatter;

/**
 * Created by allen on 2017/11/7.
 */

public class LogFormatter implements ILogFormatter {

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.CHINA);

    private final StringBuilder content = new StringBuilder();

    @Override
    public String format(String tag, int version, String msg, boolean crypt) {
        if (content.length() > 0) {
            content.delete(0, content.length());
        }
        content.append(tag);
        content.append(LogConstants.FIELD_SEPERATOR);
        content.append(getTime());
        content.append(LogConstants.FIELD_SEPERATOR);
        content.append("v");
        content.append(version);
        content.append(LogConstants.FIELD_SEPERATOR);
        content.append(msg);
        if (!crypt) {
            content.append('\n');
        }
        return content.toString();
    }

    @Override
    public String format(String tag, int version, List<String> msgFieldList, boolean crypt) {
        if (content.length() > 0) {
            content.delete(0, content.length());
        }
        content.append(tag);
        content.append(LogConstants.FIELD_SEPERATOR);
        content.append(getTime());
        content.append(LogConstants.FIELD_SEPERATOR);
        content.append("v");
        content.append(version);
        content.append(LogConstants.FIELD_SEPERATOR);

        int size = msgFieldList.size();
        for (int i = 0; i < size; ++i) {
            content.append(msgFieldList.get(i));
            if (i < size - 1) {
                content.append(LogConstants.INTERNAL_SEPERATOR);
            }
        }
        if (!crypt) {
            content.append('\n');
        }
        return content.toString();
    }

    private String getTime() {
        return sdf.format(Calendar.getInstance().getTime());
    }

}
