package com.takiku.im_lib.internal.connection;

import com.takiku.im_lib.entity.base.Address;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

/**
 * author:chengwl
 * Description:
 * Date:2020/4/10
 */
public class RouteSelector {
    private InetSocketAddress lastInetSocketAddress;
    private RouteDatabase routeDatabase;
    private Address currentAddress;
    private final  List<Address> addressList;
    private int nextInetSocketAddressIndex;
    private List<InetSocketAddress> inetSocketAddresses=new ArrayList<>();


    public RouteSelector(List<Address> addressList) {
        this.routeDatabase=routeDatabase;
        this.addressList=addressList;
        for (Address address:addressList){
            if (address.type()==Address.Type.HTTP){
                this.lastInetSocketAddress=InetSocketAddress.createUnresolved(address.getUrl(),address.getPort());
            }else if (address.type()==Address.Type.SOCKS){
                this.lastInetSocketAddress=new InetSocketAddress(address.getUrl(),address.getPort());
            }
            inetSocketAddresses.add(lastInetSocketAddress);
        }
    }
    public InetSocketAddress lastInetSocketAddress(){
        return lastInetSocketAddress;
    }

    public boolean hasNext() {
        return hasNextInetSocketAddress();

    }
    /** Returns true if there's another socket address to try. */
    private boolean hasNextInetSocketAddress() {
        return nextInetSocketAddressIndex < inetSocketAddresses.size();
    }
    /** Returns the next socket address to try. */
    public InetSocketAddress nextInetSocketAddress() throws IOException {
        if (!hasNextInetSocketAddress()) {
                throw new SocketException("No route to " + lastInetSocketAddress.getAddress()
                    + "; exhausted inet socket addresses: " + inetSocketAddresses);
        }
        return inetSocketAddresses.get(nextInetSocketAddressIndex++);
    }


}
