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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
        File cacheFile = null;
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

    public static boolean isFileExist(File file) {
        return file != null && file.exists() && file.isFile();
    }

    public static boolean isFileFormat(File file) {
        final String pattern = "^(\\d{4})-(\\d{2})-(\\d{2})";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(file.getName());
        return m.find();
    }

    public static boolean isFileOverdue(File file) {
        return System.currentTimeMillis() - file.lastModified() > TrojanConstants.FIVE_DAY_MILLS;
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

    private static File createGZIPFile(File sourceFile, String parentPath) throws IOException {
        StringBuilder gzPathBuilder = new StringBuilder();
        if (!TextUtils.isEmpty(parentPath)) {
            gzPathBuilder.append(parentPath);
        } else {
            gzPathBuilder.append(sourceFile.getParentFile().getAbsoluteFile());
        }
        gzPathBuilder.append(File.separator);
        gzPathBuilder.append(getGZIPFileName(sourceFile));

        File gzFile = new File(gzPathBuilder.toString());
        if (gzFile.exists()) {
            gzFile.delete();
        }
        gzFile.createNewFile();
        return gzFile;
    }

    private static String getGZIPFileName(File sourceFile) {
        final String pattern = "^(\\d{4})-(\\d{2})-(\\d{2})";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(sourceFile.getName());
        if (m.find()) {
            return m.group(0) + TrojanConstants.GZ;
        }
        return sourceFile.getName() + TrojanConstants.GZ;
    }

    public static File handleLogFile(File logFile, String renameParentPath, boolean needGZ) {
        if (!isFileExist(logFile)) {
            return null;
        }
        if (!isFileFormat(logFile) || isFileOverdue(logFile)) {
            logFile.delete();
            return null;
        }

        final String fileName = logFile.getName();

        if (fileName.endsWith(TrojanConstants.GZ)) {
            return logFile;
        }

        boolean hasContainUp = fileName.endsWith(TrojanConstants.UP);
        boolean hasContainToday = fileName.startsWith(DateUtils.getDate());
        if (needGZ && (hasContainUp || !hasContainToday)) {
            try {
                File gzFile = FileHelper.save2GZIPFile(logFile, renameParentPath);
                return gzFile;
            } catch (IOException e) {
                e.printStackTrace();
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
            allLogFiles.addAll(filterLogFile(tempDir.listFiles(), null));
        }

        if (!TextUtils.isEmpty(logDirPath)) {
            allLogFiles.addAll(filterLogFile(new File(logDirPath).listFiles(), null));
        }

        if (allLogFiles.size() == 0) {
            return null;
        }

        Collections.sort(allLogFiles, new Comparator<File>() {
            @Override
            public int compare(File lhs, File rhs) {
                return (int) (rhs.lastModified() - lhs.lastModified());
            }
        });

        Set<String> logPathSet = new HashSet<>();
        for (File logFile : allLogFiles) {
            File gzFile = handleLogFile(logFile, logDirPath, true);
            if (gzFile != null) {
                logPathSet.add(gzFile.getAbsolutePath());
            }
        }

        final List<File> gzFileList = new LinkedList<>();
        for (String logPath : logPathSet) {
            gzFileList.add(new File(logPath));
        }

        return gzFileList;
    }

    private static List<File> filterLogFile(File[] files, String dateTime) {
        final List<File> logFileList = new LinkedList<>();
        if (files == null || files.length == 0) {
            return logFileList;
        }

        final boolean isToday = !TextUtils.isEmpty(dateTime) && dateTime.equals(DateUtils.getDate());

        for (File file : files) {
            if (!isFileExist(file)) {
                continue;
            }
            if (!isFileFormat(file)) {
                continue;
            }
            if (TextUtils.isEmpty(dateTime)) {
                logFileList.add(file);
                continue;
            }
            if (file.getName().startsWith(dateTime)
                    && (!isToday || file.getName().contains(TrojanConstants.UP))) {
                logFileList.add(file);
            }
        }
        return logFileList;
    }

    /**
     * Note: this is a time-consuming operation
     * <p>
     * clean up the log files async：delete the overdue log files and compress the valid log files by GZ
     *
     * @param context
     * @param logDirPath
     * @param dateTime
     */
    public static File getLogFileByDate(Context context, String logDirPath, String dateTime) {
        if (context == null || TextUtils.isEmpty(logDirPath) || TextUtils.isEmpty(dateTime)) {
            return null;
        }

        final List<File> allLogFiles = new LinkedList<>();

        File tempDir = getTempDir(context);
        if (tempDir != null) {
            allLogFiles.addAll(filterLogFile(tempDir.listFiles(), dateTime));
        }

        if (!TextUtils.isEmpty(logDirPath)) {
            allLogFiles.addAll(filterLogFile(new File(logDirPath).listFiles(), dateTime));
        }

        if (allLogFiles.size() == 0) {
            return null;
        }

        Collections.sort(allLogFiles, new Comparator<File>() {
            @Override
            public int compare(File lhs, File rhs) {
                return (int) (rhs.lastModified() - lhs.lastModified());
            }
        });

        File gzFile = null;

        for (File logFile : allLogFiles) {
            File tempFile = handleLogFile(logFile, logDirPath, true);
            if (tempFile != null) {
                gzFile = tempFile;
            }
        }

        return gzFile;
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
            if (!isFileExist(logFile)) {
                continue;
            }
            if (!isFileFormat(logFile) || isFileOverdue(logFile)) {
                logFile.delete();
                continue;
            }

            final String fileName = logFile.getName();

            // skip the writing file
            if (writeFileName.equals(fileName) || fileName.contains(TrojanConstants.UP)) {
                continue;
            }

            renameToUp(logFile);
        }
        return logFileList;
    }

    public static void deleteBlankContent(File file) {
        if (!isFileExist(file)) {
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
                if (raf.readByte() == '>') {
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

}