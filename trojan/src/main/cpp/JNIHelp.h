//
// Created by Allen on 2017/11/13.
//

#ifndef TROJAN_JNIHELP_H
#define TROJAN_JNIHELP_H

#include <jni.h>
#include <unistd.h>
#include "ErrInfo.h"
//#include "Logger.h"

#ifdef __cplusplus
extern "C" {
#endif
/*
 * Throw an exception with the specified class and an optional message.
 *
 * The "className" argument will be passed directly to FindClass, which
 * takes strings with slashes (e.g. "java/lang/Object").
 *
 * If an exception is currently pending, we log a warning message and
 * clear it.
 *
 * Returns 0 on success, nonzero if something failed (e.g. the exception
 * class couldn't be found, so *an* exception will still be pending).
 *
 * Currently aborts the VM if it can't throw the exception.
 */
int doJniThrowException(C_JNIEnv *env, const char *className, const char *msg);

/**
 * 由于这个函数很短，作为内联函数即可
 * @param env
 * @param className
 * @param msg
 * @return
 */
inline int jniThrowException(JNIEnv* env, const char* className, const char* msg) {
    return doJniThrowException(&env->functions, className, msg);
}

/**
 * throw java/lang/IllegalArgumentException
 * @param env
 * @param msg
 */
void doThrowIAE(JNIEnv*env,const char*msg);

/**
 * throw java/langa/IllegalStateException
 * @param env
 * @param msg
 */
void doThrowISE(JNIEnv*env,const char*msg);

/**
 * throw java/io/IOException
 * @param env
 * @param msg
 */
void doThrowIOE(JNIEnv*env,const char*msg);

void print(JNIEnv *env, const char *string);

void throwExceptionIfNeed(JNIEnv *env, ErrInfo *errInfo);

#ifdef __cplusplus
};
#endif

#endif //TROJAN_JNIHELP_H
