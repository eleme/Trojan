package me.ele.trojan.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

/**
 * Created by michaelzhong on 2017/3/29.
 */

public class NetworkUtils {

    public static final int NETWORK_NONE = 0;
    public static final int NETWORK_WIFI = 1;
    public static final int NETWORK_2G = 2;
    public static final int NETWORK_3G = 3;
    public static final int NETWORK_4G = 4;
    public static final int NETWORK_UNKNOWN_MOBILE = 5;

    public static int getNetworkType(Context context) {
        if (null == context) {
            return NETWORK_NONE;
        }
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (null == connectivityManager) {
            return NETWORK_NONE;
        }

        final NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        if (null == activeNetInfo || !activeNetInfo.isAvailable()) {
            return NETWORK_NONE;
        } else if (activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            return NETWORK_WIFI;
        } else if (activeNetInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
            final NetworkInfo.State state = activeNetInfo.getState();
            final String subTypeName = activeNetInfo.getSubtypeName();
            if (null != state) {
                switch (activeNetInfo.getSubtype()) {
                    case TelephonyManager.NETWORK_TYPE_GPRS:
                    case TelephonyManager.NETWORK_TYPE_CDMA:
                    case TelephonyManager.NETWORK_TYPE_EDGE:
                    case TelephonyManager.NETWORK_TYPE_1xRTT:
                    case TelephonyManager.NETWORK_TYPE_IDEN:
                        return NETWORK_2G;
                    case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    case TelephonyManager.NETWORK_TYPE_UMTS:
                    case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    case TelephonyManager.NETWORK_TYPE_HSDPA:
                    case TelephonyManager.NETWORK_TYPE_HSUPA:
                    case TelephonyManager.NETWORK_TYPE_HSPA:
                    case TelephonyManager.NETWORK_TYPE_EVDO_B:
                    case TelephonyManager.NETWORK_TYPE_EHRPD:
                    case TelephonyManager.NETWORK_TYPE_HSPAP:
                        return NETWORK_3G;
                    case TelephonyManager.NETWORK_TYPE_LTE:
                        return NETWORK_4G;
                    default:
                        if (subTypeName.equalsIgnoreCase("TD-SCDMA")
                                || subTypeName.equalsIgnoreCase("WCDMA")
                                || subTypeName.equalsIgnoreCase("CDMA2000")) {
                            return NETWORK_3G;
                        } else {
                            return NETWORK_UNKNOWN_MOBILE;
                        }
                }
            }
        }
        return NETWORK_NONE;
    }

    /**
     * @param context
     * @return
     */
    public static boolean isNetworkAvailable(Context context) {
        if (context == null) {
            return false;
        }
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getApplicationContext().getSystemService(
                        Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return false;
        }

        NetworkInfo networkinfo = connectivityManager.getActiveNetworkInfo();

        return networkinfo != null && networkinfo.isAvailable();
    }

}
