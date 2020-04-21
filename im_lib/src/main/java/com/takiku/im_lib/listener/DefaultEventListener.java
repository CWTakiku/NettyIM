package com.takiku.im_lib.listener;

import android.telecom.StatusHints;

import com.takiku.im_lib.call.Call;
import com.takiku.im_lib.dispatcher.Connection;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * 默认事件处理，这里只是打印了日志
 */
public class DefaultEventListener extends EventListener {
    private String userId;
    public DefaultEventListener(String tag){
        userId=tag;
    }
    @Override
    public void connectionBroken() {
        super.connectionBroken();
        System.out.println(userId+"connectionBroken");
    }

    @Override
    public void connectStart(InetSocketAddress inetSocketAddress) {
        super.connectStart(inetSocketAddress);
        System.out.println(userId+" connectStart "+inetSocketAddress.toString());
    }

    @Override
    public void connectSuccess() {
        super.connectSuccess();
        System.out.println(userId+" connectSuccess ");
    }

    @Override
    public void sendMsgStart(Call call) {
        super.sendMsgStart(call);
        System.out.println(userId+" sendMsgStart");
    }

    @Override
    public void sendMsgEnd(Call call) {
        super.sendMsgEnd(call);
        System.out.println(userId+" sendMsgEnd");
    }

    @Override
    public void connectionException(Throwable throwable) {
        super.connectionException(throwable);
        System.out.println(userId+" connectionException");
    }

    @Override
    public void sendMsgFailed(Call call) {
        super.sendMsgFailed(call);
        System.out.println(userId +" sendMsgFailed");
    }

    @Override
    public void connectFailed(InetSocketAddress inetSocketAddress, IOException ioe) {
        super.connectFailed(inetSocketAddress, ioe);
        System.out.println(userId+" connectFailed");
    }

    @Override
    public void connectionReleased(Connection connection) {
        super.connectionReleased(connection);
        System.out.println(userId+" connectionReleased");
    }
}
