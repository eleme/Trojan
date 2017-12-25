package me.ele.trojan.upload;

import me.ele.trojan.listener.WaitUploadListener;

/**
 * Created by michaelzhong on 2017/11/7.
 */

public interface ILogUploader {

    void prepareUploadLogFile(final WaitUploadListener waitUploadListener);

}
