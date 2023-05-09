# NettyIM SDK
#### A highly customized long-connect SDK based on Netty that supports communication between proprietary and Websocket protocols.



 [中文文档](https://github.com/CWTakiku/NettyIM/blob/master/README.md)

[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg)](https://github.com/CWTakiku/NettyIM/pulls)      [![License](https://img.shields.io/badge/license-MIT-blue.svg)](https://github.com/CWTakiku/NettyIM/blob/master/LICENSE) [![](https://www.jitpack.io/v/CWTakiku/NettyIM.svg)](https://www.jitpack.io/#CWTakiku/NettyIM)

#### I. Feature

1. Support user-defined private protocols
2. Support websocket ws and wss protocols
3. A set of default private protocol implementation is built in
4. Disconnection and connection retry are supported
5. Automatic address switching
6. Supports message resending and message confirmation mechanisms
7. Support the heartbeat mechanism
8. User-defined protocols support handshake authentication
9. Provide Netty message processor registration
10. Proprietary protocols support custom codecs
11. Monitor connection status and message status
12. You can set whether a confirmation packet is required for a single message
13. Various parameter Settings are supported

#### II.  Classic Case
1. Use IM communication
2. Embedded device communication
#### III. Reference library
1. Add the warehouse address
```
allprojects {
repositories {
.
maven { url 'https://jitpack.io' }}
}
 ```
2. Add dependencies
 ```
dependencies {
  implementation 'com.github.CWTakiku:NettyIM:latest'
}
 ```
#### IV. Use

 ```

// Add network permissions
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.INTERNET" />

 ```
1. Configure the client
- Common configurations of private protocols

 ```
// So default can be replaced by the developer's implementation, as long as the corresponding interface is implemented
                imClient=new IMClient.Builder()
                .setCodec (new DefaultCodec()) // Default codec. Developers can use their own protobuf codec if it is a proprietary protocol
                .setShakeHands (new DefaultMessageShakeHandsHandler (getDefaultHands ())) // set the handshake authentication, optional
                .setHeartBeatMsg (getDefaultHeart()) // Set heartbeat to an object supported by the decoder. It is optional
                .setAckConsumer(new DefaultAckConsumer()) // Sets the consumer of the message acknowledgement packet, which affects whether the sent message times out
                .setConnectTimeout(10, TimeUnit.SECONDS) // Set the connection timeout
                .setResendCount(3)// Sets the number of retry attempts Whether
                .setConnectionRetryEnabled (true) // connection retry
                .setSendTimeout(6,TimeUnit.SECONDS)// Sets the sending timeout
                .setHeartIntervalBackground (30, TimeUnit. SECONDS) //heartbeat interval  the background
                .registerMessageHandler (new DefaultMessageReceiveHandler (onMessageArriveListener)) // message receiving processor
                .registerMessageHandler (new DefaultReplyReceiveHandler (onReplyListener)) // message states receiving processor
                .registerMessageHandler (new DefaultHeartbeatRespHandler ()) // heart receiving place
                .setEventListener(new DefaultEventListener("user id1")) // Event listener, optional
                .addAddress(new Address("192.168.69.32",8766, Address.Type.SOCKS))
                .addAddress(new Address("www.baidu.com",8766,Address.Type.SOCKS))
                .setProtocol(IMProtocol.PRIVATE)// Protocol type
                .setMaxFrameLength(65535)// Maximum frame length
                .setMsgTriggerReconnectEnabled (true) //whether/set message trigger reconnection (default true)
                .setConnectRetryInterval (500, TimeUnit. MILLISECONDS) // set the connection retry time interval
                .setOpenLog(true) // Open SDK internal logs
                .build();
 ```

- Additional configuration items of Websocket protocol

 ```
IMClient.Builder().addAddress (new Address (" ws: / / 192.168.69.32:8804 / ws ", 8804, the Address, the ws)) / / websocket Address
                  .addAddress (new Address (WSS: / / test. Domain: 8804 / WSS ", 8804, the Address, the WS)) / / WSS protocol Address
                 .addWsHeader("user","userId1") //ws header
                 .setProtocol(IMProtocol.WEB_SOCKET); // Set to webscoket protocol
 ```
2. Establish a connection
 ```
imClient.startConnect(); // Establish a connection
 ```
3. Disconnect the connection
 ```
imClient.disConnect(); // The connection is disconnected actively and will not be reconnected automatically
 ```
4. Send a message
 ```
request request=new request.builder (). // Create a message to send Request
setNeedACK(true).// ACK is required
setSendRetry(true). // Can send retry
setBody(getMsgPack(appMessage.buildProto())). //body is the object supported for decoding
build();
 ```
 ```
imClient.newCall(request).enqueue(callback); // Send the message, the message in the child thread callback
 ```
 ```
Disposable disposable=   imClient.newCall(request).enqueue(callback).subscribe(consumer);  // To send a message, subscribe to a specific message processing
 ```
5. Other apis
 ```
imClient.setBackground(background); // Set the front/background switch, and different heartbeat intervals will be automatically switched
imClient.isConnected(); // Check whether the connection is established
.
 ```

#### V. Project structure design drawing
![image](https://github.com/CWTakiku/NettyIM/blob/master/IMPic.png)

#### VI. Demo use
APP module test contains the background code of the built-in custom protocol and webscoket protocol. Start the server, modify the server IP on the client of the corresponding protocol, and run the client

#### VII. Project blog address
[Jane books](https://www.jianshu.com/p/5b01f4d6e4f4) 
[CSDN](https://blog.csdn.net/smile__dream/article/details/105681018) 
[Denver](https://juejin.im/post/5ea569aaf265da47e34c19ed)


If you encounter any problems or questions in the use process, you are welcome to submit the issue, and welcome star!
** Contact information **QQ916379012

**Android IM feedback communication Group **
QQ group: 1051018406

![image](https://github.com/mrchengwenlong/NettyIM/blob/master/50327b1d735eb106d6c94f40edfbbc7.jpg)
