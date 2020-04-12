package com.takiku.im_lib.call;

import com.google.protobuf.GeneratedMessageLite;
import com.takiku.im_lib.entity.Address;
import com.takiku.im_lib.entity.base.AbstractBody;
import com.takiku.im_lib.entity.base.AbstractPack;

public final class Request {

  public static final int PACK_MSG_TYPE=0;
  public static final int PACK_REPLY_TYPE=1;

  Address address;
  AbstractPack body;
  GeneratedMessageLite value;

  public Address getAddress() {
    return address;
  }

  public void setAddress(Address address) {
    this.address = address;
  }

  public AbstractPack getBody() {
    return body;
  }

  public void setBody(AbstractPack body) {
    this.body = body;
  }

  public GeneratedMessageLite getValue() {
    return value;
  }

  public void setValue(GeneratedMessageLite value) {
    this.value = value;
  }
}
