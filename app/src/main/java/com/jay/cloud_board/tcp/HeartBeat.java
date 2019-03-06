package com.jay.cloud_board.tcp;

import com.jay.cloud_board.base.Constant;
import com.jay.cloud_board.base.Global;
import com.jay.cloud_board.eventbus.Reconnect2ServerEvent;
import com.jay.cloud_board.eventbus.ServerDeadEvent;
import com.jay.cloud_board.meeting_protocal.HeartBeatProtocol;
import com.jay.cloud_board.util.LogUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.Timer;
import java.util.TimerTask;


/**
 * @Description 心跳包机制
 * Created by jj on 2019/3/4.
 */

public class HeartBeat {

    private static final String TAG = HeartBeat.class.getSimpleName();
    private static final long HEART_BEAT_PERIOD = 15000;
    public static long sLastServerBeatTime;
    private static Timer mTimer;

    /**
     * 开启心跳任务
     */
    public static void startBeat() {
        LogUtil.d(TAG, "startBeat");

        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {

                //发心跳包
                HeartBeatProtocol protocol = new HeartBeatProtocol(Global.getUserRole(), Constant.PROTOCOL_TYPE_BEART_HEAT);
                Writer.send(protocol);

                //判断服务器是否宕机
                judgeAlive();
            }
        }, 0, HEART_BEAT_PERIOD);
    }

    /**
     * 判断与服务器连接状态
     */
    private static void judgeAlive() {
        long waitTime = (System.currentTimeMillis() - sLastServerBeatTime) / 1000;

        //重连服务器
        if (15 < waitTime && waitTime <= 100) {
            EventBus.getDefault().post(new Reconnect2ServerEvent());

            //提示用户 :服务器宕机了
        } else if (waitTime > 100) {
            EventBus.getDefault().post(new ServerDeadEvent());
            stop();
        }
    }

    public static void stop() {
        LogUtil.d(TAG, "stop");
        if (mTimer != null) {
            mTimer.cancel();
        }
    }
}
