package com.jay.cloud_board.eventbus;

/**
 * Created by jj on 2019/3/4.
 */

public class NetWorkStateChangedEvent {
    private NetStateType mNetStateType;

    public NetWorkStateChangedEvent(NetStateType netStateType) {

        mNetStateType = netStateType;
    }

    public NetStateType getNetStateType() {
        return mNetStateType;
    }

    public void setNetStateType(NetStateType netStateType) {
        mNetStateType = netStateType;
    }

    public enum NetStateType {
        TYPE_MOBILE_DATA_CONNECTED,
        TYPE_WIFI_CONNECTED,
        TYPE_NONE_CONNECTED
    }
}
