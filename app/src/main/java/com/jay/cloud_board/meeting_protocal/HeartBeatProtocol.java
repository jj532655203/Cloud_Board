package com.jay.cloud_board.meeting_protocal;

/**
 * @Description 心跳协议
 * Created by jj on 2019/3/4.
 */

public class HeartBeatProtocol  {

    private String userId;
    private int protocolType;

    public HeartBeatProtocol() {
    }

    public HeartBeatProtocol(String userId, int protocolType) {
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
