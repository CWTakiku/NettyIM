package com.takiku.im_lib.entity.base;

import android.telecom.StatusHints;

import com.google.protobuf.GeneratedMessageV3;

public final class Request {

  public static final int PACK_MSG_TYPE=0;
  public static final int PACK_REPLY_TYPE=1;
  public static final int PACK_HANDS_TYPE=3;
  public static final int PACK_CONNECT_TYPE=4;

 public String requestTag;
 public Address address;
 public boolean sendRetry=true;//失败是否重试
 public com.google.protobuf.GeneratedMessageV3 body;

  Request(Builder builder) {
     this.address=builder.address;
     this.body=builder.body;
     this.requestTag=builder.requestTag;
  }
    public static class Builder {
    Address address;
    com.google.protobuf.GeneratedMessageV3 body;
    String requestTag;
    boolean sendRetry=true;


    public Builder() {

    }
    Builder(Request request) {
      this.address=request.address;
      this.body = request.body;
      this.requestTag=request.requestTag;
      this.sendRetry= request.sendRetry;
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
    public Request build(){
      return new Request(this);
    }
  }
}
