//
// Created by Allen on 2017/11/22.
//
#include "DESEncrypt.h"

//初始置换表,根据表中的规定，将输入的64位明文重新进行排序，即将第58位放到第1位，第50位放到第2位...以此类推,初始置换以后得到的是一个64位的输出
const static unsigned char ip_table[64] = {58, 50, 42, 34, 26, 18, 10, 2,
                                           60, 52, 44, 36, 28, 20, 12, 4,
                                           62, 54, 46, 38, 30, 22, 14, 6,
                                           64, 56, 48, 40, 32, 24, 16, 8,
                                           57, 49, 41, 33, 25, 17, 9, 1,
                                           59, 51, 43, 35, 27, 19, 11, 3,
                                           61, 53, 45, 37, 29, 21, 13, 5,
                                           63, 55, 47, 39, 31, 23, 15, 7};

//扩展置换，将数据从32位扩展为48位
static const unsigned char expa_perm[48] = {
        32, 1, 2, 3, 4, 5,
        4, 5, 6, 7, 8, 9,
        8, 9, 10, 11, 12, 13,
        12, 13, 14, 15, 16, 17,
        16, 17, 18, 19, 20, 21,
        20, 21, 22, 23, 24, 25,
        24, 25, 26, 27, 28, 29,
        28, 29, 30, 31, 32, 1
};

//S盒子代替
const static unsigned char sbox[8][64] = {
        {//S1盒子
                14, 4,  13, 1,  2,  15, 11, 8,  3,  10, 6,  12, 5,  9,  0,  7,
                0,  15, 7,  4,  14, 2,  13, 1,  10, 6, 12, 11, 9,  5,  3,  8,
                4,  1,  14, 8,  13, 6,  2,  11, 15, 12, 9,  7,  3,  10, 5,  0,
                15, 12, 8,  2,  4,  9,  1,  7,  5,  11, 3,  14, 10, 0, 6,  13
        },
        {//S2盒子
                15, 1,  8,  14, 6,  11, 3,  4,  9,  7,  2,  13, 12, 0,  5,  10,
                3,  13, 4,  7,  15, 2,  8,  14, 12, 0, 1,  10, 6,  9,  11, 5,
                0,  14, 7,  11, 10, 4,  13, 1,  5,  8,  12, 6,  9,  3,  2,  15,
                13, 8,  10, 1,  3,  15, 4,  2,  11, 6,  7,  12, 0,  5, 14, 9
        },
        {//S3盒子
                10, 0,  9,  14, 6,  3,  15, 5,  1,  13, 12, 7,  11, 4,  2,  8,
                13, 7,  0,  9,  3,  4,  6,  10, 2,  8, 5,  14, 12, 11, 15, 1,
                13, 6,  4,  9,  8,  15, 3,  0,  11, 1,  2,  12, 5,  10, 14, 7,
                1,  10, 13, 0,  6,  9,  8,  7,  4,  15, 14, 3,  11, 5, 2,  12
        },
        {//S4盒子
                7,  13, 14, 3,  0,  6,  9,  10, 1,  2,  8,  5,  11, 12, 4,  15,
                13, 8,  11, 5,  6,  15, 0,  3,  4,  7, 2,  12, 1,  10, 14, 9,
                10, 6,  9,  0,  12, 11, 7,  13, 15, 1,  3,  14, 5,  2,  8,  4,
                3,  15, 0,  6,  10, 1,  13, 8,  9,  4,  5,  11, 12, 7, 2,  14
        },
        {//S5盒子
                2,  12, 4,  1,  7,  10, 11, 6,  8,  5,  3,  15, 13, 0,  14, 9,
                14, 11, 2,  12, 4,  7,  13, 1,  5,  0, 15, 10, 3,  9,  8,  6,
                4,  2,  1,  11, 10, 13, 7,  8,  15, 9,  12, 5,  6,  3,  0,  14,
                11, 8,  12, 7,  1,  14, 2,  13, 6,  15, 0,  9,  10, 4, 5,  3
        },
        {//S6盒子
                12, 1,  10, 15, 9,  2,  6,  8,  0,  13, 3,  4,  14, 7,  5,  11,
                10, 15, 4,  2,  7,  12, 9,  5,  6,  1, 13, 14, 0,  11, 3,  8,
                9,  14, 15, 5,  2,  8,  12, 3,  7,  0,  4,  10, 1,  13, 11, 6,
                4,  3,  2,  12, 9,  5,  15, 10, 11, 14, 1,  7,  6,  0, 8,  13
        },
        {//S7盒子
                4,  11, 2,  14, 15, 0,  8,  13, 3,  12, 9,  7,  5,  10, 6,  1,
                13, 0,  11, 7,  4,  9,  1,  10, 14, 3, 5,  12, 2,  15, 8,  6,
                1,  4,  11, 13, 12, 3,  7,  14, 10, 15, 6,  8,  0,  5,  9,  2,
                6,  11, 13, 8,  1,  4,  10, 7,  9,  5,  0,  15, 14, 2, 3,  12
        },
        {//S8盒子
                13, 2,  8,  4,  6,  15, 11, 1,  10, 9,  3,  14, 5,  0,  12, 7,
                1,  15, 13, 8,  10, 3,  7,  4,  12, 5, 6,  11, 0,  14, 9,  2,
                7,  11, 4,  1,  9,  12, 14, 2,  0,  6,  10, 13, 15, 3,  5,  8,
                2,  1,  14, 7,  4,  10, 8,  13, 15, 12, 9,  0,  3,  5, 6,  11
        }
};

//P盒置换
const static unsigned char p_table[32] = {
        16, 7, 20, 21, 29, 12, 28, 17, 1, 15, 23, 26, 5, 18, 31, 10,
        2, 8, 24, 14, 32, 27, 3, 9, 19, 13, 30, 6, 22, 11, 4, 25
};

//末置换
const static unsigned char ipr_table[64] = {
        40, 8, 48, 16, 56, 24, 64, 32, 39, 7, 47, 15, 55, 23, 63, 31,
        38, 6, 46, 14, 54, 22, 62, 30, 37, 5, 45, 13, 53, 21, 61, 29,
        36, 4, 44, 12, 52, 20, 60, 28, 35, 3, 43, 11, 51, 19, 59, 27,
        34, 2, 42, 10, 50, 18, 58, 26, 33, 1, 41, 9, 49, 17, 57, 25
};

//将数据块初始置换为左右两个部分
int DESEncrypt::ip(const Block &block, HBlock &left, HBlock &right) {
    for (size_t i = 0; i < right.size(); ++i) {
        right[i] = block[ip_table[i] - 1]; //获取置换后的右半部分
    }
    for (size_t i = 0; i < left.size(); ++i) {
        left[i] = block[ip_table[i + left.size()] - 1]; //获取置换后的左半部分
    }
    return 0;
}

//一轮加密运算，不带交换
int DESEncrypt::des_turn(HBlock &left, HBlock &right, const Code &subkey) {
    Code code; //48位数据块
    HBlock pcode; //32位数据块
    //将右半部分扩展为48位
    for (size_t i = 0; i < code.size(); ++i) {
        code[i] = right[expa_perm[i] - 1];
    }
    //与子密钥异或
    code ^= subkey;
    //S盒的列
    bitset<4> col;
    //S盒的行
    bitset<2> row;
    for (size_t i = 0; i < 8; ++i) {
        //8个盒子
        row[0] = code[6 * i];//获取行标
        row[1] = code[6 * i + 5];
        col[0] = code[6 * i + 1]; //获取列标
        col[1] = code[6 * i + 2];
        col[2] = code[6 * i + 3];
        //这里是col[3]还是col[4]呀?
        col[3] = code[6 * i + 4];

        bitset<4> temp(sbox[i][row.to_ulong() * 16 + col.to_ulong()]);
        for (size_t j = 0; j < temp.size(); ++j) {
            code[4 * i + j] = temp[j]; //将32位暂存于48位中
        }

    }

    for (size_t i = 0; i < pcode.size(); ++i) {
        pcode[i] = code[p_table[i] - 1]; //P盒变换
    }
    left ^= pcode; //异或

    return 0;

}

//交换左右两个部分
int DESEncrypt::exchange(HBlock &left, HBlock &right) {
    HBlock temp;
    for (size_t i = 0; i < temp.size(); ++i) {
        temp[i] = left[i];
    }
    for (size_t i = 0; i < left.size(); ++i) {
        left[i] = right[i];
    }
    for (size_t i = 0; i < right.size(); ++i) {
        right[i] = temp[i];
    }
    return 0;
}

//将左右两部分数据进行末置换形成一个数据块
int DESEncrypt::rip(const HBlock &left, const HBlock &right, Block &block) {
    for (size_t i = 0; i < block.size(); ++i) {
        if (ipr_table[i] <= 32) {
            block[i] = right[ipr_table[i] - 1]; //从right部分获取数据
        } else {
            block[i] = left[ipr_table[i] - 32 - 1]; //从left部分获取数据
        }
    }
    return 0;
}

//密钥置换表，将64位密钥置换压缩为56位
const static unsigned char key_table[56] = {
        57, 49, 41, 33, 25, 17, 9, 1,
        58, 50, 42, 34, 26, 18, 10, 2,
        59, 51, 43, 35, 27, 19, 11, 3,
        60, 52, 44, 36, 63, 55, 47, 39,
        31, 23, 15, 7, 62, 54, 46, 38,
        30, 22, 14, 6, 61, 53, 45, 37,
        29, 21, 13, 5, 28, 20, 12, 4
};


//每轮移动的位数
const static unsigned char bit_shift[16] = {
        1, 1, 2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 1
};

//压缩置换表，56位密钥压缩为48位密钥
const static unsigned char comp_perm[48] = {
        14, 17, 11, 24, 1, 5, 3, 28,
        15, 6, 21, 10, 23, 19, 12, 4,
        26, 8, 16, 7, 27, 20, 13, 2,
        41, 52, 31, 37, 47, 55, 30, 40,
        51, 45, 33, 48, 44, 49, 39, 56,
        34, 53, 46, 42, 50, 36, 29, 32
};

//获取bkey产生的第n轮子密钥
DESEncrypt::Code DESEncrypt::getKey(const unsigned int n,const Block&bkey){
    //n在区间[0,15]之间取值，bkey为64位密钥
    Code result; //返回值，48位子密钥
    Key key; //56位密钥
    unsigned int klen=key.size(),rlen=result.size(); //分别为56和48
    //获取56位密钥
    for(size_t i=0;i<key.size();++i){
        key[i]=bkey[key_table[i]-1]; //密钥置换
    }

    for(size_t i=0;i<=n;++i){
        //循环移位
        for(size_t j=0;j<bit_shift[i];++j){
            //将密钥循环暂存在result中
            result[rlen - bit_shift[i] + j] = key[klen - bit_shift[i] + j] ;
            result[rlen / 2 - bit_shift[i] + j] = key[klen / 2 - bit_shift[i] + j] ;
        }
        key<<=bit_shift[i]; //移位
        for(size_t j=0;j<bit_shift[i];++j){
            //写回key中
            key[klen / 2 + j] = result[rlen - bit_shift[i] + j] ;
            key[j] = result[rlen / 2 - bit_shift[i] + j] ;
        }
    }
    //压缩置换
    for(size_t i=0;i<result.size();++i){
        result[i]=key[comp_perm[i]-1];
    }
    return result;
}


//加解密运算
int DESEncrypt::des(Block& block,Block& bkey,const Method method){
    //block为数据块，bkey为64位密钥
    HBlock left,right; //左右部分
    ip(block,left,right); //初始置换
    switch(method){
        case e://加密
            for(char i=0;i<16;++i){
                Code key=getKey((unsigned int)i,bkey);
                des_turn(left,right,key);
                if(i!=15){
                    exchange(left,right);
                }
            }
            break;
        case d://解密
            for(char i=15;i>=0;--i){
                Code key=getKey((unsigned int)i,bkey);
                des_turn(left,right,key);
                if(i!=0){
                    exchange(left,right);
                }
            }
            break;
        default:
            break;
    }
    rip(left,right,block); //末置换
    return 0;
}

void DESEncrypt::blockFromStr(Block&block,const char*str){
    for(size_t i=0;i<block.size();++i){
        if(0 != (*((unsigned char *)(str) + i / 8) & (1 << (7 - i % 8))))
            block[i] = true ;
        else
            block[i] = false ;
    }
}

void DESEncrypt::strFromBlock(char*str,const Block&block){
    //将8个字节全部置0
    memset(str,0,8);
    for(size_t i=0;i<block.size();++i){
        if(block[i]){
            *((unsigned char *)(str) + i / 8) |= (1 << (7 - i % 8)) ;
        }
    }
}

void DESEncrypt::encrypt(const char *src, char *cipher) {
    size_t len=strlen(src);
    Block block,bkey;
    blockFromStr(bkey,key.c_str());

    char buffer[8];
    //memset(cipher,0,len);

    int i=0,index=0;
    while(1){
        if(i>len-1){
            break;
        }
        memset(buffer,0,8);
        for(int j=0;j<8;++j){
            if(i>len-1){
                break;
            }
            buffer[j]=src[i++];
        }
        //构建数据b块
        blockFromStr(block,buffer);
        des(block,bkey,e);
        //获取运算后的数据
        strFromBlock(buffer,block);
        for(int j=0;j<8;++j){
            cipher[index++]=buffer[j];
        }
    }
}
