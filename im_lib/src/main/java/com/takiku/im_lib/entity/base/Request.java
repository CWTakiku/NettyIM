package com.takiku.im_lib.entity.base;

import android.telecom.StatusHints;

import com.google.protobuf.GeneratedMessageV3;

public  class Request {

  public static final int PACK_MSG_TYPE=0;
  public static final int PACK_REPLY_TYPE=1;
  public static final int PACK_HANDS_TYPE=3;
  public static final int PACK_CONNECT_TYPE=4;

 public String requestTag;//请求tag，与应答的tag对呀
 public Address address; //暂时未用到，为预留字段
 public boolean sendRetry;//失败是否重试
 public boolean needResponse;//是否需要响应
 public com.google.protobuf.GeneratedMessageV3 requestBody;

  Request(Builder builder) {
     this.address=builder.address;
     this.requestBody=builder.body;
     this.requestTag=builder.requestTag;
     this.sendRetry=builder.sendRetry;
     this.needResponse=builder.needResponse;
  }
    public static class Builder {
    Address address;
    com.google.protobuf.GeneratedMessageV3 body;
    String requestTag;
    boolean sendRetry=true;
    public boolean needResponse;//是否需要响应


    public Builder() {
     needResponse=true;
     sendRetry=true;
    }
    Builder(Request request) {
      this.address=request.address;
      this.body = request.requestBody;
      this.requestTag=request.requestTag;
      this.sendRetry= request.sendRetry;
      this.needResponse=request.needResponse;
    }
    public Builder setAddress(Address address){
      this.address=address;
      return this;
    }
    public Builder setBody(com.google.protobuf.GeneratedMessageV3 body){
      this.body=body;
      return this;
    }
    public Builder setRequestTag(String tag){
        this.requestTag=tag;
        return this;
    }
    public Builder setSendRetry(boolean sendRetry){
        this.sendRetry=sendRetry;
        return this;
    }
    public Builder setNeedResponse(boolean needResponse){
        this.needResponse=needResponse;
        return this;
    }
    public Request build(){
      return new Request(this);
    }
  }
}
