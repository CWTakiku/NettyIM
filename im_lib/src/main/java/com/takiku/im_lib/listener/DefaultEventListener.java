package com.takiku.im_lib.listener;

import com.takiku.im_lib.call.Call;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * 默认事件处理，这里只是打印了日志
 */
public class DefaultEventListener extends EventListener {
    @Override
    public void connectionBroken() {
        super.connectionBroken();
        System.out.println("connectionBroken");
    }

    @Override
    public void connectStart(InetSocketAddress inetSocketAddress) {
        super.connectStart(inetSocketAddress);
        System.out.println("connectStart "+inetSocketAddress.toString());
    }

    @Override
    public void connectSuccess() {
        super.connectSuccess();
        System.out.println("connectSuccess ");
    }

    @Override
    public void sendMsgStart(Call call) {
        super.sendMsgStart(call);
        System.out.println("sendMsgStart");
    }

    @Override
    public void sendMsgEnd(Call call) {
        super.sendMsgEnd(call);
        System.out.println("sendMsgEnd");
    }

    @Override
    public void connectionException(Throwable throwable) {
        super.connectionException(throwable);
        System.out.println("connectionException");
    }

    @Override
    public void sendMsgFailed(Call call) {
        super.sendMsgFailed(call);
        System.out.println("sendMsgFailed");
    }

    @Override
    public void connectFailed(InetSocketAddress inetSocketAddress, IOException ioe) {
        super.connectFailed(inetSocketAddress, ioe);
        System.out.println("connectFailed");
    }
}
