//
// Created by Allen on 2017/11/22.
//

#ifndef TROJAN_DESENCRYPT_H
#define TROJAN_DESENCRYPT_H

#include <iostream>
#include <bitset>
#include "../SymEncrypt.h"

using namespace std;

class DESEncrypt : public SymEncrypt {

public:
    DESEncrypt(std::string key) : SymEncrypt(key) {

    }

    typedef bitset<64> Block;
    typedef bitset<56> Key;
    typedef bitset<48> Code;

    typedef bitset<32> HBlock;
    typedef bitset<28> HKey;
    typedef bitset<24> HCode;

private:


    typedef enum {
        e, d
    } Method;

    int ip(const Block &block, HBlock &left, HBlock &right);

    int des_turn(HBlock &left, HBlock &right, const Code &subkey);

    int exchange(HBlock &left, HBlock &right);

    int rip(const HBlock &left, const HBlock &right, Block &block);

    Code getKey(const unsigned int n, const Block &bkey);

    int des(Block &block, Block &bkey, const Method method);

    void blockFromStr(Block &block, const char *str);

    void strFromBlock(char *str, const Block &block);

    /**
     * 加密，返回密文。
     * 注意返回值是通过new创建的，后面要记得销毁掉!
     * @param src 明文
     * @return
     */
    void encrypt(const char src[], char cipher[]);

};


#endif //TROJAN_DESENCRYPT_H
