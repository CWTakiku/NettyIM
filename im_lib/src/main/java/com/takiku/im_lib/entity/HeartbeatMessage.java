package com.takiku.im_lib.entity;

import com.takiku.im_lib.entity.base.AbstractPack;
import com.takiku.im_lib.entity.base.Request;

/**
 * @author chengwl
 * @des
 * @date:2023/1/31
 */
public class HeartbeatMessage extends AbstractPack {
    public HeartbeatMessage() {
        super(Request.PACK_HEART_TYPE);
    }
}
