package me.ele.trojan.upload;

import java.io.File;

import me.ele.trojan.listener.WaitUploadListener;

/**
 * Created by michaelzhong on 2017/11/7.
 */

public interface ILogUploader {

    void prepareUploadLogFileAsync(final WaitUploadListener waitUploadListener);

    File prepareUploadLogFileSync(final String dateTime);

}
