package me.ele.trojan.listener;

import java.io.File;
import java.util.List;

/**
 * Created by allen on 2017/11/8.
 */

public interface PrepareUploadListener {

    void readyToUpload();

    void failToReady();
}
