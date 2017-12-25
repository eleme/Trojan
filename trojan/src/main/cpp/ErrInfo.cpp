#include "ErrInfo.h"//
// Created by Allen on 2017/11/16.
//

ErrInfo::ErrInfo(int errCode, const char *errMsg) {
    this->errCode = errCode;
    this->errMsg = errMsg;
}