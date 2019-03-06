package com.jay.cloud_board.meeting_protocal;

import java.io.Serializable;

/**
 * desc:登录/切换账号 协议
 * Created by jj on 2019/3/3.
 */

public class LoginProtocol implements Serializable {

    private String userId;
    private int protocolType;

    public LoginProtocol() {
    }

    public LoginProtocol(String userId, int protocolType) {
        this.userId = userId;
        this.protocolType = protocolType;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getProtocolType() {
        return protocolType;
    }

    public void setProtocolType(int protocolType) {
        this.protocolType = protocolType;
    }

    @Override
    public String toString() {
        return "ProtocolShell{" +
                "userId='" + userId + '\'' +
                ", protocolType='" + protocolType + '\'' +
                '}';
    }
}
