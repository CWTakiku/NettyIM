package com.takiku.nettyim;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {

    private static volatile SessionManager instance = null;
    // netty生成的sessionID和Session的对应关系
    private Map<String, Session> sessionIdMap;
    // userId和netty生成的sessionID的对应关系
    private Map<String, String> userToSessionIdMap;


    public SessionManager() {
        this.sessionIdMap = new ConcurrentHashMap<>();
        this.userToSessionIdMap = new ConcurrentHashMap<>();
    }


    public static SessionManager getInstance() {
        if (instance == null) {
            synchronized (SessionManager.class) {
                if (instance == null) {
                    instance = new SessionManager();
                }
            }
        }
        return instance;
    }

    public boolean containsKey(String sessionId) {
        return sessionIdMap.containsKey(sessionId);
    }

    public boolean containsSession(Session session) {
        return sessionIdMap.containsValue(session);
    }

    public Session getBySessionId(String sessionId) {
        return sessionIdMap.get(sessionId);
    }
    public Session getByUserId(String userId){
        if (userToSessionIdMap.containsKey(userId)){
            String sessionId=userToSessionIdMap.get(userId);
            return sessionIdMap.get(sessionId);
        }else {
            return null;
        }
    }

    public synchronized Session removeBySessionId(String sessionId) {
        if (sessionId == null)
            return null;
        Session session = sessionIdMap.remove(sessionId);
        if (session == null)
            return null;
        if (session.getUserId() != null)
            this.userToSessionIdMap.remove(session.getUserId());
        return session;
    }

    public synchronized Session put(String key, Session value) {
        if (value.getUserId() != null && !"".equals(value.getUserId().trim())) {
            this.userToSessionIdMap.put(value.getUserId(), value.getId());
        }
        return sessionIdMap.put(key, value);
    }
}
