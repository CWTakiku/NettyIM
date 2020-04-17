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



    private static final ExecutorService bossPool = Executors.newFixedThreadPool(1);
    private static final ExecutorService workPool = Executors.newFixedThreadPool(1);;// 工作线程组，负责心跳
    private  RealConnection realConnection;



    public ConnectionPool(){

    }


    public void deduplicate(StreamAllocation streamAllocation){
        realConnection=null;
        if (streamAllocation!=null){
            streamAllocation.release();
        }
    }
    public void put(RealConnection connection) {
        assert (Thread.holdsLock(this));
        realConnection=connection;
    }




    /**
     * 执行心跳任务
     *
     * @param r
     */
    public void execWorkTask(Runnable r) {
        workPool.execute(r);
    }

    public RealConnection get(Address address, StreamAllocation streamAllocation) {
        if (realConnection!=null){
            streamAllocation.acquire(realConnection);
        }
        return realConnection;
    }
}
