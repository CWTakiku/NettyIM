package com.takiku.im_lib;

import io.netty.channel.Channel;

public class Session {
    private String id;
    private Channel channel = null;
    private String userId;
    private long lastCommunicateTimeStamp = 0l;

    public static String buildId(Channel channel) {
        return channel.id().asLongText();
    }



    public static Session buildSession(Channel channel, String userId) {
        Session session = new Session();
        session.setChannel(channel);
        session.setId(buildId(channel));
        session.setUserId(userId);
        session.setLastCommunicateTimeStamp(System.currentTimeMillis());
        return session;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public long getLastCommunicateTimeStamp() {
        return lastCommunicateTimeStamp;
    }

    public void setLastCommunicateTimeStamp(long lastCommunicateTimeStamp) {
        this.lastCommunicateTimeStamp = lastCommunicateTimeStamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    public void writeAndFlush(Object o){
        if (channel!=null&&channel.isActive()){
            channel.writeAndFlush(o);
        }
    }
}
