package me.ele.trojan.helper;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import me.ele.trojan.config.TrojanConstants;
import me.ele.trojan.log.Logger;
import me.ele.trojan.utils.AppUtils;
import me.ele.trojan.utils.DateUtils;
import me.ele.trojan.utils.IOUtil;

/**
 * Created by allen on 2017/8/9.
 */
public final class FileHelper {

    public static File getLogDir(Context context) {
        if (context == null) {
            return null;
        }
        File dir = null;
        String cacheDirPath = getSDDirPath();
        if (!TextUtils.isEmpty(cacheDirPath)) {
            StringBuilder pathBuilder = new StringBuilder();
            pathBuilder.append(cacheDirPath)
                    .append(File.separator)
                    .append(getDirName(context))
                    .append(File.separator)
                    .append(AppUtils.getCurProcessName(context));
            dir = new File(pathBuilder.toString());
        }
        if (dir == null) {
            StringBuilder pathBuilder = new StringBuilder();
            pathBuilder.append(getCacheDir(context).getAbsolutePath())
                    .append(File.separator)
                    .append(TrojanConstants.LOG_DIR)
                    .append(File.separator)
                    .append(AppUtils.getCurProcessName(context));
            dir = new File(pathBuilder.toString());
        }
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    private static String getSDDirPath() {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            return Environment.getExternalStorageDirectory().getAbsolutePath();
        }
        return null;
    }

    private static String getDirName(Context context) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(context.getApplicationContext().getPackageName());
        stringBuilder.append(TrojanConstants.TROJAN_LOG);
        return stringBuilder.toString();
    }

    private static File getCacheDir(Context context) {
        try {
            return context.getExternalCacheDir();
        } catch (Exception ex) {
            return context.getCacheDir();
        }
    }

    /**
     * 将类似2017-11-05的文件命名成2017-11-05-up这样的名称
     *
     * @param file
     */
    public static File renameToUp(File file) {
        if (file == null || !file.exists()) {
            return null;
        }

        StringBuilder rename = new StringBuilder();
        rename.append(file.getParentFile().getPath());
        rename.append(File.separator);
        rename.append(file.getName());
        rename.append(TrojanConstants.UP);
        File targetFile = new File(rename.toString());
        if (targetFile.exists()) {
            targetFile.delete();
        }
        if (file.renameTo(targetFile)) {
            return targetFile;
        }
        return null;
    }

    /**
     * 这里没有考虑文件权限的问题!
     * 这里需要先重命名(比如统一重全名为2017-11-05-up这样的名字)，然后再压缩
     *
     * @param sourceFile
     * @return
     * @throws IOException
     */
    public static File save2GZIPFile(File sourceFile) throws IOException {

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
            fis = new FileInputStream(sourceFile);
            File gzipFile = createGZIPFile(sourceFile);
            fos = new FileOutputStream(gzipFile);
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

    private static File createGZIPFile(File sourceFile) throws IOException {
        StringBuilder gzPathBuilder = new StringBuilder();
        gzPathBuilder.append(sourceFile.getParentFile().getAbsoluteFile());
        gzPathBuilder.append(File.separator);
        gzPathBuilder.append(getGZIPFileName(sourceFile));

        File file = new File(gzPathBuilder.toString());
        if (file.exists()) {
            file.delete();
        }
        file.createNewFile();
        return file;
    }

    private static String getGZIPFileName(File sourceFile) {
        return sourceFile.getName() + TrojanConstants.GZ;
    }

    /**
     * Note: this is a time-consuming operation
     * <p>
     * clean up the log files async：delete the overdue log files and compress the valid log files by GZ
     *
     * @param context
     */
    public static List<File> cleanUpLogFile(Context context, String logDir) {
        if (context == null || TextUtils.isEmpty(logDir)) {
            return null;
        }
        File dir = new File(logDir);
        if (dir == null || !dir.exists()) {
            return null;
        }
        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            return null;
        }
        final List<File> gzFileList = new LinkedList<>();
        String todayDate = DateUtils.getDate();
        long currentTime = System.currentTimeMillis();
        for (File logFile : files) {
            if (logFile == null || !logFile.isFile()) {
                continue;
            }
            // delete the overdue log files
            if (currentTime - logFile.lastModified() > TrojanConstants.FIVE_DAY_MILLS) {
                logFile.delete();
                continue;
            }
            String fileName = logFile.getName();
            if (fileName.endsWith(TrojanConstants.GZ)) {
                gzFileList.add(logFile);
                continue;
            }
            // except today log file, need compress the log files if it is not gz file
            if (!fileName.contains(todayDate)) {
                try {
                    File targetFile = logFile;
                    // rename the log file
                    if (!fileName.contains(TrojanConstants.UP)) {
                        targetFile = FileHelper.renameToUp(logFile);
                    }
                    // compress the log file use GZ
                    File gzFile = FileHelper.save2GZIPFile(targetFile);
                    if (gzFile != null && gzFile.isFile()) {
                        gzFileList.add(gzFile);
                    }
                } catch (IOException e) {
                    Logger.e("FileHelper-->cleanUpLogFile:" + e);
                    e.printStackTrace();
                }
            }
        }
        return gzFileList;
    }

    public static List<File> renameToUpAllLogFileIfNecessary(String user, String logDir, String writeFileName) {
        if (TextUtils.isEmpty(logDir) || TextUtils.isEmpty(writeFileName)) {
            return null;
        }
        final File dir = new File(logDir);
        if (dir == null || !dir.isDirectory()) {
            return null;
        }
        final File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            return null;
        }
        final long currentTime = System.currentTimeMillis();
        final List<File> gzFileList = new LinkedList<>();
        for (File logFile : files) {
            if (logFile == null || !logFile.isFile()) {
                continue;
            }
            // delete the overdue log files
            if (currentTime - logFile.lastModified() > TrojanConstants.FIVE_DAY_MILLS) {
                logFile.delete();
                continue;
            }

            String fileName = logFile.getName();
            if (TextUtils.isEmpty(fileName)) {
                continue;
            }
            if (fileName.equals(writeFileName)) {
                continue;
            }
            if (fileName.contains(TrojanConstants.GZ)) {
                gzFileList.add(logFile);
                continue;
            }
            if (!fileName.contains(TrojanConstants.UP)) {
                File renameFile = FileHelper.renameToUp(logFile);
                if (renameFile != null && renameFile.isFile()) {
                    logFile = renameFile;
                }
            }
            gzFileList.add(logFile);
        }
        return gzFileList;
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
        }
    }

}