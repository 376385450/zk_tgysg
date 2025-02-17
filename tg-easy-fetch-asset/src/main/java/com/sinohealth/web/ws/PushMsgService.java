package com.sinohealth.web.ws;

public interface PushMsgService {

    void pushMsgToUser(String userId, String msg);

    void pushMsgToAllUsers(String msg);
}
