package com.takiku.im_lib.internal.connection;

import com.takiku.im_lib.entity.Address;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * author:chengwl
 * Description:
 * Date:2020/4/10
 */
public class RouteSelector {
    private InetSocketAddress lastInetSocketAddress;
    private RouteDatabase routeDatabase;
    private final Address address;
    private int nextInetSocketAddressIndex;
    private List<InetSocketAddress> inetSocketAddresses=new ArrayList<>();


    public RouteSelector(Address address, RouteDatabase routeDatabase) {
        this.routeDatabase=routeDatabase;
        this.address=address;
        if (address.type()==Address.Type.HTTP){
            this.lastInetSocketAddress=InetSocketAddress.createUnresolved(address.getUrl(),address.getPort());
        }else if (address.type()==Address.Type.SOCKS){
            this.lastInetSocketAddress=new InetSocketAddress(address.getUrl(),address.getPort());
        }
        inetSocketAddresses.add(lastInetSocketAddress);

    }

    public boolean hasNext() {
        return hasNextInetSocketAddress();

    }
    /** Returns true if there's another socket address to try. */
    private boolean hasNextInetSocketAddress() {
        return nextInetSocketAddressIndex < inetSocketAddresses.size();
    }
    /** Returns the next socket address to try. */
    private InetSocketAddress nextInetSocketAddress() throws IOException {
        if (!hasNextInetSocketAddress()) {
                throw new SocketException("No route to " + address.getUrl()
                    + "; exhausted inet socket addresses: " + inetSocketAddresses);
        }
        return inetSocketAddresses.get(nextInetSocketAddressIndex++);
    }

    public Route next() throws IOException {
        lastInetSocketAddress=nextInetSocketAddress();
        Route route = new Route(address, lastInetSocketAddress);
        if (routeDatabase.shouldPostpone(route)){
            return next();
        }
        return route;
    }
}
