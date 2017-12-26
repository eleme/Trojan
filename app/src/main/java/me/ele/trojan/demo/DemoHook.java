package me.ele.trojan.demo;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import me.ele.lancet.base.Origin;
import me.ele.lancet.base.annotations.Insert;
import me.ele.lancet.base.annotations.NameRegex;
import me.ele.lancet.base.annotations.Proxy;
import me.ele.lancet.base.annotations.TargetClass;
import me.ele.trojan.Trojan;
import me.ele.trojan.log.Logger;
import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * Created by michaelzhong on 2017/12/26.
 */

public class DemoHook {

    @Proxy("e")
    @TargetClass("android.util.Log")
    public static int e(String tag, String msg) {
        Trojan.log(tag, msg);
        return (int) Origin.call();
    }


    @TargetClass("okhttp3.OkHttpClient")
    @NameRegex("okhttp3/RealCall")
    @Proxy("interceptors")
    public List<Interceptor> networkInterceptors() {
        // record the http request
        Logger.i("networkInterceptors");
        List<Interceptor> interceptors = (List<Interceptor>) Origin.call();
        List<Interceptor> newList = new ArrayList<>(interceptors.size() + 1);
        newList.addAll(interceptors);
        newList.add(new RecordRequestInterceptor());
        return newList;
    }

    @Insert("getResponseWithInterceptorChain")
    @TargetClass(value = "okhttp3.RealCall")
    private Response getResponseWithInterceptorChain() throws Throwable {
        // record the http response
        Logger.i("getResponseWithInterceptorChain");
        Response response = (Response) Origin.call();
        try {
            HttpRecordHelper.recordResponse(response);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return response;
    }

}
