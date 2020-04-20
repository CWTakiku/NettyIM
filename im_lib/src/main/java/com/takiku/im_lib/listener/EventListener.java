package com.takiku.im_lib.listener;

import com.takiku.im_lib.call.Call;
import com.takiku.im_lib.dispatcher.Connection;
import com.takiku.im_lib.entity.base.Request;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;

/**
 * author:chengwl
 * Description:事件监听
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

}
