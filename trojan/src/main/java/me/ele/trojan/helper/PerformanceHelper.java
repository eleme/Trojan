package me.ele.trojan.helper;

import android.os.Build;
import android.os.Debug;

import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import me.ele.trojan.Trojan;
import me.ele.trojan.config.LogConstants;
import me.ele.trojan.config.TrojanConstants;

/**
 * Created by michaelzhong on 2018/3/16.
 */
public class PerformanceHelper {

    private static final DecimalFormat decimalFormat = new DecimalFormat("######0.00");

    public static void recordThread() {
        Map<String, Integer> threadNumMap = new LinkedHashMap<>();
        Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
        for (Thread thread : threadSet) {
            String threadName = thread.getName().replaceAll("#?_?-?\\d+", "");
            if (threadNumMap.containsKey(threadName)) {
                threadNumMap.put(threadName, threadNumMap.get(threadName) + 1);
            } else {
                threadNumMap.put(threadName, 1);
            }
        }
        List<String> msgList = new LinkedList<>();
        msgList.add(String.valueOf(threadSet.size()));
        for (Map.Entry<String, Integer> entry : threadNumMap.entrySet()) {
            if (entry.getValue() >= TrojanConstants.MIN_THREAD_NUM) {
                msgList.add(entry.getKey() + ":" + entry.getValue());
            }
        }
        Trojan.log(TrojanConstants.TAG_THREAD, msgList);
    }

    public static void recordMemory() {
        Runtime runtime = Runtime.getRuntime();
        float dalvikMem = (float) ((runtime.totalMemory() - runtime.freeMemory()) * 1.0 / TrojanConstants.FORMAT_MB);
        float nativeMem = (float) (Debug.getNativeHeapAllocatedSize() * 1.0 / TrojanConstants.FORMAT_MB);

        List<String> msgList = new LinkedList<>();
        msgList.add(String.valueOf(decimalFormat.format(dalvikMem + nativeMem)) + TrojanConstants.MB);
        msgList.add(String.valueOf(decimalFormat.format(dalvikMem)) + TrojanConstants.MB);
        msgList.add(String.valueOf(decimalFormat.format(nativeMem)) + TrojanConstants.MB);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Debug.MemoryInfo memoryInfo = new Debug.MemoryInfo();
                Debug.getMemoryInfo(memoryInfo);
                float stackSize = Float.parseFloat(memoryInfo.getMemoryStat("summary.stack"));
                msgList.add(String.valueOf(decimalFormat.format(stackSize / TrojanConstants.FORMAT_KB)) + TrojanConstants.MB);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Trojan.log(LogConstants.MEMORY_TAG, msgList);
    }

}
