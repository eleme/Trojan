package me.ele.trojan.encrypt;

/**
 * Created by michaelzhong on 2017/11/22.
 */

//TODO 后面会加入AES和TEA
public enum EncryptMethod {

    DES("DES");

    private String name;

    EncryptMethod(String name) {
        this.name = name;
    }

    public String getMethodName() {
        return name;
    }
}
