//
// Created by Allen on 2017/11/7.
//
#include "LogWriter.h"
#include "ErrInfo.h"
#include <iostream>
#include <sys/file.h>


LogWriter::LogWriter() {

}

ErrInfo *LogWriter::initMmap(JNIEnv *env, std::string basicInfo, std::string logDir) {
    //首先，记录基本信息
    this->basicInfo = basicInfo;
    this->logDir = logDir;
    //然后，为写入做准备,包括:对没有压缩的文件进行压缩操作;以及mmap操作
    //考虑了一下，还是把压缩操作放在Java层好了，这里就只准备mmap

    buildDate = getDate();
    //加入后缀,防止普通IO和mmap方式同用一个文件而导致错误
    filePath = logDir + "/" + buildDate + "-mmap";

    bool fileExists = true;

    this->fd = open(filePath.c_str(), O_RDWR | O_CREAT, (mode_t) 0600);

    if (fd == -1) {
        return new ErrInfo(OPEN_EXIT, "Error opening file");
    }

    if (lock(fd) < 0) {
        close(fd);
        return new ErrInfo(LOCK_EXIT, "Error locked file");
    }

    struct stat fileStat;
    if (fstat(fd, &fileStat) == -1) {
        unlock(fd);
        close(fd);
        return new ErrInfo(FSTAT_EXIT, "Error fstat file");
    }

    fileSize = fileStat.st_size;
    this->pageSize = sysconf(_SC_PAGE_SIZE);

    logPageSize = ALLOC_PAGE_NUM * pageSize;

    //如果fileSize为0，则表示是首次创建文件
    //如果fileSize不是pageSize的整数倍，则让其补全到pageSize的整数倍
    if (fileSize < pageSize || fileSize % pageSize != 0) {

        fileExists = fileSize > 0;

        off_t increaseSize = logPageSize - fileSize % logPageSize;

        if (ftruncate(fd, fileSize + increaseSize) == -1) {
            unlock(fd);
            close(fd);
            return new ErrInfo(LSEEK_EXIT, "Error when calling ftruncate() to stretch the file");
        }
        fileSize += increaseSize;


        if (lseek(fd, fileSize - 1, SEEK_SET) == -1) {
            unlock(fd);
            close(fd);
            return new ErrInfo(LSEEK_EXIT, "Error calling lseek() to stretch the file");
        }


        //TODO 其实可以考虑在这里写入一个特殊的结束符
        if (write(fd, "", 1) == -1) {
            unlock(fd);
            close(fd);
            return new ErrInfo(WRITE_EXIT, "Error writing last byte of the file");
        }

    }

    //之后，跳转到最后一个页面的起始位置 ，这里当然就需要利用offset了
    ////////////////////////////////////////////////////////////////////////////
    off_t pageOffsetNum = fileSize / logPageSize - 1;
    if (pageOffsetNum < 0) {
        pageOffsetNum = 0;
    }
    pageOffset = pageOffsetNum * logPageSize;

    //TODO 每次mmap的长度为logPageSize,目前暂时定为一个页面，但是后面这个值肯定需要修改，比如修改为600K
    map = mmap(0, logPageSize, PROT_READ | PROT_WRITE, MAP_SHARED, fd, pageOffset);
    ////////////////////////////////////////////////////////////////////////////////


    if (map == MAP_FAILED) {
        unlock(fd);
        close(fd);
        return new ErrInfo(MMAP_EXIT, "Error mmaping the file");
    }

    recordPtr = (char *) map;
    for (int i = 0; i < logPageSize - 1; i++) {  //TODO 这里是不是要在logPageSize-1和logPageSize中选择一个呢?
        recordPtr++;
    }

    bool findFlag = false;
    size_t i;
    for (i = 0; i < logPageSize - 1; i++) {
        //找到第一个'\n'即停止查找,如果没找到，则说明这个页面还是空白的，正好回到页面开始处
        if (*recordPtr == '\n') {
            findFlag = 1;
            break;
        }
        --recordPtr;
    }


    if (findFlag) {
        restSize = i;
    } else {
        restSize = i + 1;
    }

    //如果文件是首次创建，则需要写入基本信息
    if (!fileExists) {
        return writeLog(env, basicInfo.c_str(), encryptBasic);
    }

    return nullptr;
}

/**
 * 注意:这个是在子线程执行的
 * @param basicInfo
 * @param logDir
 */

//TODO 抛出异常的行为还是放在jni方法中比较好，否则都带上JNIEnv的话不利于跨平台应用
ErrInfo *LogWriter::init(JNIEnv *env, std::string basicInfo, std::string logDir,
                         bool encryptBasic, std::string encryptMethod, std::string key) {
    this->encryptBasic = encryptBasic;
    initEncrypt(encryptMethod, key);
    return initMmap(env, basicInfo, logDir);
}

LogWriter::~LogWriter() {

    //now write it to disk
    if (msync(map, logPageSize, MS_SYNC) == -1) {
        perror("Could not sync the file to disk");
    }

    //Don't forget to free mmapped memory.
    if (munmap(map, logPageSize) == -1) {
        unlock(fd);
        close(fd);
        perror("Error un-mmaping the file");
        exit(EXIT_FAILURE);
    }
    unlock(fd);
    //Un-mapping doesn't close the file, so we still need to do that.
    close(fd);

}

void LogWriter::initEncrypt(std::string encryptMethod, std::string key) {
    //密钥不能为空
    if (key.empty()) {
        return;
    }
    if (encryptMethod.empty()) {
        return;
    }
    if (encryptMethod == AES_NAME) {
        //TODO 后面添加上AES加密
    } else if (encryptMethod == DES_NAME) {
        symEncrypt = new DESEncrypt(key);
    } else {
        //后面加上TEA加密
    }
}

ErrInfo *LogWriter::writeLog(JNIEnv *env, const char *logMsg, bool crypt) {
    //TODO 如果是写密文的话，就必须自己传递textSize,因为密文中间可能含有\0,这样的话strlen就获取不到真实长度
    size_t textSize = strlen(logMsg);

    if (!crypt || symEncrypt == NULL) {
        return writeLog(env, logMsg, textSize);
    } else {
        //TODO 这里暂时还不明白为何要一个比textSize更大的数组
        size_t cipherLen;
        if (textSize % 8 == 0) {
            cipherLen = textSize;
        } else {
            cipherLen = textSize + (8 - textSize % 8);
        }
        char cipher[cipherLen];
        memset(cipher, 0, cipherLen);

        symEncrypt->encrypt(logMsg, cipher);
        ////////////////////////////////////////////

        //获得密文后，需要在前面加上"Cipher_Start"这个密文开始标志,在后面加上"Cipher_End"这个密文结束标志,即进行字符串拼接

        writeLog(env, CIPHER_START, strlen(CIPHER_START));

        ErrInfo *info = writeLog(env, cipher, cipherLen);

        writeLog(env, CIPHER_END, strlen(CIPHER_END));

        return info;
    }
}

ErrInfo *LogWriter::writeLog(JNIEnv *env, const char *logMsg, size_t textSize) {
    size_t len;
    const char *msgPtr = logMsg;
    while (1) {

        len = textSize <= restSize ? textSize : restSize;

        if (len > 0) {
            memcpy(recordPtr, msgPtr, len);

            recordPtr = recordPtr + len;
            msgPtr = msgPtr + len;
        }


        //说明在这次写入之前，restSize小于textSize,所以需要再开辟一页
        if (len < textSize) {

            textSize -= len;

            unixMunmap(fd, map, logPageSize);

            fileSize += logPageSize;

            //扩展文件大小
            if (ftruncate(fd, fileSize) == -1) {
                unlock(fd);
                close(fd);
                return new ErrInfo(LSEEK_EXIT, "Error calling ftruncate() to stretch file");
            }

            //移动到文件末尾
            if (lseek(fd, logPageSize - 1, SEEK_CUR) == -1) {
                unlock(fd);
                close(fd);
                return new ErrInfo(LSEEK_EXIT, "Error calling lseek() to stretch the file");
            }

            //在文件末尾写入一个字符，达到扩展文件大小的目的
            if (write(fd, "", 1) == -1) {
                unlock(fd);
                close(fd);
                return new ErrInfo(WRITE_EXIT, "Error writing last byte of the file");
            }

            pageOffset += logPageSize;

            map = mmap(0, logPageSize, PROT_READ | PROT_WRITE, MAP_SHARED, fd, pageOffset);

            if (map == MAP_FAILED) {
                unlock(fd);
                close(fd);
                return new ErrInfo(MMAP_EXIT, "Error mmaping the file");
            }

            recordPtr = (char *) map;

            restSize = logPageSize;

        } else {
            restSize -= len;
            break;
        }
    }

    return nullptr;
}


void LogWriter::refreshBasicInfo(JNIEnv *env, std::string basicInfo) {
    //TODO 这个会不会涉及浅copy和深copy的问题
    this->basicInfo = basicInfo;
}

ErrInfo *LogWriter::closeAndRenew(JNIEnv *env) {

    //还是改成复制一个文件出来更好,比如将2017-11-05复制出一个2017-11-05-up的文件出来
    //首先取消映射
    ErrInfo *errInfo = unixMunmap(fd, map, logPageSize);
    if (errInfo != NULL) {
        return errInfo;
    }
    unlock(fd);
    //然后关闭文件
    close(fd);
    //然后重命名文件
    std::string upFilePath = logDir + "/" + buildDate + "-mmap-up";
    //为了预防文件存在的情况，所以进行一次删除操作
    remove(upFilePath.c_str());
    //为防止日志文件被删除，先检查一下日志文件是否存在
    if (access(filePath.c_str(), 0) == 0) {
        rename(filePath.c_str(), upFilePath.c_str());
    }
    //最后重新初始化，即新建文件并映射
    //return init(env, basicInfo, logDir);
    return initMmap(env, basicInfo, logDir);
}

std::string LogWriter::getDate() {
    time_t now = time(0);
    tm localTime = *localtime(&now);
    std::string *date;
    char buf[20];
    strftime(buf, sizeof(buf), "%Y-%m-%d", &localTime);
    date = new std::string(buf);
    return *date;
}

ErrInfo *LogWriter::unixMunmap(int fd, void *map, size_t map_size) {
    if (munmap(map, map_size) == -1) {
        unlock(fd);
        close(fd);
        return new ErrInfo(UNMMAP_EXIT, "Error un-mmapping the file");
    }
    return NULL;
}

//采用文件锁机制，要是文件锁已经被其他进程占用，则直接返回而不阻塞
int LogWriter::lock(int fd) {
    this->loglock.l_len = 0;
    this->loglock.l_start = 0;
    this->loglock.l_type = F_WRLCK;
    this->loglock.l_whence = SEEK_SET;
    return fcntl(fd, F_SETLK, &loglock);
}

int LogWriter::unlock(int fd) {
    this->loglock.l_type = F_ULOCK;
    return fcntl(fd, F_SETLK, &loglock);
}

