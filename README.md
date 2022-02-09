# NettyIM
基于Netty+TCP+Protobuf+okhttp设计模式的SDK,让你拥有像Okhttp一样的使用体验，完全可定制化，内置断线重连，路由自动切换、消息重复、连接超时、读写超时、可定制化拦截器、消息回执(已读，撤回等)、可定制化protobuf等功能。

### 一、添加依赖
Step 1. Add the JitPack repository to your build file
Add it in your root build.gradle at the end of repositories:
```
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```
Step 2. Add the dependency
```
dependencies {
	        implementation 'com.github.mrchengwenlong:NettyIM:1.0.1'
	}
```
### 二、使用方式
```  
    //添加网络权限
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.INTERNET" />
```
``` 
    //所以default都可以替换成开发者的实现，只要实现相应接口即可
        imClient=new IMClient.Builder()
                .setCodec(new DefaultCodec()) //默认的编解码，开发者可以使用自己的protobuf编解码
                .setShakeHands(new DefaultMessageShakeHandsHandler(getDefaultHands())) //设置握手认证，可选
                .setHeartBeatMsg(getDefaultHeart()) //设置心跳,可选
                .setAckConsumer(new DefaultAckConsumer()) //设置心跳机制
                .setConnectTimeout(10, TimeUnit.SECONDS) //设置连接超时
                .setResendCount(3)//设置失败重试数
                .setConnectionRetryEnabled(true)//是否连接重试
                .setSendTimeout(6,TimeUnit.SECONDS)//设置发送超时
                .setHeartIntervalBackground(30,TimeUnit.SECONDS)//后台心跳间隔
                .registerMessageHandler(new DefaultMessageReceiveHandler(onMessageArriveListener)) //消息接收处理器
                .registerMessageHandler(new DefaultReplyReceiveHandler(onReplyListener)) //消息状态接收处理器
                .registerMessageHandler(new DefaultHeartbeatRespHandler()) //心跳接收处理器
                .setEventListener(new DefaultEventListener("user id1")) //事件监听，可选
                .setAddress(new Address("192.168.69.32",8766,Address.Type.SOCKS))
                .setAddress(new Address("www.baidu.com",8766,Address.Type.HTTP))
                .build();
```
```
imClient.startConnect();//建立连接
```
```
imClient.disConnect();//主动断开连接，不会自动重连
```
```
  Request request=new Request.Builder(). //创建一个消息发送request           
              setNeedACK(true).//需要ACK
              setSendRetry(true). //能发送重试
              setBody(getMsgPack(appMessage.buildProto())). //body为protbuf
              build();
```
```
 imClient.newCall(request).enqueue(callback);//发送消息，消息在子线程回调
```
```
Disposable disposable=   imClient.newCall(request).enqueue(callback).subscribe(consumer); //发送消息，会订阅特定的消息处理
```
```
 imClient.setBackground(background);//设置前后台切换，将会自动切换不同的心跳间隔
```

### 三、项目结构设计图
![image](https://github.com/mrchengwenlong/NettyIM/blob/master/IM客户端架构图.png)

### 四、详细使用
可自行下载源码运行，先运行com.takiku.im_lib.NettyServerDemo类的服务端Demo（这个是个微型服务端后台，开了这个才能让APP互发消息），再运行Demo APP

### 五、项目博客地址
[简书](https://www.jianshu.com/p/5b01f4d6e4f4)       [CSDN](https://blog.csdn.net/smile__dream/article/details/105681018)  [掘金](https://juejin.im/post/5ea569aaf265da47e34c19ed) 


如果使用过程遇到什么问题或者疑问都可以和我说,欢迎star!
**联系方式**QQ916379012

**Android IM反馈交流群**
QQ群：1051018406

![image](https://github.com/mrchengwenlong/NettyIM/blob/master/50327b1d735eb106d6c94f40edfbbc7.jpg)
