package com.takiku.im_lib.util;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.io.Serializable;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;


public class IdWorker {
    private static final AtomicLong NETID_GENERATOR = new AtomicLong(0);
    private static SnowFlake snowFlake;
    private static ConcurrentMap<Serializable, AtomicLong> sessionMap;

    static {
        sessionMap = new ConcurrentHashMap<>();
    }

    public static String uuid() {
        return UUID.randomUUID().toString();
    }

    /**
     * snowFlake
     * for InternalMsg
     *
     * @return
     */
    public static Long snowGenId() {
        return snowFlake.nextId();
    }

    /**
     * consistent id
     * for ChatMsg, AckMsg
     *
     * @return
     */
    public static Long nextId(Serializable connectorId) {
        if (sessionMap.containsKey(connectorId)) {
            return sessionMap.get(connectorId).incrementAndGet();
        } else {
            AtomicLong atomicLong = new AtomicLong(0);
            sessionMap.put(connectorId, atomicLong);
          return   atomicLong.incrementAndGet();
        }
    }
}
