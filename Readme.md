# 1.简介
Trojan是一个高效的移动端轻量级日志SDK，既可以记录通用日志，比如网络请求、电量变化、页面生命周期，也可以记录自定义的日志，通过用户日志来定位和分析问题。

* 简洁的API，通过几行Java代码就可以完成日志记录功能
* 使用AOP技术[Lancet](https://github.com/eleme/lancet)框架来插桩收集日志
* 使用mmap技术，保证日记记录的高效性
* 扩展性高，开发者可以自定义日志文件的上传功能
* 支持在线配置，远程控制用户日志文件上传与否

# 2.Gradle配置
在根目录的 build.gradle 添加:
```java
dependencies{
    classpath 'me.ele:lancet-plugin:1.0.2'
}
```

在 app 目录的'build.gradle' 添加：
```java
apply plugin: 'me.ele.lancet'

dependencies {
    provided 'me.ele:lancet-base:1.0.2'
		compile 'me.ele:trojan:3.0.0'
}
```

# 3.权限声明
```java
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
```

# 4.初始化
在自定义的`Application`添加：
```java
TrojanConfig config = new TrojanConfig.Builder(this)
                // 设置用户信息
                .userInfo("xxxx")
                // 设置当前的设备信息
                .deviceInfo("xxxx")
                // 可选，自定义IExtraFileProvider接口，上传用户的附加文件
                .extraFileProvider(new CustomExtraFileProvider())
                // 控制台日志开关，默认是打开
                .enableLog(true)
                .build();
Trojan.init(config);
```

特别说明：
1. 附加文件并不会在初始化时上传，而是等日志文件一并上传，附件文件仅限于文件，不支持目录；
2. 日志文件保存在sdcard中，即时卸载后重装，日志也不会丢失；

# 5.记录日志
Trojan提供两种方式记录日志:

第一种：
```java
Trojan.log("Trojan", "We have a nice day!");
```

第二种：
```java
List<String> msgList = new LinkedList<>();
msgList.add("We have a nice day!");
msgList.add("Hello world!");
Trojan.log("Trojan", msgList);
```

# 6.用户信息
当用户信息发生改变或者切换用户时，可以调用：

```java
Trojan.refreshUser("new user id");
```

当然了，要是用户登出，可以传空值，即表示登出操作:

```java
Trojan.refreshUser(null);
```
# 7.最后
三个API就可以完成用户日志的记录功能，是不是很方便呀！要是对Lancet使用有疑问，可以参考https://github.com/eleme/lancet/blob/dev/README_zh.md ，这里就不赘述。# Trojan
