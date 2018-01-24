package me.ele.trojan.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.BatteryManager;

import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;

import me.ele.trojan.Trojan;
import me.ele.trojan.config.LogConstants;
import me.ele.trojan.log.Logger;
import me.ele.trojan.utils.NetworkUtils;

/**
 * Created by wangallen on 2017/3/29.
 */

public class TrojanReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        Logger.i("TrojanReceiver-->action:" + action);
        if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
            showBatteryState(intent);
        } else if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
            showNetworkType(context);
        } else if (Intent.ACTION_TIME_TICK.equals(action)) {
            showMemoryState();
        }
    }

    private void showNetworkType(Context context) {
        int networkType = NetworkUtils.getNetworkType(context.getApplicationContext());
        Trojan.log(LogConstants.NETWORK_TAG, networkType + "");
    }

    private void showBatteryState(Intent intent) {
        int status = intent.getIntExtra("status", 0);
        int level = intent.getIntExtra("level", 0);
        String statusResult = "discharging";
        switch (status) {
            case BatteryManager.BATTERY_STATUS_UNKNOWN:
                statusResult = "discharging";
                break;
            case BatteryManager.BATTERY_STATUS_CHARGING:
                statusResult = "charging";
                break;
            case BatteryManager.BATTERY_STATUS_DISCHARGING:
                statusResult = "discharging";
                break;
            case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                statusResult = "discharging";
                break;
            case BatteryManager.BATTERY_STATUS_FULL:
                statusResult = "charging";
                break;
        }

        List<String> msgList = new LinkedList<>();
        msgList.add(String.valueOf((level * 1.00 / 100)));
        msgList.add(statusResult);
        Trojan.log(LogConstants.BATTERY_TAG, msgList);
    }

    private void showMemoryState() {
        try {
            float totalMemory = (float) (Runtime.getRuntime().totalMemory() * 1.0 / (1024 * 1024));
            DecimalFormat df = new DecimalFormat("######0.00");
            Trojan.log(LogConstants.MEMORY_TAG, String.valueOf(df.format(totalMemory)) + "MB");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
