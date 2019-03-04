package com.jay.cloud_board.tcp;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.jay.cloud_board.meeting_protocal.HeartBeatProtocol;


/**
 * @Description 心跳包机制
 * Created by jj on 2019/3/4.
 */

public class HeartBeat {

    private static final long HEART_BEAT_TIME = 5000;
    private static final int MSG_BEAT = 0;
    private static boolean isBeatting;
    private static Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG_BEAT) {
                startBeat();
            }
        }
    };

    /**
     * 开启心跳任务
     */
    public static void startBeat() {

        if (isBeatting)
            return;

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Writer.send(new HeartBeatProtocol());
                mHandler.obtainMessage(MSG_BEAT).sendToTarget();
            }
        }, HEART_BEAT_TIME);

        isBeatting = true;
    }

    public static void stop() {
        mHandler.removeCallbacksAndMessages(null);
        isBeatting = false;
    }
}
