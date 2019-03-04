package com.jay.cloud_board.meeting_protocal;

import com.jay.cloud_board.base.Constant;

/**
 * desc:登录/切换账号 协议
 * Created by jj on 2019/3/3.
 */

public class LoginProtocol extends MeetingProtocol {
    public LoginProtocol() {
        setProtocolType(Constant.PROTOCOL_TYPE_LOGIN);
    }
}
