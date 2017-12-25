package me.ele.trojan.utils;

import com.google.gson.Gson;

import java.lang.reflect.Type;

/**
 * Created by michaelzhong on 2017/8/18.
 */

public class GsonUtils {

    private static final Gson GSON = new Gson();

    public static String toJson(Object obj) {
        if (null == obj) {
            return "";
        }
        return GSON.toJson(obj);
    }

    public static Object fromJson(String json, Type classType) {
        if (null == json) {
            return null;
        }
        return GSON.fromJson(json, classType);
    }
}
