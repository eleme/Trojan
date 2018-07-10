package me.ele.trojan.config;

import android.content.Context;
import android.text.TextUtils;

import java.io.File;

import me.ele.trojan.helper.FileHelper;
import me.ele.trojan.utils.AppUtils;

/**
 * Created by michaelzhong on 2017/4/6.
 */

public final class TrojanConfig {
    private Context context;
    private String userInfo;
    private String deviceId;
    private boolean enableLog;
    private boolean enableBackup;
    private String logDir;
    private String cipherKey;

    private TrojanConfig(final Builder builder) {
        this.context = builder.context;
        this.userInfo = builder.userInfo;
        this.deviceId = builder.deviceId;
        this.logDir = builder.logDir;
        this.enableLog = builder.enableLog;
        this.enableBackup = builder.enableBackup;
        this.cipherKey = builder.cipherKey;
    }

    public String getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(String userInfo) {
        this.userInfo = userInfo;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getLogDir() {
        if (TextUtils.isEmpty(logDir)) {
            logDir = FileHelper.getLogDir(context).getAbsolutePath();
        }
        File dirFile = new File(logDir);
        if (!dirFile.exists() || !dirFile.isDirectory()) {
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

    public boolean isEnableBackup() {
        return enableBackup;
    }

    public String getCipherKey() {
        return cipherKey;
    }

    public static class Builder {
        private Context context;
        private String userInfo;
        private String deviceId;
        private String logDir;
        private boolean enableLog = true;
        private boolean enableBackup = false;
        private String cipherKey;

        public Builder(Context context) {
            if (null == context) {
                throw new IllegalArgumentException("context == null");
            }
            this.context = context;
        }

        public Builder userInfo(String userInfo) {
            this.userInfo = userInfo;
            return this;
        }

        public Builder deviceId(String deviceId) {
            this.deviceId = deviceId;
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

        public Builder enableBackup(boolean enableBackup) {
            this.enableBackup = enableBackup;
            return this;
        }

        public Builder cipherKey(String cipherKey) {
            if (cipherKey == null || cipherKey.length() < 16) {
                throw new IllegalArgumentException("the length of cipherKey must be greater than 16");
            }
            this.cipherKey = cipherKey;
            return this;
        }

        public TrojanConfig build() {
            initWithDefaultValues();
            return new TrojanConfig(this);
        }

        private void initWithDefaultValues() {
            if (userInfo == null) {
                userInfo = "";
            }
            if (!TextUtils.isEmpty(logDir)) {
                logDir = logDir + File.separator + AppUtils.getCurProcessName(context);
            }
        }
    }

}
