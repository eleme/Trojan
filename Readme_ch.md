# 1.简介

[Trojan](https://github.com/ELELogistics/Trojan)是一个稳定高效的移动端轻量级日志SDK，既可以记录通用日志，比如网络请求、电量变化、页面生命周期，也可以记录自定义的日志，从而可以通过用户日志来帮助我们定位分析问题。具有以下特点：

* 简洁的API，通过几行代码就可以接入，实现日记记录功能；
* 使用AOP技术[Lancet](https://github.com/eleme/lancet)框架插桩收集通用日志，并且支持增量编译；
* 使用mmap技术，保证日记记录的高效性；
* 扩展性高，开发者可以自定义日志文件的上传功能；
* 流量开销小，支持在线配置，远程控制用户日志文件上传与否；
* 稳定性高，目前已稳定运行在饿了么物流团队众包等多个APP上；

# 2.Trojan

在开源的[Trojan](https://github.com/ELELogistics/Trojan) SDK中，目前采集了Activity和Fragment生命周期，View Click事件，网络状态变化，手机电量状态变化等基础日志，还通过AOP技术插桩[KLog](https://github.com/ZhaoKaiQiang/KLog)采集Log日志，要是项目中未使用KLog，也可以根据项目情况具体定制。考虑到每个项目中网络模块的实现框架都不尽相同，有[OkHttp](https://github.com/square/okhttp)、[Volley](https://github.com/google/volley)、[Android-Async-Http](https://github.com/loopj/android-async-http)等等，所以采集网络日志这一部分不方便统一处理，需要使用者通过[Lancet](https://github.com/eleme/lancet)插桩具体网络框架来收集日志。在[Demo](https://github.com/ELELogistics/Trojan/blob/master/app/src/main/java/me/ele/trojan/demo/DemoHook.java)中具体以OkHttp为例，实现采集Http request和response的功能，可作为参考。而与业务相关的日志，需要使用者自己采集。

# 3.Gradle配置

在根目录的 build.gradle 添加:
```java
buildscript {
    repositories {
        ......
        maven { url "https://dl.bintray.com/michaelzhong/maven" }
    }
    dependencies {
        .......
        classpath 'me.ele:lancet-plugin:1.0.2'
    }
}
allprojects {
    repositories {
        ......
        maven { url "https://dl.bintray.com/michaelzhong/maven" }
    }
}

```

在 app 目录的'build.gradle' 添加：
```java
apply plugin: 'me.ele.lancet'

dependencies {
    ......
    provided 'me.ele:lancet-base:1.0.2'
    compile 'me.ele:trojan-library:0.0.3'
}
```
# 4.初始化

在自定义的`Application`添加：
```java
TrojanConfig config = new TrojanConfig.Builder(this)
                // 设置用户信息
                .userInfo("xxxx")
                // 设置当前的设备信息
                .deviceInfo("xxxx")
                // 可选，文件目前默认为/sdcard/包名_trojanLog/
                .logDir("xxxx")
                // 控制台日志开关，默认是打开
                .enableLog(true)
                .build();
Trojan.init(config);
```

特别说明：
1. 日志文件默认保存在sdcard中，即使应用被卸载，也不会丢失日志；
2. 为兼容多进程，避免文件相互干扰，日志文件保存在各自的目录下，目录以进程名来命名；
3. 默认情况下日志不加密，目前仅提供DES加密方式，但仍在探索更为高效简洁的实现方式，敬请期待。

# 5.记录日志

Trojan提供两种方式记录日志:

第一种：
```java
Trojan.log("Trojan", "We have a nice day!");
```

第二种：
```java
List<String> msgList = new LinkedList<>();
msgList.add("Hello Trojan!");
msgList.add("We have a nice day!");
msgList.add("Hello world!");
Trojan.log("Trojan", msgList);
```

# 6.用户信息
当用户信息发生改变或者切换用户时，可以调用：

```java
Trojan.refreshUser("new user info");
```

当然了，要是用户登出，可以传空值，即表示登出操作:

```java
Trojan.refreshUser(null);
```

# 7.上传方案

针对日志上传，在[Demo](https://github.com/ELELogistics/Trojan/blob/master/app/src/main/java/me/ele/trojan/demo/upload/DemoLeanCloudUploader.java)中提供了[LeanCloud](https://leancloud.cn/)这种免费简单的方式，可以实现上传、浏览、下载等文件服务的基本功能，可供参考。

# 8.最后

通过以上方式，就可以集成[Trojan](https://github.com/ELELogistics/Trojan)实现用户日志的记录功能，是不是很简单呀！要是对Lancet的使用有疑问，大家可以参考https://github.com/eleme/lancet/blob/dev/README_zh.md ，这里就不赘述。














