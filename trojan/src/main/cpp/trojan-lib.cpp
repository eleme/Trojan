//
// Created by Allen on 2017/11/8.
//
#include <stdio.h>
#include <stdlib.h>
#include <sys/stat.h>
#include <unistd.h>
#include <fcntl.h>
#include <sys/mman.h>
#include <jni.h>
#include <string>
#include <vector>
#include <fstream>
#include "LogWriter.h"

#ifndef TROJAN_LOG_WRITER_H
#define TROJAN_LOG_WRITER_H

#ifdef __cplusplus
extern "C" {
#endif

std::string jstring2string(JNIEnv *env, jstring jStr) {
    const jclass stringClass = env->GetObjectClass(jStr);
    const jmethodID getBytes = env->GetMethodID(stringClass, "getBytes", "(Ljava/lang/String;)[B");
    const jbyteArray stringJbytes = (jbyteArray) env->CallObjectMethod(jStr, getBytes,
                                                                       env->NewStringUTF("UTF-8"));

    size_t length = (size_t) env->GetArrayLength(stringJbytes);
    jbyte *pBytes = env->GetByteArrayElements(stringJbytes, NULL);

    std::string ret = std::string((char *) pBytes, length);
    env->ReleaseByteArrayElements(stringJbytes, pBytes, JNI_ABORT);

    env->DeleteLocalRef(stringJbytes);
    env->DeleteLocalRef(stringClass);

    return ret;
}

jlong Java_me_ele_trojan_record_impl_MmapLogWriter_nativeInit(JNIEnv *env, jobject object,
                                                              jstring basic_info, jstring dir,
                                                              jboolean encrypt_basic,
                                                              jstring encrypt_method, jstring key) {

    //////////////////////////////////////
    std::string methodName = "nativeInit";
    print(env, methodName.c_str());
    ////////////////////////////////////

    LogWriter *logWriter = new LogWriter();
    std::string basicInfo = jstring2string(env, basic_info);
    std::string logDir = jstring2string(env, dir);
    //注意:encrypt_method和key有可能为null
    std::string encryptMethod;
    if (encrypt_method != NULL) {
        encryptMethod = jstring2string(env, encrypt_method);
    }
    //TODO 如果对于encryptKey有长度要求的话，可以考虑进行统一的处理(比如截断，填充或者是进行MD5处理)得到定长的key
    std::string encryptKey;
    if (key != NULL) {
        encryptKey = jstring2string(env, key);
    }

    ErrInfo *errInfo = logWriter->init(env, basicInfo, logDir, (bool) encrypt_basic, encryptMethod,
                                       encryptKey);

    throwExceptionIfNeed(env, errInfo);

    if (errInfo != NULL) {
        delete errInfo;
    }

    return reinterpret_cast<jlong>(logWriter);
}

jlong Java_me_ele_trojan_record_impl_MmapLogWriter_nativeWrite(JNIEnv *env,
                                                               jobject object,
                                                               jlong log_writer_object,
                                                               jstring msg_content,
                                                               jboolean crypt) {
    //////////////////////////////////////
    std::string methodName = "nativeWrite";
    print(env, methodName.c_str());
    ////////////////////////////////////

    //TODO 这里要加上判空操作
    const char *msg = env->GetStringUTFChars(msg_content, 0);
    LogWriter *logWriter = reinterpret_cast<LogWriter *>(log_writer_object);
    ErrInfo *errInfo = logWriter->writeLog(env, msg, (bool) crypt);
    if (errInfo != NULL) {
        throwExceptionIfNeed(env, errInfo);
        delete errInfo;
    }
    env->ReleaseStringUTFChars(msg_content, msg);

    return reinterpret_cast<jlong>(logWriter);
}

void Java_me_ele_trojan_record_impl_MmapLogWriter_nativeRefreshBasicInfo(JNIEnv *env,
                                                                         jobject object,
                                                                         jlong log_writer_object,
                                                                         jstring basic_info) {
    //////////////////////////////////////
    std::string methodName = "nativeRefreshBasicInfo";
    print(env, methodName.c_str());
    ////////////////////////////////////

    std::string basicInfo = jstring2string(env, basic_info);
    LogWriter *logWriter = reinterpret_cast<LogWriter *>(log_writer_object);
    logWriter->refreshBasicInfo(env, basicInfo);
}

void Java_me_ele_trojan_record_impl_MmapLogWriter_nativeCloseAndRenew(JNIEnv *env, jobject object,
                                                                      jlong logWriterObject) {

    //////////////////////////////////////
    std::string methodName = "nativeCloseAndRenew";
    print(env, methodName.c_str());
    ////////////////////////////////////

    LogWriter *logWriter = reinterpret_cast<LogWriter *>(logWriterObject);
    logWriter->closeAndRenew(env);
}

#ifdef __cplusplus
};
#endif


#endif

