package me.ele.trojan.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.BatteryManager;

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
        String action = intent.getAction();
        Logger.i("TrojanReceiver->action:" + action);
        if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
            showBatteryState(intent);
        } else if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
            showNetworkType(context);
        }

    }

    private void showNetworkType(Context context) {
        int networkType = NetworkUtils.getNetworkType(context.getApplicationContext());
        Trojan.log(LogConstants.NETWORK_TAG, networkType + "");
    }

    private void showBatteryState(Intent intent) {
        int status = intent.getIntExtra("status", 0);
        int health = intent.getIntExtra("health", 0);
        boolean present = intent.getBooleanExtra("present", false);
        int level = intent.getIntExtra("level", 0);
        int scale = intent.getIntExtra("scale", 0);
        int icon_small = intent.getIntExtra("icon-small", 0);
        int plugged = intent.getIntExtra("plugged", 0);
        int voltage = intent.getIntExtra("voltage", 0);
        int temperature = intent.getIntExtra("temperature", 0);
        String technology = intent.getStringExtra("technology");
        String statusResult = "unknown";
        switch (status) {
            case BatteryManager.BATTERY_STATUS_UNKNOWN:
                statusResult = "unknown";
                break;
            case BatteryManager.BATTERY_STATUS_CHARGING:
                statusResult = "charging";
                break;
            case BatteryManager.BATTERY_STATUS_DISCHARGING:
                statusResult = "discharging";
                break;
            case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                statusResult = "not charging";
                break;
            case BatteryManager.BATTERY_STATUS_FULL:
                statusResult = "full";
                break;
        }

        String actionResult = "";
        switch (plugged) {
            case BatteryManager.BATTERY_PLUGGED_AC:
                actionResult = "plugged ac";
                break;
            case BatteryManager.BATTERY_PLUGGED_USB:
                actionResult = "plugged usb";
                break;
        }

        Trojan.log(LogConstants.BATTERY_TAG, statusResult + LogConstants.INTERNAL_SEPERATOR + level + LogConstants.INTERNAL_SEPERATOR +
                scale + LogConstants.INTERNAL_SEPERATOR + actionResult);
    }

}
