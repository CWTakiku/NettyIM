package com.takiku.im_lib.internal.connection;

import com.takiku.im_lib.entity.base.Address;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * author:chengwl
 * Description:
 * Date:2020/4/11
 */
public final class ConnectionPool {




    private  volatile ExecutorService workPool ;// 工作线程组，负责心跳
    private   volatile   RealConnection realConnection;



    public ConnectionPool(){

    }
    public RealConnection realConnection(){
        return realConnection;
    }


    public void deduplicate(){
        if (realConnection!=null){
            realConnection.release(false);
            realConnection=null;
        }
    }
    public void put(RealConnection connection) {
        assert (Thread.holdsLock(this));
        realConnection=connection;
    }


    public void changeHeartbeatInterval(int heartbeatInterval,int readerIdleTime,boolean readerIdleReconnectEnabled){
        if (realConnection==null||!realConnection.isHealth()){
            try {
                throw new Exception("changeHeartbeatInterval is failure,because connection broken");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        realConnection.addHeartbeatHandler(this,heartbeatInterval,readerIdleTime,readerIdleReconnectEnabled);
    }




    /**
     * 执行心跳任务
     *
     * @param r
     */
    public synchronized void execWorkTask(Runnable r) {
        if (r == null){
            return;
        }
        if (workPool==null){
            workPool= Executors.newFixedThreadPool(1);
        }
        workPool.execute(r);
    }

    public RealConnection get(Address address, StreamAllocation streamAllocation) {
        if (realConnection!=null){
            streamAllocation.acquire(realConnection);
        }
        return realConnection;
    }
    /**
     * 释放work线程池
     */
    public synchronized void destroyWorkLoopGroup() {
        if (workPool != null) {
            try {
                workPool.shutdownNow();
            } catch (Throwable t) {
                t.printStackTrace();
            } finally {
                workPool = null;
            }
        }
    }
}
