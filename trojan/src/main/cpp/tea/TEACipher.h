//
// Created by michaelzhong on 2017/12/25.
//

#ifndef UNTITLED1_TEACIPHER_H
#define UNTITLED1_TEACIPHER_H


#include <string>

class TEACipher {

protected:
    std::string key;

public:
    TEACipher(const std::string &key) {
        this->key = key;
    }

    void encrypt(const char *plaint, int srcSize, char *cipher);

    void decrypt(const char *cipher, int srcSize, char *plaint);
};


#endif //UNTITLED1_TEACIPHER_H
