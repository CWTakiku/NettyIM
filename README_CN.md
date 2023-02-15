# NettyIM SDK
#### 一款基于Netty高度定制化的长连接SDK,它支持私有协议和Websocket协议的通信。



[英文文档](https://github.com/CWTakiku/NettyIM/blob/master/README.md)

[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg)](https://github.com/CWTakiku/NettyIM/pulls)      [![License](https://img.shields.io/badge/license-MIT-blue.svg)](https://github.com/CWTakiku/NettyIM/blob/master/LICENSE)

#### 一、功能介绍

1. 支持自定义私有协议
2. 支持websocket的ws、wss协议
3. 内置一套默认私有协议实现
4. 支持断线重连、连接重试
5. 地址自动切换
6. 支持消息重发、消息确认机制
7. 支持心跳机制
8. 自定义协议支持握手鉴权
9.  提供Netty消息处理器注册
10. 私有协议支持自定义编解码器
11. 连接状态、消息状态监听
12. 支持单个消息设置是否需要确认包
13. 支持各种参数配置

#### 二、典型应用
1. 应用IM通讯
2. 嵌入式设备通信
#### 三、引用库
 1. 添加仓库地址
```
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```
 2. 添加依赖
```
dependencies {
	        implementation 'com.github.mrchengwenlong:NettyIM:1.0.2'
	}
```
#### 四、使用方式
```  
    //添加网络权限
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.INTERNET" />
```
1. 客户端的配置说明
 - 私有协议的常用配置

``` 
    //所以default都可以替换成开发者的实现，只要实现相应接口即可
       imClient=new IMClient.Builder()
                .setCodec(new DefaultCodec()) //默认的编解码，如果是私有协议开发者可以使用自己的protobuf编解码
                .setShakeHands(new DefaultMessageShakeHandsHandler(getDefaultHands())) //设置握手认证，可选
                .setHeartBeatMsg(getDefaultHeart()) //设置心跳需为解码器所支持的对象,可选
                .setAckConsumer(new DefaultAckConsumer()) //设置消息确认包的消费者，这个将影响发送的消息是否发送超时
                .setConnectTimeout(10, TimeUnit.SECONDS) //设置连接超时
                .setResendCount(3)//设置发送失败重试次数
                .setConnectionRetryEnabled(true)//是否连接重试
                .setSendTimeout(6,TimeUnit.SECONDS)//设置发送超时
                .setHeartIntervalBackground(30,TimeUnit.SECONDS)//后台心跳间隔
                .registerMessageHandler(new DefaultMessageReceiveHandler(onMessageArriveListener)) //消息接收处理器
                .registerMessageHandler(new DefaultReplyReceiveHandler(onReplyListener)) //消息状态接收处理器
                .registerMessageHandler(new DefaultHeartbeatRespHandler()) //心跳接收处器
                .setEventListener(new DefaultEventListener("user id1")) //事件监听，可选
                .addAddress(new Address("192.168.69.32",8766,Address.Type.SOCKS))
                .addAddress(new Address("www.baidu.com",8766,Address.Type.SOCKS))
                .setProtocol(IMProtocol.PRIVATE)//协议类型
                .setMaxFrameLength(65535)//最大帧长
                .setMsgTriggerReconnectEnabled(true)//设置是否消息触发重连（默认true)
                .setConnectRetryInterval(500,TimeUnit.MILLISECONDS)//设置连接重试间隔时间
                .setOpenLog(true)   //打开SDK内部日志
                .build();
```

- Websocket协议的额外配置项
   
``` 
       IMClient.Builder()
                .addAddress(new Address("ws://192.168.69.32:8804/ws",8804,Address.Type.WS))//websocket地址
                 .addAddress(new Address("wss://test.domain:8804/wss",8804,Address.Type.WS))//wss协议地址
                .addWsHeader("user","userId1") //ws头
                .setProtocol(IMProtocol.WEB_SOCKET);//设置为webscoket协议
``` 
2. 建立连接
```
imClient.startConnect();//建立连接
```
3. 断开连接
```
imClient.disConnect();//主动断开连接，不会自动重连
```
4. 发送消息
```
  Request request=new Request.Builder(). //创建一个消息发送request           
              setNeedACK(true).//需要ACK
              setSendRetry(true). //能发送重试
              setBody(getMsgPack(appMessage.buildProto())). //body为解码所支持的对象
              build();
```
```
 imClient.newCall(request).enqueue(callback);//发送消息，消息在子线程回调
```
```
Disposable disposable=   imClient.newCall(request).enqueue(callback).subscribe(consumer); //发送消息，会订阅特定的消息处理
```
5. 其他一些API
```
 imClient.setBackground(background);//设置前后台切换，将会自动切换不同的心跳间隔
 imClient.isConnected();//判断是否连接中
 ...
```

#### 五、项目结构设计图
![image](https://github.com/mrchengwenlong/NettyIM/blob/master/IM客户端架构图.png)

#### 六、Demo使用
APP模块test下含有内置自定义协议和webscoket两种协议的后台代码，开启服务端，再到对应协议的客户端上修改服务端IP，运行客户端

#### 七、项目博客地址
[简书](https://www.jianshu.com/p/5b01f4d6e4f4)       [CSDN](https://blog.csdn.net/smile__dream/article/details/105681018)  [掘金](https://juejin.im/post/5ea569aaf265da47e34c19ed) 


如果使用过程遇到什么问题或者疑问欢迎提交issue,也欢迎star!
**联系方式**QQ916379012

**Android IM反馈交流群**
QQ群：1051018406

![image](https://github.com/mrchengwenlong/NettyIM/blob/master/50327b1d735eb106d6c94f40edfbbc7.jpg)
