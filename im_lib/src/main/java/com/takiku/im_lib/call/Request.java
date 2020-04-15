package com.takiku.im_lib.call;

import com.google.protobuf.GeneratedMessageLite;
import com.google.protobuf.GeneratedMessageV3;
import com.takiku.im_lib.entity.Address;
import com.takiku.im_lib.entity.base.AbstractBody;
import com.takiku.im_lib.entity.base.AbstractPack;

public final class Request {

  public static final int PACK_MSG_TYPE=0;
  public static final int PACK_REPLY_TYPE=1;
    public static final int PACK_HANDS_TYPE=3;

 public Address address;
 public   AbstractPack body;
 com.google.protobuf.GeneratedMessageV3 value;

  Request(Builder builder) {
     this.address=builder.address;
     this.body=builder.body;
  }

    public GeneratedMessageV3 getValue() {
        return value;
    }

    public void setValue(GeneratedMessageV3 value) {
        this.value = value;
    }

    public static class Builder {
    Address address;
    AbstractPack body;


    public Builder() {

    }
    Builder(Request request) {
      this.address=request.address;
      this.body = request.body;
    }
    public Builder setAddress(Address address){
      this.address=address;
      return this;
    }
    public Builder setBody(AbstractPack abstractPack){
      this.body=abstractPack;
      return this;
    }
    public Request build(){
      if (address == null) throw new IllegalStateException("address == null");
      return new Request(this);
    }
  }
}
