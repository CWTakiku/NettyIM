package com.takiku.im_lib.entity.base;

/**
 * author:chengwl
 * Description:
 * Date:2020/4/10
 */
public abstract class AbstractPack<T extends AbstractBody> {

    public static final  int MSG_TYPE=0;
    public static final  int MSG_REPLY_TYPE=1;
    public static final  int MSG_HEART_TYPE=2;


    private int packType;
    private T abstractMessage;

    public int getPackType() {
        return packType;
    }

    public void setPackType(int packType) {
        this.packType = packType;
    }

    public T getAbstractMessage() {
        return abstractMessage;
    }

    public void setAbstractMessage(T abstractMessage) {
        this.abstractMessage = abstractMessage;
    }
}
