package me.ele.trojan.record;

import java.util.List;

/**
 * Created by allen on 2017/11/7.
 */

public interface ILogFormatter {

    String format(String tag, String msg);

    String format(String tag, List<String> msgFieldList);

}
