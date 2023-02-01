package com.takiku.im_lib.entity.base;

import android.telecom.StatusHints;

import com.google.protobuf.ByteOutput;
import com.google.protobuf.GeneratedMessageV3;

public  class Request {

  public static final int PACK_MSG_TYPE=0;
  public static final int PACK_REPLY_TYPE=1;
  public static final int PACK_HANDS_TYPE=3;
  public static final int PACK_CONNECT_TYPE=4;
  public static final int PACK_ACK_TYPE = 5;
  public static final int PACK_HEART_TYPE = 6;

 public String requestTag;//请求tag，这个tag能确定唯一的request
 public Address address; //暂时未用到，为预留字段
 public boolean sendRetry;//失败是否重试
 public boolean needACK=true;//是否需要确认
 public Object requestBody;

  Request(Builder builder) {
     this.address=builder.address;
     this.requestBody=builder.body;
     this.requestTag=builder.requestTag;
     this.sendRetry=builder.sendRetry;
     this.needACK=builder.needACK;
  }
    public static class Builder {
    Address address;
    Object body;
    String requestTag;
    boolean sendRetry;
    boolean needACK;

    public Builder() {
        sendRetry = true;
        needACK = true;
    }
    Builder(Request request) {
      this.address=request.address;
      this.body = request.requestBody;
      this.requestTag=request.requestTag;
      this.sendRetry= request.sendRetry;
    }
    public Builder setAddress(Address address){
      this.address=address;
      return this;
    }
    public Builder setBody(Object body){
      this.body=body;
      return this;
    }

    public Builder setSendRetry(boolean sendRetry){
        this.sendRetry=sendRetry;
        return this;
    }
        public Builder setNoNeedACK(){
            this.needACK = false;
            return this;
        }
    public Builder setNeedACK(String requestTag){
        this.needACK = true;
        this.requestTag = requestTag;
        return this;
    }
    public Request build(){
      return new Request(this);
    }
  }
}
