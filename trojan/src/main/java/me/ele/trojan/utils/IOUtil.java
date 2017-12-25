package me.ele.trojan.utils;

import java.io.Closeable;

/**
 * Created by michaelzhong on 2017/4/11.
 */

public final class IOUtil {

    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (RuntimeException ex) {
                throw ex;
            } catch (Exception ignored) {

            }
        }
    }

}
