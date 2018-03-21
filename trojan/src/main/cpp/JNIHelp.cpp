//
// Created by Allen on 2017/11/13.
//
#include <jni.h>
#include <unistd.h>
#include "JNIHelp.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

/**
 * Equivalent to ScopedLocalRef, but for C_JNIEnv instead. (And slightly more powerful.)
 */
template<typename T>
class scoped_local_ref {
public:
    scoped_local_ref(C_JNIEnv *env, T localRef = NULL)
            : mEnv(env), mLocalRef(localRef) {
    }

    ~scoped_local_ref() { reset(); }

    void reset(T localRef = NULL) {
        if (mLocalRef != NULL) {
            (*mEnv)->DeleteLocalRef(reinterpret_cast<JNIEnv *>(mEnv), mLocalRef);
            mLocalRef = localRef;
        }
    }

    T get() const { return mLocalRef; }

private:
    C_JNIEnv *mEnv;
    T mLocalRef;

    // Disallow copy and assignment.
    scoped_local_ref(const scoped_local_ref &);

    void operator=(const scoped_local_ref &);
};

/*
 * Returns a human-readable summary of an exception object.  The buffer will
 * be populated with the "binary" class name and, if present, the
 * exception message.
 */
static char *getExceptionSummary0(C_JNIEnv *env, jthrowable exception) {
    JNIEnv *e = reinterpret_cast<JNIEnv *>(env);

    /* get the name of the exception's class */
    scoped_local_ref<jclass> exceptionClass(env,
                                            (*env)->GetObjectClass(e, exception)); // can't fail
    scoped_local_ref<jclass> classClass(env, (*env)->GetObjectClass(e,
                                                                    exceptionClass.get())); // java.lang.Class, can't fail
    jmethodID classGetNameMethod = (*env)->GetMethodID(e, classClass.get(), "getName",
                                                       "()Ljava/lang/String;");
    scoped_local_ref<jstring> classNameStr(env, (jstring) (*env)->CallObjectMethod(e,
                                                                                   exceptionClass.get(),
                                                                                   classGetNameMethod));
    if (classNameStr.get() == NULL) {
        return NULL;
    }

    /* get printable string */
    const char *classNameChars = (*env)->GetStringUTFChars(e, classNameStr.get(), NULL);
    if (classNameChars == NULL) {
        return NULL;
    }

    /* if the exception has a detail message, get that */
    jmethodID getMessage = (*env)->GetMethodID(e, exceptionClass.get(), "getMessage",
                                               "()Ljava/lang/String;");
    scoped_local_ref<jstring> messageStr(env, (jstring) (*env)->CallObjectMethod(e, exception,
                                                                                 getMessage));
    if (messageStr.get() == NULL) {
        return strdup(classNameChars);
    }

    char *result = NULL;
    const char *messageChars = (*env)->GetStringUTFChars(e, messageStr.get(), NULL);
    if (messageChars != NULL) {
        asprintf(&result, "%s: %s", classNameChars, messageChars);
        (*env)->ReleaseStringUTFChars(e, messageStr.get(), messageChars);
    } else {
        (*env)->ExceptionClear(e); // clear OOM
        asprintf(&result, "%s: <error getting message>", classNameChars);
    }
    (*env)->ReleaseStringUTFChars(e, classNameStr.get(), classNameChars);
    return result;
}

static char *getExceptionSummary(C_JNIEnv *env, jthrowable exception) {
    JNIEnv *e = reinterpret_cast<JNIEnv *>(env);
    char *result = getExceptionSummary0(env, exception);
    if (result == NULL) {
        (*env)->ExceptionClear(e);
        result = strdup("<error getting class name>");
    }
    return result;
}

static jclass findClass(C_JNIEnv *env, const char *className) {
    JNIEnv *e = reinterpret_cast<JNIEnv *>(env);
    return (*env)->FindClass(e, className);
}


extern "C" int
doJniThrowException(C_JNIEnv *env, const char *className, const char *msg) {
    JNIEnv *e = reinterpret_cast<JNIEnv *>(env);

    if ((*env)->ExceptionCheck(e)) {
        /* consider creating the new exception with this as "cause" */
        scoped_local_ref<jthrowable> exception(env, (*env)->ExceptionOccurred(e));
        (*env)->ExceptionClear(e);

        if (exception.get() != NULL) {
            char *text = getExceptionSummary(env, exception.get());
            //ALOGW("Discarding pending exception (%s) to throw %s", text,className);
            free(text);
        }
    }

    scoped_local_ref<jclass> exceptionClass(env, findClass(env, className));
    if (exceptionClass.get() == NULL) {
        //ALOGE("Unable to find exception class %s", className);
        /* ClassNotFoundException now pending */
        return -1;
    }

    if ((*env)->ThrowNew(e, exceptionClass.get(), msg) != JNI_OK) {
        //ALOGE("Failed throwing '%s' '%s'", className, msg);
        /* an exception, most likely OOM, will now be pending */
        return -1;
    }

    return 0;
}

void doThrowIAE(JNIEnv *env, const char *msg) {
    jniThrowException(env, "java/lang/IllegalArgumentException", msg);
}

void doThrowISE(JNIEnv *env, const char *msg) {
    jniThrowException(env, "java/lang/IllegalStateException", msg);
}

void doThrowIOE(JNIEnv *env, const char *msg) {
    jniThrowException(env, "java/io/IOException", msg);
}

void print(JNIEnv *env, const char *string) {
    jclass log = env->FindClass("android/util/Log");
    jmethodID v = env->GetStaticMethodID(log, "v", "(Ljava/lang/String;Ljava/lang/String;)I");
    jstring mmap = env->NewStringUTF("Mmap");
    jstring msg = env->NewStringUTF(string);
    env->CallStaticIntMethod(log, v, mmap, msg);
    env->DeleteLocalRef(log);
    env->DeleteLocalRef(mmap);
    env->DeleteLocalRef(msg);
}


void throwExceptionIfNeed(JNIEnv *env, ErrInfo *errInfo) {
    if (errInfo == NULL) {
        return;
    }
    switch (errInfo->errCode) {
        case OPEN_EXIT:
            doThrowIOE(env, errInfo->errMsg);
            break;
        case FSTAT_EXIT:
            doThrowIAE(env, errInfo->errMsg);
            break;
        case LSEEK_EXIT:
            doThrowIOE(env, errInfo->errMsg);
            break;
        case WRITE_EXIT:
            doThrowIOE(env, errInfo->errMsg);
            break;
        case MMAP_EXIT:
            doThrowIOE(env, errInfo->errMsg);
            break;
        case UNMMAP_EXIT:
            doThrowIOE(env, errInfo->errMsg);
        case LOCK_EXIT:
            doThrowIOE(env, errInfo->errMsg);
            break;
        case UNLOCK_EXIT:
            doThrowIOE(env, errInfo->errMsg);
            break;
        case ACCESS_EXIT:
            doThrowIOE(env, errInfo->errMsg);
        default:
            //do nothing
            break;
    }
}

