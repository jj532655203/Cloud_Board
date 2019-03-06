package com.jay.cloud_board.meeting_protocal;

import com.jay.cloud_board.base.Constant;

import java.io.Serializable;

/**
 * Created by jj on 2019/3/5.
 */

public class ProxyProtocol implements Serializable {

    private static final long serialVesionUid = Constant.SERIAL_UID_PROXY_PROTOCOL;
    private int protocolType;

    public int getProtocolType() {
        return protocolType;
    }

    public void setProtocolType(int protocolType) {
        this.protocolType = protocolType;
    }

    @Override
    public String toString() {
        return "ProxyProtocol{" +
                "protocolType=" + protocolType +
                '}';
    }
}
