package com.takiku.im_lib.internal.connection;

import com.takiku.im_lib.entity.base.Address;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
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
    private final  List<Address> addressList;
    private int nextInetSocketAddressIndex=1;
    private List<InetSocketAddress> inetSocketAddresses=new ArrayList<>();


    public RouteSelector(List<Address> addressList) {
        if (addressList==null||addressList.size()<1){
            throw new NullPointerException("address is null");
        }
        this.addressList=addressList;
        InetSocketAddress inetSocketAddress=null;
        for (Address address:addressList){
            if (address.type()==Address.Type.SOCKS){
                inetSocketAddress=InetSocketAddress.createUnresolved(address.getUrl(),address.getPort());
            }else if (address.type()==Address.Type.WS){
                inetSocketAddress=new InetSocketAddress(address.getUrl(),address.getPort());
            }
            inetSocketAddresses.add(inetSocketAddress);
        }
        lastInetSocketAddress=inetSocketAddresses.get(0);
    }
    public InetSocketAddress lastInetSocketAddress(){
        return lastInetSocketAddress;
    }

    public boolean hasNext() {
        return hasNextInetSocketAddress();

    }
    /** Returns true if there's another socket address to try. */
    private boolean hasNextInetSocketAddress() {
        return nextInetSocketAddressIndex <inetSocketAddresses.size();
    }
    /** Returns the next socket address to try. */
    public InetSocketAddress nextInetSocketAddress() throws IOException {

        if (!hasNextInetSocketAddress()) {
                throw new SocketException("No route to " + lastInetSocketAddress.getAddress()
                    + "; exhausted inet socket addresses: " + inetSocketAddresses);
        }
        lastInetSocketAddress=inetSocketAddresses.get(nextInetSocketAddressIndex++);
        return lastInetSocketAddress;
    }


}
