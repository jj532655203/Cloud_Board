package com.jay.cloud_board.tcp;

import com.google.gson.Gson;
import com.jay.cloud_board.base.Constant;
import com.jay.cloud_board.meeting_protocal.AddStrokeProtocol;
import com.jay.cloud_board.meeting_protocal.MeetingProtocol;

import org.greenrobot.eventbus.EventBus;


public class ReaderHandler {


    private static final String TAG = ReaderHandler.class.getSimpleName();

    public void handleProtocol(String jsonStr) {

        MeetingProtocol protocol = new Gson().fromJson(jsonStr, MeetingProtocol.class);
        int protocolType = protocol.getProtocolType();

        System.out.println(TAG + "handleProtocol 协议type=" + protocolType);

        switch (protocolType) {

            //新增笔划协议
            case Constant.PROTOCOL_TYPE_ADD_STROKE:
                AddStrokeProtocol addStrokeProtocol = new Gson().fromJson(jsonStr, AddStrokeProtocol.class);
                EventBus.getDefault().post(addStrokeProtocol);
                break;
        }

    }
}
