package me.ele.trojan.demo;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by panda on 2017/12/19.
 */

public class RecordRequestInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        HttpRecordHelper.recordRequest(request);
        return chain.proceed(request);
    }
}
