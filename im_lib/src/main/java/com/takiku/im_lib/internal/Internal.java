package com.takiku.im_lib.internal;

import com.takiku.im_lib.client.IMClient;
import com.takiku.im_lib.entity.Address;
import com.takiku.im_lib.internal.connection.ConnectionPool;
import com.takiku.im_lib.internal.connection.RealConnection;
import com.takiku.im_lib.internal.connection.Route;
import com.takiku.im_lib.internal.connection.RouteDatabase;
import com.takiku.im_lib.internal.connection.StreamAllocation;

import java.net.Socket;

import io.netty.channel.Channel;

public abstract class Internal {

  public static void initializeInstanceForTests() {
    // Needed in tests to ensure that the instance is actually pointing to something.
    new IMClient();
  }
  public static Internal instance;

  public abstract RealConnection get(ConnectionPool pool, Address address,
                                     StreamAllocation streamAllocation, Route route);

  public abstract void deduplicate(
          ConnectionPool pool, StreamAllocation streamAllocation);

  public abstract void put(ConnectionPool pool, RealConnection connection);

  public abstract RouteDatabase routeDatabase(ConnectionPool connectionPool);

  public abstract  com.google.protobuf.Internal.EnumLite HeartPack();
}
