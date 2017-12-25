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
                        Trojan.log(LogConstants.ACTIVITY_LIFE_TAG, "onCreate:" + activity.getClass().getName() + " Bundle = " + bundle);
                    }

                    @Override
                    public void onActivityStarted(Activity activity) {
                        Trojan.log(LogConstants.ACTIVITY_LIFE_TAG, "onStart:" + activity.getClass().getName());
                    }

                    @Override
                    public void onActivityResumed(Activity activity) {
                        Trojan.log(LogConstants.ACTIVITY_LIFE_TAG, "onResume:" + activity.getClass().getName());
                    }

                    @Override
                    public void onActivityPaused(Activity activity) {
                        Trojan.log(LogConstants.ACTIVITY_LIFE_TAG, "onPause:" + activity.getClass().getName());
                    }

                    @Override
                    public void onActivityStopped(Activity activity) {
                        Trojan.log(LogConstants.ACTIVITY_LIFE_TAG, "onStop:" + activity.getClass().getName());
                    }

                    @Override
                    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
                        Trojan.log(LogConstants.ACTIVITY_LIFE_TAG, "onSaveInstanceState:" + activity.getClass().getName());
                    }

                    @Override
                    public void onActivityDestroyed(Activity activity) {
                        Trojan.log(LogConstants.ACTIVITY_LIFE_TAG, "onDestroy:" + activity.getClass().getName());
                    }
                }
        );
    }
}
