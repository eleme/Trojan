package me.ele.trojan.demo;

import android.app.Application;
import android.os.Environment;

import me.ele.trojan.Trojan;
import me.ele.trojan.config.TrojanConfig;
import me.ele.trojan.encrypt.EncryptMethod;

/**
 * Created by wangallen on 2017/6/7.
 */

public class CustomApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        TrojanConfig config = new TrojanConfig.Builder(this)
                .userInfo("520")
                .deviceInfo("726e78ec-c351-3a52-ac02-ec04e689a9f3")
                // 文件目录可以不用设置，默认为/sdcard/packagename_trojanLog/
                .logDir(Environment.getExternalStorageDirectory().getAbsolutePath() + "/trojan_demo")
                .enableLog(true)
                .encrypt(EncryptMethod.DES, "12345678")
                .build();
        Trojan.init(config);
    }
}
