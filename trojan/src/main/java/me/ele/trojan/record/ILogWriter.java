package me.ele.trojan.record;

import android.content.Context;

/**
 * Created by allen on 2017/11/7.
 */

public interface ILogWriter {

    void init(Context context, String basicInfo, String dir, String key) throws Throwable;

    void write(String content, boolean cryptFlag) throws Throwable;

    void refreshBasicInfo(String basicInfo);

    //为上传做准备，需要做三件事:第一是关闭当前文件,如果有映射的话，需要取消映射;第二是新建一个文件；第三是对于新文件重新打开输入流或者重新映射
    void closeAndRenew();

    boolean isLogFileExist();

}
