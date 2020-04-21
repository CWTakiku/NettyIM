# NettyIM
基于Netty+TCP+Protobuf+okhttp设计模式的SDK,让你拥有像Okhttp一样的使用体验，完全可定制化，内置断线重连，路由自动切换、消息重复、连接超时、读写超时、可定制化拦截器、消息回执(已读，撤回等)、可定制化protobuf等功能。

### 使用方式
```
    imClient=new IMClient.Builder()
                .setCodec(new DefaultCodec()) //默认的编解码，开发者可以使用自己的protobuf编解码
                .setShakeHands(getDefaultHands(),new DefaultShakeHandsHandler()) //设置握手认证，可选
                .setHeartBeatMsg(getDefaultHeart()) //设置心跳,可选
                 .setConnectTimeout(10, TimeUnit.SECONDS)//设置连接超时
                .setResendCount(3)//设置失败重试数
                .setConnectionRetryEnabled(true)//是否连接重试
                .setSendTimeout(6,TimeUnit.SECONDS)//设置发送超时
                .setHeartIntervalBackground(30,TimeUnit.SECONDS)//后台心跳间隔
                .setMessageRespHandler(new DefaultMessageRespHandler()) //消息响应接收器，开发者可自行定制实现MessageRespHandler接口即可
                .setMessageReceiveHandler(new DefaultMessageReceiveHandler(onMessageArriveListener)) //客户端消息接收器
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
### 项目博客地址
[简书](https://www.jianshu.com/p/5b01f4d6e4f4)
如果使用过程遇到什么问题或者疑问都可以和我说,欢迎star!
**联系方式**QQ916379012
