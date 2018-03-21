![](/assets/trojan_banner.png)

<p align="center">
	<a href="https://gradleupdate.appspot.com/ELELogistics/Trojan/status">
        <img src="https://gradleupdate.appspot.com/ELELogistics/Trojan/status.svg">
    </a>
    <a href="https://www.java.com">
        <img src="https://img.shields.io/badge/language-Java-blue.svg">
    </a>
    <a href="https://raw.githubusercontent.com/EyreFree/EFQRCode/assets/icon/MadeWith%3C3.png">
        <img src="https://img.shields.io/badge/made%20with-%3C3-orange.svg">
    </a>
</p>

[Trojan](https://github.com/ELELogistics/Trojan) is a stable and efficient mobile lightweight log SDK that not only records general logs, such as Http, power changes, component life cycles, but also records the definition of the log, which it is useful to  analysis problems through the user logs. Here is the following characteristics:

* Concise API, it is easy to achieve diary record function through a few lines of code;
* Use the AOP technologies [Lancet](https://github.com/eleme/lancet) SDK, it is comfortable to collect common logs, also support incremental compile;
* Use mmap technology to ensure the efficiency of log records;
* Scalability, developers can customize the log file to achieve the purpose of the upload;
* Small traffic overhead, remote control user log file upload or not by online configuration;
* High stability, it is very stable at many apps.

> [中文介绍](/README_CN.md)

## Overview

In the open source [Trojan](https://github.com/ELELogistics/Trojan) SDK, we have collected basic logs such as Activity and Fragment lifecycles, View Click events, network status changes, phone battery status changes, also include collecting [KLog](https://github.com/ZhaoKaiQiang/KLog) logs, if KLog is not used in the project, it can be customized according to the project specific conditions. Considering that the implementation framework for network modules is different at fact, there are [OkHttp](https://github.com/square/okhttp), [Volley](https://github.com/google/volley), [Android-Async-Http](https://github.com/loopj/android-async-http), etc. Therefore, this part of collecting network logs is not suitable for customization. Users can use [Lancet](https://github.com/eleme/lancet) to collect logs  at the specific network framework. In [Demo](https://github.com/ELELogistics/Trojan/blob/master/app/src/main/java/me/ele/trojan/demo/DemoHook.java), we can achieve acquisition Http request and response functions in OkHttp, it can be used as a reference. As for business-related logs, users should collect by self.

## Installation

Add in the root directory's build.gradle:

```java
buildscript {
    dependencies {
        ......
        classpath 'me.ele:lancet-plugin:1.0.2'
    }
}
```

Add in the app directory's build.gradle:

```java
apply plugin: 'me.ele.lancet'

dependencies {
    ......
    provided 'me.ele:lancet-base:1.0.2'
    compile 'me.ele:trojan-library:0.0.5'
}
```

## Use

### 1. Initialization

Add in the custom Application:

```java
TrojanConfig config = new TrojanConfig.Builder(this)
    // Set user information
    .userInfo("xxxx")
    // Set device id
    .deviceId("xxxx")
    // Set cipher key if need encry log
    .cipherKey("xxxx")
    // Optional, save log file in sdcard by default
    .logDir("xxxx")
    // Console log switch, the default is open
    .enableLog(true)
    .build();
Trojan.init(config);
```

Special Note:

1. The log files are stored in sdcard by default and will not be lost even if the application is uninstalled;
2. To be compatible with multiple processes, log files stored in their respective directories;
3. The log is not encrypted by default, we currently only provide TEA encryption in view for high efficiency。

### 2. Record the log

Trojan provides two ways to recode log:

Firstly:

```java
Trojan.log("Trojan", "We have a nice day!");
```

Secondly:

```java
List<String> msgList = new LinkedList <>();
msgList.add("Hello Trojan!");
msgList.add("We have a nice day!");
msgList.add("Hello world!");
Trojan.log("Trojan", msgList);
```

By default, a single line log is not encrypted, and if you need to encrypt it, the following is used:

```java
Trojan.log("Trojan", "We have a nice day!", true);
```

### 3. User Information

When the user information changes or switch users, you can call:

```java
Trojan.refreshUser("new user info");
```

Of course, if the user logs out, you can pass a null value:

```java
Trojan.refreshUser(null);
```

### 4. Upload Solution

In [Demo](https://github.com/ELELogistics/Trojan/blob/master/app/src/main/java/me/ele/trojan/demo/upload/DemoLeanCloudUploader.java) we provided free and simple way such as [LeanCloud](https://leancloud.cn/) to upload log file, you can browse, download and download log file.

### 5. Data Decryption

When we set the encryption key, we can encrypt a single log to ensure the security of sensitive data. We must decrypt the encrypted data When we analyze the log. Use the [decryption script](/decrypt/trojan_decrypt.py) as follows:

1. Compile and generate decryption SO library on MAC, The so library has been generated in the repository. This step can be omitted:
    
    ```java
    gcc -shared -Wl,-install_name,trojan_decrypt.so -o trojan_decrypt.so -fPIC trojan_decrypt.c
    
    ```
2. Call python script to decrypt the data on MAC, you need to pass in the decryption key and file path to be decrypted, it should be noted that the path of the python script:

    ```java
    python ./trojan_decrypt.py cipher-key cipher-file-path
    
    ```
    
## PS

Through the above steps, you can integrate [Trojan](https://github.com/ELELogistics/Trojan) SDK to record user's log, That is very simple! If you have any questions about the use of Lancet, you can get more details by [Readme](https://github.com/eleme/lancet/blob/dev/README_zh.md).

## License

![](/assets/trojan_license.png)

Trojan is available under the Apache-2.0 license. See the [LICENSE](/LICENSE) file for more info.
