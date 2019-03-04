package com.jay.cloud_board.meeting_protocal;

import com.jay.cloud_board.base.Constant;

/**
 * @Description 心跳协议
 * Created by jj on 2019/3/4.
 */

public class HeartBeatProtocol extends MeetingProtocol {
    public HeartBeatProtocol() {
        setProtocolType(Constant.PROTOCOL_TYPE_BEART_HEAT);
    }
}
