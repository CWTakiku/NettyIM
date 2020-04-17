package com.takiku.im_lib.listener;

import com.takiku.im_lib.call.Call;
import com.takiku.im_lib.dispatcher.Connection;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;

/**
 * author:chengwl
 * Description:
 * Date:2020/4/11
 */
public abstract class EventListener {
    public static final EventListener NONE = new EventListener() {
    };
    public static EventListener.Factory factory(final EventListener listener) {
        return new EventListener.Factory() {
            public EventListener create(Call call) {
                return listener;
            }
        };
    }

    public interface Factory {
        EventListener create(Call call);
    }


    public  void connectStart( InetSocketAddress inetSocketAddress){

    }
    public  void connectSuccess(){

    }

    /**
     * 
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

    public void connectionAcquired(Call call, Connection connection) {
    }
    public void connectionReleased(Call call, Connection connection) {
    }
    public void callFailed(Call call, IOException ioe) {
    }
    public void callStart(Call call) {
    }
    public void sendMsgStart(Call call) {
    }
    public void sendMsgEnd(Call call) {
    }

}
