package me.ele.trojan.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.BatteryManager;

import java.util.ArrayList;
import java.util.List;

import me.ele.trojan.Trojan;
import me.ele.trojan.config.LogConstants;
import me.ele.trojan.executor.TrojanExecutor;
import me.ele.trojan.helper.PerformanceHelper;
import me.ele.trojan.log.Logger;
import me.ele.trojan.utils.NetworkUtils;

/**
 * Created by wangallen on 2017/3/29.
 */

public class TrojanReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, final Intent intent) {
        final String action = intent.getAction();
        Logger.i("TrojanReceiver-->action:" + action);
        if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
            TrojanExecutor.getInstance().execute(new Runnable() {
                @Override
                public void run() {
                    showBatteryState(intent);
                }
            });
        } else if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
            final Context applicationContext = context.getApplicationContext();
            TrojanExecutor.getInstance().execute(new Runnable() {
                @Override
                public void run() {
                    showNetworkType(applicationContext);
                }
            });
        } else if (Intent.ACTION_TIME_TICK.equals(action)) {
            TrojanExecutor.getInstance().execute(new Runnable() {
                @Override
                public void run() {
                    PerformanceHelper.recordMemory();
                    PerformanceHelper.recordThread();
                }
            });
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

        List<String> msgList = new ArrayList<>();
        msgList.add(String.valueOf((level * 1.00 / 100)));
        msgList.add(statusResult);
        Trojan.log(LogConstants.BATTERY_TAG, msgList);
    }

}
