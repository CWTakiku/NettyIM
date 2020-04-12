package com.takiku.im_lib.entity;

import java.net.Proxy;
import java.net.SocketAddress;

/**
 * author:chengwl
 * Description:
 * Date:2020/4/10
 */
public class Address {
    private String url;
    private int port;
    private Type type;
    private SocketAddress sa;

    public Address(String url,int port,Type type){
        this.url=url;
        this.port=port;
        this.type=type;
    }

    public enum Type{
        HTTP,
        SOCKS
    }

    public SocketAddress address() {
        return sa;
    }
    public Type type() {
        return type;
    }

    public String getUrl() {
        return url == null ? "" : url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
