package com.takiku.nettyim;

import androidx.multidex.MultiDex;

/**
 * @author chengwl
 * @des
 * @date:2022/11/14
 */
public class IMApplication extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();
        MultiDex.install(this);
    }
}
