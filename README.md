# NettyIM SDK
### 一款基于Netty高度定制化的通讯SDK,它支持TCP、UDP和WebSocket协议的通信。



[英文文档](https://github.com/CWTakiku/NettyIM/blob/master/README_EN.md)

[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg)](https://github.com/CWTakiku/NettyIM/pulls)      [![License](https://img.shields.io/badge/license-MIT-blue.svg)](https://github.com/CWTakiku/NettyIM/blob/master/LICENSE) [![](https://www.jitpack.io/v/CWTakiku/NettyIM.svg)](https://www.jitpack.io/#CWTakiku/NettyIM)

### 一、功能介绍

1. 支持TCP协议
2. 支持WebSocket的ws、wss协议
3. 支持UDP协议
4. 内置一套默认私有协议实现
5. 支持断线重连、连接重试
6. 地址自动切换
7. 支持消息重发、消息确认机制
8. 支持心跳机制
9. tcp协议、udp协议、websocket都支持握手鉴权
10. 提供Netty消息处理器注册
11. 支持自定义编解码器
12. 连接状态、消息状态监听
13. 支持单个消息设置是否需要确认包
14. 支持各种参数配置

### 二、典型应用
1. 应用IM通讯
2. 嵌入式设备通信
### 三、引用库
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
	       implementation 'com.github.CWTakiku:NettyIM:latest'
	}
```
### 四、使用方式
```  
    //添加网络权限
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.INTERNET" />
```
#### 1.  客户端的配置
说明：所有以Default开头的类都是可以被替代的，只要开发者实现相应的接口
 - 多种协议的公共配置
  

``` 
    IMClient.Builder builder = new IMClient.Builder()
                .setConnectTimeout(10, TimeUnit.SECONDS) //设置连接超时
                .setResendCount(3)//设置失败重试数
                .setConnectRetryInterval(1000,TimeUnit.MILLISECONDS)//连接尝试间隔
                .setConnectionRetryEnabled(true)//是否连接重试
                .setSendTimeout(6,TimeUnit.SECONDS)//设置发送超时
                .setHeartIntervalBackground(30,TimeUnit.SECONDS)//后台心跳间隔
                .setReaderIdleTimeBackground(90,TimeUnit.SECONDS)//后台读空闲触发时间 ，搭配心跳机制使用（指在一定的时间内没收到服务器的任何消息，则认为网络异常或者服务器异常，如果setReaderIdleReconnectEnabled(true)触发重连）
                .setEventListener(eventListener!=null?eventListener:new DefaultEventListener(userId)) //事件监听，可选
                .setMsgTriggerReconnectEnabled(true)  //如果连接已经断开，消息发送是否触发重连
                .setReaderIdleReconnectEnabled(true) //读空闲是否会触发重连
                .setProtocol(protocol) //哪种协议 IMProtocol.PRIVATE、IMProtocol.WEB_SOCKET、IMProtocol.UDP
                .setOpenLog(true);//是否开启日志
```

- TCP协议配置
   
``` 
        //以下支持两种数据格式，一种protobuf,一种string格式
         builder.setCodec(codecType == 0?new DefaultTcpProtobufCodec():new DefaultTcpStringCodec())//默认的编解码，开发者可以使用自己的protobuf或者其他格式的编解码
                    .setShakeHands(codecType == 0? new DefaultProtobufMessageShakeHandsHandler(getDefaultTcpHands()):new DefaultStringMessageShakeHandsHandler(getDefaultStringHands())) //设置握手认证，可选
                    .setHeartBeatMsg(codecType == 0? getDefaultProtobufHeart(): getDefaultStringHeart()) //设置心跳,可选
                    .setAckConsumer(codecType == 0?new DefaultProtobufAckConsumer():new DefaultStringAckConsumer()) //设置消息确认机制，如果需要消息回执，必选
                    .registerMessageHandler(codecType == 0?new DefaultProtobufMessageReceiveHandler(onMessageArriveListener):new DefaultStringMessageReceiveHandler(onMessageArriveListener)) //消息接收处理器
                    .registerMessageHandler(codecType == 0?new DefaultReplyReceiveHandler(onReplyListener):new DefaultStringMessageReplyHandler(onReplyListener)) //消息状态接收处理器
                    .registerMessageHandler(codecType == 0?new DefaultProtobufHeartbeatRespHandler():new DefaultStringHeartbeatRespHandler()) //心跳接收处理器
                    .setTCPLengthFieldLength(2)//本库拆包采用消息头包含消息长度的协议，装包拆包的长度字段的占用字节数，默认值为2
                    .addAddress(new Address(ip,9081,Address.Type.TCP))
                    .setMaxFrameLength(65535*100); //设置最大帧长 //私有tcp和websocket生效
``` 
- WebSocket协议配置
  
``` 
     builder.setHeartBeatMsg(getDefaultWsHeart())
                    .setAckConsumer(new DefaultWSAckConsumer())
                    .registerMessageHandler(new DefaultWSMessageReceiveHandler(onMessageArriveListener))
                    .registerMessageHandler(new DefaultWSMessageReplyHandler(onReplyListener))
                    .registerMessageHandler(new DefaultWsHeartbeatRespHandler())
                    .addAddress(new Address(ip,8804,Address.Type.WS))
                    .setMaxFrameLength(65535*100)
                  //  .addAddress(new Address(ip,8804,Address.Type.WSS))//支持WSS协议，请在scheme带上wss标识
                    .addWsHeader("user",userId); //webSocket特有的，可以用来鉴权使用
``` 


- UDP协议配置
                 
``` 
   builder.setCodec(new DefaultUdpStringCodec(new InetSocketAddress(ip,8804), CharsetUtil.UTF_8)) //String的编解码，开发者可以设定为自己的格式
                    .setShakeHands(new DefaultStringMessageShakeHandsHandler(getDefaultStringHands())) //设置握手认证，可选
                    .setHeartBeatMsg(getDefaultStringHeart()) //设置心跳,可选
                    .setAckConsumer(new DefaultStringAckConsumer()) //设置确认机制
                    .registerMessageHandler(new DefaultStringMessageReceiveHandler(onMessageArriveListener)) //消息接收处理器
                    .registerMessageHandler(new DefaultStringMessageReplyHandler(onReplyListener)) //消息状态接收处理器
                    .registerMessageHandler(new DefaultStringHeartbeatRespHandler()) //心跳接收处理器
                    .addAddress(new Address(ip, 8804, Address.Type.UDP));                
``` 

#### 2 构造IMClient
``` 
  IMClient imClient = builder.build();
``` 
#### 3. 建立连接
```
imClient.startConnect();//建立连接
```
#### 4. 断开连接
```
imClient.disConnect();//主动断开连接，不会自动重连
```
#### 5. 发送消息
```
  Request request=new Request.Builder(). //创建一个消息发送request           
              setNeedACK(true).//需要ACK，true就会触发消息确认机制
              setSendRetry(true). //此消息能否发送重试
              setBody(getMsgPack(appMessage.buildProto())). //body为解码所支持的对象
              build();
```

```
 imClient.newCall(request).enqueue(callback);//发送消息，消息在子线程回调
```
```
Disposable disposable=   imClient.newCall(request).enqueue(callback).subscribe(consumer); //发送消息，会订阅特定的消息处理，例如：我发送了一个特别的消息，然后想订阅该特定消息的后续响应
```
#### 6、接收消息
所有的消息接收都在上述配置中注册的registerMessageHandler()里,开发者可以自行实现MessageHandler接口
```
public  interface  MessageHandler<message extends Object>  {
    boolean isFocusMsg(Object msg); //是否是该处理器关注的消息类型
    void handleMsg(message message);//接收处理消息
}
```

#### 7.状态监听
状态监听在上述配置中的setEventListener()里，开发者可以自行继承EventListener类监听回调
```

    /**
     * 连接开始
     * @param inetSocketAddress
     */
    public  void connectStart( InetSocketAddress inetSocketAddress){

    }

    /**
     * 连接成功
     */
    public  void connectSuccess(){

    }

    /**
     * 连接出现异常
     * @param throwable
     */
    public void connectionException(Throwable throwable){

    }

    /**
     * 连接失败
     * @param inetSocketAddress
     * @param ioe
     */
    public void connectFailed( InetSocketAddress inetSocketAddress, IOException ioe) {

    }

    /**
     * 连接断开
     */
    public void connectionBroken(){

    }


    /**
     * 连接释放
     * @param connection
     */
    public void connectionReleased(Connection connection) {
    }

    /**
     * 发送开始
     * @param call
     */
    public void sendMsgStart(Call call) {
    }

    /**
     * 发送结束
     * @param call
     */
    public void sendMsgEnd(Call call) {
    }

    /**
     * 发送失败
     * @param call
     */
    public void sendMsgFailed(Call call){}

```

#### 8. 其他一些API
```
 imClient.setBackground(background);//设置前后台切换，将会自动切换不同的心跳间隔
 imClient.isConnected();//判断是否连接中
 ...
```

### 五、项目结构设计图
![image](https://github.com/CWTakiku/NettyIM/blob/master/IMPic.png)

### 六、Demo使用

#### 步骤1、 修改服务端地址，运行项目
在MainActivity类里将localHost改为自己电脑的ip地址，运行项目，将APP跑在手机或者模拟器上
#### 步骤2、开启服务器
APP模块test下含有tcp协议的protobuf和string两种数据格式的服务端demo、webscoket协议、udp协议，服务端的demo，运行相对应的服务器demo
#### 步骤3、使用
在APP里点击相应的协议进入chat界面




### 七、项目博客地址
[简书](https://www.jianshu.com/p/5b01f4d6e4f4)       [CSDN](https://blog.csdn.net/smile__dream/article/details/105681018)  [掘金](https://juejin.im/post/5ea569aaf265da47e34c19ed) 


如果使用过程遇到什么问题或者疑问欢迎提交issue,也欢迎star!
**联系方式**QQ916379012

**Android IM反馈交流群**
QQ群：1051018406

![image](https://github.com/mrchengwenlong/NettyIM/blob/master/50327b1d735eb106d6c94f40edfbbc7.jpg)
