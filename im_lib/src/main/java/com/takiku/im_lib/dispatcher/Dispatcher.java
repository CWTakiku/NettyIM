package com.takiku.im_lib.dispatcher;

import androidx.annotation.Nullable;

import com.takiku.im_lib.call.RealCall;
import com.takiku.im_lib.util.Util;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Dispatcher {
    private int maxRequests = 64;
    private @Nullable Runnable idleCallback;
    /** Executes calls. Created lazily. */
    private @Nullable ExecutorService executorService;
    /** Running asynchronous calls. Includes canceled calls that haven't finished yet. */
    private final Deque<RealCall.AsyncCall> runningAsyncCalls = new ArrayDeque<>();
    private final Deque<RealCall.AsyncCall> readyAsyncCalls = new ArrayDeque<>();

   public void finished(RealCall.AsyncCall call) {
        finished(runningAsyncCalls, call, true);
    }
    public  <T> void finished(Deque<T> calls, T call, boolean promoteCalls) {
        int runningCallsCount;
        Runnable idleCallback;
        synchronized (this) {
            if (!calls.remove(call)) throw new AssertionError("Call wasn't in-flight!");
            if (promoteCalls) promoteCalls();
            runningCallsCount = runningCallsCount();
            idleCallback = this.idleCallback;
        }

        if (runningCallsCount == 0 && idleCallback != null) { //没有运行的任务,则运行空闲任务
            idleCallback.run();
        }
    }
    public synchronized int runningCallsCount() {
        return runningAsyncCalls.size();
    }
    private void promoteCalls() {
        if (runningAsyncCalls.size() >= maxRequests) return; // Already running max capacity.
        if (readyAsyncCalls.isEmpty()) return; // No ready calls to promote.

        for (Iterator<RealCall.AsyncCall> i = readyAsyncCalls.iterator(); i.hasNext(); ) {
            RealCall.AsyncCall call = i.next();
            i.remove();
            runningAsyncCalls.add(call);
            executorService().execute(call);
            if (runningAsyncCalls.size() >= maxRequests) return; // Reached max capacity.
        }
    }
    public synchronized ExecutorService executorService() {
        if (executorService == null) {
            executorService = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60, TimeUnit.SECONDS,
                    new SynchronousQueue<Runnable>(), Util.threadFactory("IMClient Dispatcher", false));
        }
        return executorService;
    }


        public synchronized void enqueue(RealCall.AsyncCall call) {
            if (runningAsyncCalls.size() < maxRequests) {
                runningAsyncCalls.add(call);
                executorService().execute(call);
            } else {
                readyAsyncCalls.add(call);
            }
        }

}
