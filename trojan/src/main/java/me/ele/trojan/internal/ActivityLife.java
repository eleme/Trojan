package me.ele.trojan.internal;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;

import me.ele.trojan.Trojan;
import me.ele.trojan.config.LogConstants;

/**
 * Created by michaelzhong on 17/1/20.
 */

public class ActivityLife {

    public static void init(Context context) {
        ((Application) context.getApplicationContext()).registerActivityLifecycleCallbacks(
                new Application.ActivityLifecycleCallbacks() {
                    @Override
                    public void onActivityCreated(Activity activity, Bundle bundle) {
                        Trojan.log(LogConstants.ACTIVITY_LIFE_TAG, android.os.Process.myPid() + "#*" + activity.getClass().getName() + (bundle == null ? "#*onCreate:Bundle=null" : "#*onCreate:Bundle!=null"));
                    }

                    @Override
                    public void onActivityStarted(Activity activity) {
                        Trojan.log(LogConstants.ACTIVITY_LIFE_TAG, android.os.Process.myPid() + "#*" + activity.getClass().getName() + "#*onStart");
                    }

                    @Override
                    public void onActivityResumed(Activity activity) {
                        Trojan.log(LogConstants.ACTIVITY_LIFE_TAG, android.os.Process.myPid() + "#*" + activity.getClass().getName() + "#*onResume");
                    }

                    @Override
                    public void onActivityPaused(Activity activity) {
                        Trojan.log(LogConstants.ACTIVITY_LIFE_TAG, android.os.Process.myPid() + "#*" + activity.getClass().getName() + "#*onPause");
                    }

                    @Override
                    public void onActivityStopped(Activity activity) {
                        Trojan.log(LogConstants.ACTIVITY_LIFE_TAG, android.os.Process.myPid() + "#*" + activity.getClass().getName() + "#*onStop");
                    }

                    @Override
                    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
                        Trojan.log(LogConstants.ACTIVITY_LIFE_TAG, android.os.Process.myPid() + "#*" + activity.getClass().getName() + (bundle == null ? "#*onSaveInstanceState:Bundle=null" : "#*onSaveInstanceState:Bundle!=null"));
                    }

                    @Override
                    public void onActivityDestroyed(Activity activity) {
                        Trojan.log(LogConstants.ACTIVITY_LIFE_TAG, android.os.Process.myPid() + "#*" + activity.getClass().getName() + "#*onDestroy");
                    }
                }
        );
    }
}
