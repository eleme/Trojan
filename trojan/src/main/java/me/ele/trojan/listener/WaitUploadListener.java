package me.ele.trojan.listener;

import java.io.File;
import java.util.List;

/**
 * Created by allen on 17/6/16.
 */

public interface WaitUploadListener {

    void onReadyToUpload(String user, String device, List<File> waitUploadFileList);

    void onReadyFail();

}
