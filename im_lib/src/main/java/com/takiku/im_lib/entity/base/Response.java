package com.takiku.im_lib.entity.base;

import com.google.protobuf.GeneratedMessageV3;

public class Response<T extends com.google.protobuf.GeneratedMessageV3> {
   public int code;
   public Request request;
   public T  body;

   public static final int SUCCESS=0;
   public static final int NO_RESPONSE=-1;//无响应
   public static final int CONNECT_FAILED=-2;

   public Response(Builder builder){
       this.code=builder.code;
       this.request=builder.request;
       this.body= (T) builder.response;
   }


    public static class Builder<T extends com.google.protobuf.GeneratedMessageV3>{
        private Request request;
        private T response;
        private int code;

        public Builder setRequest(Request request) {
            this.request = request;
            return this;
        }

        public Builder setResponse(T response) {
            this.response = response;
            return this;
        }
        public Builder setCode(int code){
            this.code=code;
            return this;
        }
        public Response build(){
            return new Response(this);
        }
    }

}
