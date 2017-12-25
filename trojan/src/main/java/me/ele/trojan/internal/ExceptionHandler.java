package me.ele.trojan.internal;

import me.ele.trojan.Trojan;
import me.ele.trojan.config.LogConstants;

/**
 * Created by Eric on 17/1/20.
 */

public class ExceptionHandler {

    public static void init() {
        final Thread.UncaughtExceptionHandler handler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable throwable) {
                Trojan.log(LogConstants.EXCEPTION_TAG, "Exception occurred in Thread " + thread.getName());
                Trojan.log(LogConstants.EXCEPTION_TAG, "Exception type: " + throwable.getClass().getName());
                Trojan.log(LogConstants.EXCEPTION_TAG, "Exception message: " + throwable.getMessage());
                StackTraceElement[] stackTraceElements = throwable.getStackTrace();
                for (StackTraceElement stackTraceElement : stackTraceElements) {
                    StringBuilder stackBuilder = new StringBuilder();
                    stackBuilder.append(" ")
                            .append(stackTraceElement.getClassName())
                            .append(" ")
                            .append(stackTraceElement.getMethodName())
                            .append("(")
                            .append(stackTraceElement.getFileName())
                            .append(":")
                            .append(stackTraceElement.getLineNumber())
                            .append(")");
                    Trojan.log(LogConstants.EXCEPTION_TAG, stackBuilder.toString());
                }
                handler.uncaughtException(thread, throwable);
            }
        });
    }
}
