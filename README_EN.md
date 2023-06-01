# NettyIM SDK
### A highly customized communication SDK based on Netty, which supports communication of TCP, UDP and WebSocket protocols.



 [中文文档](https://github.com/CWTakiku/NettyIM/blob/master/README.md)

[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg)](https://github.com/CWTakiku/NettyIM/pulls)      [![License](https://img.shields.io/badge/license-MIT-blue.svg)](https://github.com/CWTakiku/NettyIM/blob/master/LICENSE) [![](https://www.jitpack.io/v/CWTakiku/NettyIM.svg)](https://www.jitpack.io/#CWTakiku/NettyIM)

### I. Feature

1. Support TCP protocols
2. Support websocket ws and wss protocols
3. Support UDP protocols
4. A set of default private protocol implementation is built in
5. Disconnection and connection retry are supported
6. Automatic address switching
7. Supports message resending and message confirmation mechanisms
8. Support the heartbeat mechanism
9.  protocols support handshake authentication
10. Provide Netty message processor registration
11. Support custom codecs
12. Monitor connection status and message status
13. You can set whether a confirmation packet is required for a single message
14. Various parameter Settings are supported

### II.  Classic Case
1. Use IM communication
2. Embedded device communication
### III. Reference library
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
### IV. Use

 ```

// Add network permissions
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.INTERNET" />

 ```
#### 1. Configure the client
So default can be replaced by the developer's implementation, as long as the corresponding interface is implemented
- Common configuration of multiple protocols

 ```
   IMClient.Builder builder = new IMClient.Builder()
                .setConnectTimeout(10, TimeUnit.SECONDS) // Set the connection timeout
                .setResendCount(3)// Sets the number of retry attempts Whether
                .setConnectRetryInterval(1000,TimeUnit.MILLISECONDS)/// set the connection retry time interval
                .setConnectionRetryEnabled(true)// connection retry
                .setSendTimeout(6,TimeUnit.SECONDS)// Sets the sending timeout
                .setHeartIntervalBackground(30,TimeUnit.SECONDS)//heartbeat interval  the background
                .setReaderIdleTimeBackground(90,TimeUnit.SECONDS)//Background read idle trigger time，(Referring to not receiving any message from the server within a certain period of time, it is considered that the network is abnormal or the server is abnormal, if setReaderIdleReconnectEnabled(true) triggers reconnection)
                .setEventListener(eventListener!=null?eventListener:new DefaultEventListener(userId)) // Event listener, optional
                .setMsgTriggerReconnectEnabled(true)  //Whether message sending triggers reconnection if the connection has been disconnected
                .setReaderIdleReconnectEnabled(true) //Whether reading idle will trigger reconnection
                .setProtocol(protocol) //What kind of protocol，IMProtocol.PRIVATE、IMProtocol.WEB_SOCKET、IMProtocol.UDP
                .setOpenLog(true);//Whether to enable logs
 ```
- TCP configuration
 ```
  if (protocol == IMProtocol.PRIVATE){ //The following two data formats are supported: protobuf and string
            builder.setCodec(codecType == 0?new DefaultTcpProtobufCodec():new DefaultTcpStringCodec())//The default codec, developers can use their own protobuf or other codec formats
                    .setShakeHands(codecType == 0? new DefaultProtobufMessageShakeHandsHandler(getDefaultTcpHands()):new DefaultStringMessageShakeHandsHandler(getDefaultStringHands())) //Set handshake authentication. Optional
                    .setHeartBeatMsg(codecType == 0? getDefaultProtobufHeart(): getDefaultStringHeart()) //Setting the heartbeat is optional
                    .setAckConsumer(codecType == 0?new DefaultProtobufAckConsumer():new DefaultStringAckConsumer()) //Set the message confirmation mechanism
                    .registerMessageHandler(codecType == 0?new DefaultProtobufMessageReceiveHandler(onMessageArriveListener):new DefaultStringMessageReceiveHandler(onMessageArriveListener)) //Message receiving processor
                    .registerMessageHandler(codecType == 0?new DefaultReplyReceiveHandler(onReplyListener):new DefaultStringMessageReplyHandler(onReplyListener)) //Message status receiving processor
                    .registerMessageHandler(codecType == 0?new DefaultProtobufHeartbeatRespHandler():new DefaultStringHeartbeatRespHandler()) //Heartbeat receiving processor
                    .setTCPLengthFieldLength(2)//the length of the prepended length field. Only 1, 2, 3, 4, and 8 are allowed.
                    .addAddress(new Address(ip,9081,Address.Type.TCP))
                    .setMaxFrameLength(65535*100); //The maximum frame length is set for tcp and websocket

        }
 ```
 - WebSocket configuration
 ```
   builder.setHeartBeatMsg(getDefaultWsHeart())
                    .setAckConsumer(new DefaultWSAckConsumer())
                    .registerMessageHandler(new DefaultWSMessageReceiveHandler(onMessageArriveListener))
                    .registerMessageHandler(new DefaultWSMessageReplyHandler(onReplyListener))
                    .registerMessageHandler(new DefaultWsHeartbeatRespHandler())
                    .addAddress(new Address(ip,8804,Address.Type.WS))
                    .setMaxFrameLength(65535*100)
                  //  .addAddress(new Address(ip,8804,Address.Type.WSS))//The WSS protocol is supported. Add the wss identifier to scheme
                    .addWsHeader("user",userId); //webSocket specific and can be used for authentication
```
- UDP configuration
  
```
 builder.setCodec(new DefaultUdpStringCodec(new InetSocketAddress(ip,8804), CharsetUtil.UTF_8)) //The default String codec, developers can set to their own format
                    .setShakeHands(new DefaultStringMessageShakeHandsHandler(getDefaultStringHands())) //Set handshake authentication. Optional
                    .setHeartBeatMsg(getDefaultStringHeart()) //Heartbeat receiving processor
                    .setAckConsumer(new DefaultStringAckConsumer()) // Set the message acknowledgement mechanism, which is required when message ACK is required
                    .registerMessageHandler(new DefaultStringMessageReceiveHandler(onMessageArriveListener)) //Message receiving processor
                    .registerMessageHandler(new DefaultStringMessageReplyHandler(onReplyListener))
                    .registerMessageHandler(new DefaultStringHeartbeatRespHandler()) 
                    .addAddress(new Address(ip, 8804, Address.Type.UDP));                
  ```

#### 2. Construct the IMClient client
 ```
 IMClient imClient = builder.build();
  ```

#### 3. Establish a connection
 ```
imClient.startConnect(); // Establish a connection
 ```
#### 4. Disconnect the connection
 ```
imClient.disConnect(); // The connection is disconnected actively and will not be reconnected automatically
 ```
#### 5. Send a message
 ```
 Request request=new Request.Builder(). //Create a message sending request       
              setNeedACK(true).//If an ACK is required, true triggers the message acknowledgement mechanism
              setSendRetry(true). //Can send retry
              setBody(getMsgPack(appMessage.buildProto())). //body is the object supported by decoding
              build();
 ```
 ```
imClient.newCall(request).enqueue(callback); // Send the message, the message in the sub thread callback
 ```
 ```
Disposable disposable=   imClient.newCall(request).enqueue(callback).subscribe(consumer);  // Sending messages subscribes to specific message processing. For example: I send a particular message and then want to subscribe to subsequent responses for that particular message
 ```
#### 6. Receive message
All message reception is registered in registerMessageHandler() in the above configuration. Developers can implement the MessageHandler interface themselves

```
public  interface  MessageHandler<message extends Object>  {
    boolean isFocusMsg(Object msg); //Is the type of message that the handler is concerned about
    void handleMsg(message message);//Receive processing message
}
 ```
#### 7.Status monitoring
Status Listener In the setEventListener() configuration above, developers can inherit the EventListener class to listen for callbacks
```

    /**
     * connectStart
     * @param inetSocketAddress
     */
    public  void connectStart( InetSocketAddress inetSocketAddress){

    }

    /**
     * connectSuccess
     */
    public  void connectSuccess(){

    }

    /**
     * connectionException
     * @param throwable
     */
    public void connectionException(Throwable throwable){

    }

    /**
     * connectFailed
     * @param inetSocketAddress
     * @param ioe
     */
    public void connectFailed( InetSocketAddress inetSocketAddress, IOException ioe) {

    }

    /**
     * connectionBroken
     */
    public void connectionBroken(){

    }


    /**
     * connectionReleased
     * @param connection
     */
    public void connectionReleased(Connection connection) {
    }

    /**
     * sendMsgStart
     * @param call
     */
    public void sendMsgStart(Call call) {
    }

    /**
     * sendMsgEnd
     * @param call
     */
    public void sendMsgEnd(Call call) {
    }

    /**
     * sendMsgFailed
     * @param call
     */
    public void sendMsgFailed(Call call){}

```


#### 8. Other apis
 ```
imClient.setBackground(background); // Set the front/background switch, and different heartbeat intervals will be automatically switched
imClient.isConnected(); // Check whether the connection is established
 ```

### V. Project structure design drawing
![image](https://github.com/CWTakiku/NettyIM/blob/master/IMPic.png)

#### VI. Demo use
#### Step 1. Change the server address and run the project
Change localHost to the ip address of your computer in the MainActivity class, run the project, and run the APP on the mobile phone or simulator
#### Step 2 Start the server
APP module test contains the server demo, webscoket protocol, udp protocol and server demo of tcp protocol and string data formats. Run the corresponding server demo
#### Step 3 Use
Click the corresponding protocol in the APP to enter the chat interface
### VII. Project blog address
[Jane books](https://www.jianshu.com/p/5b01f4d6e4f4) 
[CSDN](https://blog.csdn.net/smile__dream/article/details/105681018) 
[Denver](https://juejin.im/post/5ea569aaf265da47e34c19ed)


If you encounter any problems or questions in the use process, you are welcome to submit the issue, and welcome star!
** Contact information **QQ916379012

**Android IM feedback communication Group **
QQ group: 1051018406

![image](https://github.com/mrchengwenlong/NettyIM/blob/master/50327b1d735eb106d6c94f40edfbbc7.jpg)
