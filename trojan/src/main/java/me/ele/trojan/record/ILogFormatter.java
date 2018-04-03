package me.ele.trojan.record;

import java.util.List;

/**
 * Created by allen on 2017/11/7.
 */

public interface ILogFormatter {

    String format(String tag, int version, String msg, boolean crypt);

    String format(String tag, int version, List<String> msgFieldList, boolean crypt);

}
