package me.ele.trojan.demo;

import android.app.Application;
import android.os.Environment;

import me.ele.trojan.Trojan;
import me.ele.trojan.config.TrojanConfig;

/**
 * Created by wangallen on 2017/6/7.
 */

public class CustomApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        TrojanConfig config = new TrojanConfig.Builder(this)
                .userInfo("520")
                .deviceId("726e78ec-c351-3a52-ac02-ec04e689a9f3")
                .enableLog(true)
                .cipherKey("1234567890abcdef")
                .build();
        Trojan.init(config);
    }
}
