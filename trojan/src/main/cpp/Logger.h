//
// Created by Allen on 2017/11/13.
//

#ifndef TROJAN_LOGGER_H
#define TROJAN_LOGGER_H

#include <android/log.h>

#ifdef __cplusplus
extern "C" {
#endif

typedef int (*trojan_logfunc_t)(int prio, const char *tag, const char *msg);

trojan_logfunc_t trojan_get_log_function();
void trojan_set_log_function(trojan_logfunc_t func);

int trojan_log_print(int prio, const char *tag, const char *fmt, ...)
#ifdef __GNUC__
__attribute__((format(printf, 3, 4)))
#endif
;

int trojan_log_vprint(int prio, const char *tag, const char *fmt, va_list ap);

inline static int trojan_log_write(int prio, const char *tag, const char *msg) {
    return trojan_get_log_function()(prio, tag, msg);
}

#ifndef MMDB_NO_LOGX_MACRO

#define LOGV(tag, fmt, args...)                                                \
    trojan_log_print(ANDROID_LOG_VERBOSE, (tag), (fmt), ##args)
#define LOGD(tag, fmt, args...)                                                \
    trojan_log_print(ANDROID_LOG_DEBUG, (tag), (fmt), ##args)
#define LOGI(tag, fmt, args...)                                                \
    trojan_log_print(ANDROID_LOG_INFO, (tag), (fmt), ##args)
#define LOGW(tag, fmt, args...)                                                \
    trojan_log_print(ANDROID_LOG_WARN, (tag), (fmt), ##args)
#define LOGE(tag, fmt, args...)                                                \
    trojan_log_print(ANDROID_LOG_ERROR, (tag), (fmt), ##args)

#define ALOG(priority, tag, ...)                                               \
    trojan_log_print(ANDROID_##priority, (tag), __VA_ARGS__)
#define ALOGV(...) ((void) LOGV(LOG_TAG, __VA_ARGS__))
#define ALOGD(...) ((void) LOGD(LOG_TAG, __VA_ARGS__))
#define ALOGI(...) ((void) LOGI(LOG_TAG, __VA_ARGS__))
#define ALOGW(...) ((void) LOGW(LOG_TAG, __VA_ARGS__))
#define ALOGE(...) ((void) LOGE(LOG_TAG, __VA_ARGS__))

#define IF_ALOG(priority, tag) if (1)
#define LOG_FATAL_IF(cond, ...) // do nothing
#define ALOG_ASSERT(cond, ...) LOG_FATAL_IF(!(cond), ##__VA_ARGS__)

#endif

#ifdef __cplusplus
}
#endif



#endif //TROJAN_LOGGER_H
