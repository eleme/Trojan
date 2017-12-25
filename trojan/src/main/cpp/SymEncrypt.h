//
// Created by Allen on 2017/11/22.
//

#ifndef TROJAN_SYMCRYPT_H
#define TROJAN_SYMCRYPT_H

#include <string>
#include <string.h>

class SymEncrypt{

public:
    SymEncrypt(std::string key){
        this->key=key;
    }

    virtual void encrypt(const char src[],char cipher[])=0;

protected:
    std::string key;
};


#endif //TROJAN_SYMCRYPT_H
