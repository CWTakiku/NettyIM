package com.takiku.im_lib.internal.connection;

import com.takiku.im_lib.entity.Address;

import java.net.InetSocketAddress;

public class Route {
    final Address address;
     InetSocketAddress inetSocketAddress;
    public Route(Address address,InetSocketAddress inetSocketAddress){
        if (address == null) {
            throw new NullPointerException("address == null");
        }
        this.inetSocketAddress=inetSocketAddress;
        this.address = address;
    }
    public Address address() {
        return address;
    }
    public InetSocketAddress socketAddress() {
        return inetSocketAddress;
    }
}
