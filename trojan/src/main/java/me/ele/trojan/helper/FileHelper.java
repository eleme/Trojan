package me.ele.trojan.helper;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

import me.ele.trojan.config.TrojanConstants;
import me.ele.trojan.utils.AppUtils;
import me.ele.trojan.utils.DateUtils;
import me.ele.trojan.utils.IOUtil;

/**
 * Created by allen on 2017/8/9.
 */
public final class FileHelper {

    public static File getTempDir(Context context) {
        if (context == null) {
            return null;
        }

        File dir = getCacheDirFile(context);
        if (dir == null) {
            dir = getSDDirFile(context);
        }
        return dir;
    }

    public static File getLogDir(Context context) {
        if (context == null) {
            return null;
        }

        File dir = getSDDirFile(context);
        if (dir == null) {
            dir = getCacheDirFile(context);
        }
        return dir;
    }

    public static String getDirName(Context context) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(TrojanConstants.LOG_DIR);
        stringBuilder.append(File.separator);
        stringBuilder.append(AppUtils.getCurProcessName(context));
        return stringBuilder.toString();
    }

    private static File getSDDirFile(Context context) {
        if (context == null || !Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            return null;
        }
        File sdFile = Environment.getExternalStorageDirectory();
        if (sdFile != null) {
            sdFile = new File(sdFile, getDirName(context));
            if (!sdFile.exists() || !sdFile.isDirectory()) {
                sdFile.mkdirs();
            }
        }
        return sdFile;
    }

    private static File getCacheDirFile(Context context) {
        if (context == null) {
            return null;
        }
        File cacheFile;
        try {
            cacheFile = context.getCacheDir();
        } catch (Exception ex) {
            ex.printStackTrace();
            cacheFile = context.getExternalCacheDir();
        }

        if (cacheFile != null) {
            cacheFile = new File(cacheFile, getDirName(context));
            if (!cacheFile.exists() || !cacheFile.isDirectory()) {
                cacheFile.mkdirs();
            }
        }
        return cacheFile;
    }

    /**
     * 将类似2017-11-05的文件命名成2017-11-05-up这样的名称
     *
     * @param file
     */
    public static File renameToUp(File file) {
        if (file == null || !file.exists()) {
            return file;
        }

        StringBuilder renameBuilder = new StringBuilder();
        renameBuilder.append(file.getParentFile().getAbsolutePath());
        renameBuilder.append(File.separator);
        renameBuilder.append(file.getName());
        renameBuilder.append(TrojanConstants.UP);

        File targetFile = new File(renameBuilder.toString());
        if (targetFile.exists()) {
            targetFile.delete();
        }
        if (file.renameTo(targetFile)) {
            return targetFile;
        }
        return file;
    }

    /**
     * It needs to be renamed first (for example, the name of the unified name is 2017-11-05-up) and then compressed.
     *
     * @param sourceFile
     * @return
     * @throws IOException
     */
    public static File save2GZIPFile(File sourceFile, String parentPath) throws IOException {

        if (sourceFile == null || !sourceFile.isFile()) {
            return null;
        }

        if (sourceFile.getName().endsWith(TrojanConstants.GZ)) {
            return sourceFile;
        }

        FileInputStream fis = null;
        FileOutputStream fos = null;
        GZIPOutputStream gos = null;
        try {
            // for mmap log file,must delete the blank content at the end of file
            if (sourceFile.getName().contains(TrojanConstants.MMAP)) {
                deleteBlankContent(sourceFile);
            }
            File gzipFile = createGZIPFile(sourceFile, parentPath);
            fis = new FileInputStream(sourceFile);
            fos = new FileOutputStream(gzipFile, true);
            gos = new GZIPOutputStream(fos);
            byte buffer[] = new byte[1024];
            int count;
            while ((count = fis.read(buffer)) > 0) {
                gos.write(buffer, 0, count);
            }
            if (Build.VERSION.SDK_INT != Build.VERSION_CODES.KITKAT) {
                gos.finish();
            } else {
                gos.flush();
            }
            sourceFile.delete();
            return gzipFile;
        } catch (IOException ex) {
            ex.printStackTrace();
            throw ex;
        } finally {
            IOUtil.closeQuietly(fos);
            IOUtil.closeQuietly(gos);
            IOUtil.closeQuietly(fis);
        }
    }

    private static File createGZIPFile(File sourceFile, String parentPath) throws IOException {
        StringBuilder gzPathBuilder = new StringBuilder();
        if (!TextUtils.isEmpty(parentPath)) {
            gzPathBuilder.append(parentPath);
        } else {
            gzPathBuilder.append(sourceFile.getParentFile().getAbsoluteFile());
        }
        gzPathBuilder.append(File.separator);
        gzPathBuilder.append(getGZIPFileName(sourceFile));

        File file = new File(gzPathBuilder.toString());
        if (!file.exists()) {
            file.createNewFile();
        }
        return file;
    }

    private static String getGZIPFileName(File sourceFile) {
        return sourceFile.getName() + TrojanConstants.GZ;
    }

    public static File preHandleLogFile(File logFile, String renameParentPath, boolean needGZ) {
        if (logFile == null || !logFile.isFile()) {
            return logFile;
        }
        if (System.currentTimeMillis() - logFile.lastModified() > TrojanConstants.FIVE_DAY_MILLS) {
            logFile.delete();
            return null;
        }
        String fileName = logFile.getName();
        if (TextUtils.isEmpty(fileName)) {
            logFile.delete();
            return null;
        }

        if (fileName.endsWith(TrojanConstants.GZ)) {
            return logFile;
        }

        boolean hasContainUp = fileName.endsWith(TrojanConstants.UP);
        boolean hasContainToday = fileName.startsWith(DateUtils.getDate());
        if (hasContainUp || !hasContainToday) {

            if (!hasContainUp) {
                logFile = renameToUp(logFile);
            }

            if (needGZ) {
                try {
                    return FileHelper.save2GZIPFile(logFile, renameParentPath);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (!hasContainToday) {
                        logFile.delete();
                    }
                }
            }
        }
        return null;
    }

    /**
     * Note: this is a time-consuming operation
     * <p>
     * clean up the log files async：delete the overdue log files and compress the valid log files by GZ
     *
     * @param context
     */
    public static List<File> cleanUpLogFile(Context context, String logDirPath) {
        if (context == null) {
            return null;
        }
        final List<File> allLogFiles = new LinkedList<>();

        File tempDir = getTempDir(context);
        if (tempDir != null) {
            File[] files = tempDir.listFiles();
            if (files != null && files.length > 0) {
                for (File logFile : files) {
                    if (logFile == null || !logFile.isFile()) {
                        continue;
                    }
                    allLogFiles.add(logFile);
                }
            }
        }

        if (!TextUtils.isEmpty(logDirPath)) {
            File sdDir = new File(logDirPath);
            File[] files = sdDir.listFiles();
            if (files != null && files.length > 0) {
                for (File logFile : files) {
                    if (logFile == null || !logFile.isFile()) {
                        continue;
                    }
                    allLogFiles.add(logFile);
                }
            }
        }

        if (allLogFiles.size() == 0) {
            return null;
        }

        Set<String> logPathSet = new HashSet<>();
        for (File logFile : allLogFiles) {
            if (logFile == null || !logFile.isFile()) {
                continue;
            }
            File gzFile = preHandleLogFile(logFile, logDirPath, true);
            if (gzFile != null) {
                logPathSet.add(gzFile.getAbsolutePath());
            }
        }

        final List<File> gzFileList = new LinkedList<>();
        for (String logPath : logPathSet) {
            File gzFile = new File(logPath);
            if (gzFile.isFile()) {
                gzFileList.add(gzFile);
            }
        }

        return gzFileList;
    }

    public static List<File> renameToUpAllIfNeed(Context context, String writeFileName, String logDirPath) {
        File tempDir = getTempDir(context);
        if (tempDir == null) {
            return null;
        }

        final File[] files = tempDir.listFiles();
        if (files == null || files.length == 0) {
            return null;
        }

        final List<File> logFileList = new LinkedList<>();
        for (File logFile : files) {
            if (logFile == null || !logFile.isFile()) {
                continue;
            }
            String fileName = logFile.getName();

            // delete the log file when fileName is empty
            if (TextUtils.isEmpty(fileName)) {
                logFile.delete();
                continue;
            }
            // skip the writing file
            if (writeFileName.equals(fileName)) {
                continue;
            }

            logFile = preHandleLogFile(logFile, logDirPath, false);
            if (logFile != null) {
                logFileList.add(logFile);
            }
        }
        return logFileList;
    }

    public static void deleteBlankContent(File file) {
        if (file == null || !file.isFile()) {
            return;
        }
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(file, "rw");
            long len = raf.length();
            if (len <= 3) {
                return;
            }
            long pos = len - 1;
            while (pos > 0) {
                --pos;
                raf.seek(pos);
                if (raf.readByte() == '\n') {
                    break;
                }
            }
            raf.getChannel().truncate(pos > 0 ? pos + 1 : pos).close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtil.closeQuietly(raf);
            /**
             * Get the free space of sdcard in Mb
             *
             * @return
             */
        }
    }

    public static long getSDFreeSize() {
        try {
            File path = Environment.getExternalStorageDirectory();
            StatFs sf = new StatFs(path.getPath());
            long blockSize = sf.getBlockSize();
            long freeBlocks = sf.getAvailableBlocks();
            return (freeBlocks * blockSize) / 1024 / 1024;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return TrojanConstants.MIN_SDCARD_FREE_SPACE_MB;
    }

    /**
     * determine whether the remaining space of Sdcard is greater than 50
     *
     * @return
     */
    public static boolean isSDEnough() {
        return getSDFreeSize() >= TrojanConstants.MIN_SDCARD_FREE_SPACE_MB;
    }
}