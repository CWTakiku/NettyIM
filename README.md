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
	        implementation 'com.github.mrchengwenlong:NettyIM:1.0.0'
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
                .setShakeHands(getDefaultHands(),new DefaultShakeHandsHandler()) //设置握手认证，可选
                .setHeartBeatMsg(getDefaultHeart()) //设置心跳,可选
                 .setConnectTimeout(10, TimeUnit.SECONDS)//设置连接超时
                .setResendCount(3)//设置失败重试数
                .setConnectionRetryEnabled(true)//是否连接重试
                .setSendTimeout(6,TimeUnit.SECONDS)//设置发送超时
                .setHeartIntervalBackground(30,TimeUnit.SECONDS)//后台心跳间隔
                .setHeartIntervalForeground(10,TimeUnit.SECONDS)//设置前台心跳间隔
                //.addInterceptor() //添加你自己的拦截器
                //.addChannelHandler() //添加你自己的channelHandler
                .setMessageRespHandler(new DefaultMessageRespHandler()) //设置消息响应接收器，开发者可自行定制实现MessageRespHandler接口即可
                .setMessageReceiveHandler(new DefaultMessageReceiveHandler(onMessageArriveListener)) //设置客户端消息接收器，开发者可自行定制实现接口即可
                .setEventListener(new DefaultEventListener("user id1")) //事件监听，可选
                .setAddress(new Address("192.168.69.32",8765,Address.Type.SOCKS)) //设置连接地址，可多地址
                .setAddress(new Address("www.baidu.com",8765,Address.Type.HTTP))
                .build();
```
```
imClient.startConnect();//建立连接
```
```
imClient.disConnect();//主动断开连接，不会自动重连
```
```
 imClient.newCall(request).enqueue(new UICallback(onResponseListener)); //发送消息，消息回执在主线程回调
```
```
 imClient.newCall(request).enqueue(callback);//发送消息，消息在子线程回调
```
```
 imClient.setBackground(background);//设置前后台切换，将会自动切换不同的心跳间隔
```
```
  Request request=new Request.Builder(). //创建一个消息发送request
              setRequestTag(appMessage.getHead().getMsgId()). //请求tag，对应回执tag
              setNeedResponse(true).//需要消息回执
              setSendRetry(true). //能发送重试
              setBody(getMsgPack(appMessage.buildProto())). //body为protbuf
              build();
```
### 三、详细使用
可自行下载源码运行，先运行com.takiku.im_lib.NettyServerDemo类的服务端demo，再运行demo APP

### 四、项目博客地址
[简书](https://www.jianshu.com/p/5b01f4d6e4f4)       [CSDN](https://blog.csdn.net/smile__dream/article/details/105681018)

如果使用过程遇到什么问题或者疑问都可以和我说,欢迎star!
**联系方式**QQ916379012
**Android IM交流群**
![image](https://github.com/mrchengwenlong/NettyIM/blob/master/50327b1d735eb106d6c94f40edfbbc7.jpg)
