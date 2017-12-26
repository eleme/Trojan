package me.ele.trojan.config;

import android.content.Context;
import android.text.TextUtils;

import java.io.File;

import me.ele.trojan.encrypt.EncryptMethod;
import me.ele.trojan.helper.FileHelper;
import me.ele.trojan.utils.AppUtils;

/**
 * Created by michaelzhong on 2017/4/6.
 */

public final class TrojanConfig {
    private Context context;
    private String userInfo;
    private String deviceInfo;
    private boolean enableLog;
    private String logDir;

    /**
     * 是否需要加密基本信息
     */
    private boolean encryptBasicInfo = false;
    private EncryptMethod encryptMethod;
    private String key;

    private TrojanConfig(final Builder builder) {
        this.context = builder.context;
        this.userInfo = builder.userInfo;
        this.deviceInfo = builder.deviceInfo;
        this.logDir = builder.logDir;
        this.enableLog = builder.enableLog;
        this.encryptMethod = builder.encryptMethod;
        this.encryptMethod = builder.encryptMethod;
        this.key = builder.cipherKey;
    }

    public String getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(String userInfo) {
        this.userInfo = userInfo;
    }

    public String getDeviceInfo() {
        return deviceInfo;
    }

    public String getLogDir() {
        File dirFile = new File(logDir);
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }
        return logDir;
    }

    public Context getContext() {
        return context;
    }

    public boolean isEnableLog() {
        return enableLog;
    }

    public boolean isEncryptBasicInfo() {
        return encryptBasicInfo;
    }

    public EncryptMethod getEncryptMethod() {
        return encryptMethod;
    }

    public String getKey() {
        return key;
    }

    public static class Builder {
        private Context context;
        private String userInfo;
        private String deviceInfo;
        private String logDir;
        private boolean enableLog = true;

        private boolean encryptBasicInfo = false;

        private EncryptMethod encryptMethod;

        private String cipherKey;

        public Builder(Context context) {
            if (null == context) {
                throw new IllegalArgumentException("context == null");
            }
            this.context = context;
        }

        public Builder userInfo(String info) {
            this.userInfo = info;
            return this;
        }

        public Builder deviceInfo(String info) {
            this.deviceInfo = info;
            return this;
        }

        public Builder logDir(String logDir) {
            this.logDir = logDir;
            return this;
        }

        public Builder enableLog(boolean enableLog) {
            this.enableLog = enableLog;
            return this;
        }

        public Builder encryptBasicInfo(boolean encryptBasicInfo) {
            this.encryptBasicInfo = encryptBasicInfo;
            return this;
        }

        public Builder encrypt(EncryptMethod encryptMethod, String key) {
            this.encryptMethod = encryptMethod;
            this.cipherKey = key;
            return this;
        }

        public TrojanConfig build() {
            initWithDefaultValues();
            return new TrojanConfig(this);
        }

        private void initWithDefaultValues() {
            if (deviceInfo == null) {
                deviceInfo = AppUtils.getDeviceModel() + "," + AppUtils.getSDKInt();
            }
            if (userInfo == null) {
                userInfo = "";
            }
            if (TextUtils.isEmpty(logDir)) {
                logDir = FileHelper.getLogDir(context).getAbsolutePath();
            } else {
                logDir = logDir + File.separator + AppUtils.getCurProcessName(context);
            }
        }

    }

}
