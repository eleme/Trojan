//
// Created by michaelzhong on 2017/12/25.
//

#include <malloc.h>
#include "TEACipher.h"

void encryptLong(uint32_t *firstChunk, uint32_t *secondChunk, uint32_t *key) {
    uint32_t y = *firstChunk;
    uint32_t z = *secondChunk;
    uint32_t sum = 0;

    uint32_t delta = 0x9e3779b9;

    //8轮运算(需要对应下面的解密核心函数的轮数一样)
    for (int i = 0; i < 32; i++) {
        sum += delta;
        y += ((z << 4) + key[0]) ^ (z + sum) ^ ((z >> 5) + key[1]);
        z += ((y << 4) + key[2]) ^ (y + sum) ^ ((y >> 5) + key[3]);
    }

    *firstChunk = y;
    *secondChunk = z;
}

void decryptLong(uint32_t *firstChunk, uint32_t *secondChunk, uint32_t *key) {
    uint32_t sum = 0;
    uint32_t y = *firstChunk;
    uint32_t z = *secondChunk;
    uint32_t delta = 0x9e3779b9;

    //32轮运算，所以是2的5次方；16轮运算，所以是2的4次方；8轮运算，所以是2的3次方
    sum = 0xC6EF3720;

    //8轮运算
    for (int i = 0; i < 32; i++) {
        z -= (y << 4) + key[2] ^ y + sum ^ (y >> 5) + key[3];
        y -= (z << 4) + key[0] ^ z + sum ^ (z >> 5) + key[1];
        sum -= delta;
    }
    *firstChunk = y;
    *secondChunk = z;
}

//buffer：输入的待加密数据buffer，在函数中直接对元数据buffer进行加密；size：buffer长度；key是密钥；
void encryptChar(char *buffer, int size, uint32_t *key) {
    char *p = buffer;
    int leftSize = size;
    while (p < buffer + size && leftSize >= sizeof(uint32_t) * 2) {
        encryptLong((uint32_t *) p, (uint32_t *) (p + sizeof(uint32_t)), key);
        p += sizeof(uint32_t) * 2;
        leftSize -= sizeof(uint32_t) * 2;
    }
}

//buffer：输入的待解密数据buffer，在函数中直接对元数据buffer进行解密；size：buffer长度；key是密钥；
void decryptChar(char *buffer, int size, uint32_t *key) {
    char *p = buffer;
    int leftSize = size;
    while (p < buffer + size && leftSize >= sizeof(uint32_t) * 2) {
        decryptLong((uint32_t *) p, (uint32_t *) (p + sizeof(uint32_t)), key);
        p += sizeof(uint32_t) * 2;
        leftSize -= sizeof(uint32_t) * 2;
    }
}

void TEACipher::encrypt(const char *plaint, int srcSize, char *cipher) {
    strcpy(cipher, plaint);
    encryptChar(cipher, srcSize, (uint32_t *) this->key.c_str());
}

void TEACipher::decrypt(const char *cipher, int srcSize, char *plaint) {
    strcpy(plaint, cipher);
    decryptChar(plaint, srcSize, (uint32_t *) this->key.c_str());
}
