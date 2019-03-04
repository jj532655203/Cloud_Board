package com.jay.cloud_board.tcp;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.jay.cloud_board.meeting_protocal.HeartBeatProtocol;
import com.jay.cloud_board.util.LogUtil;


/**
 * @Description 心跳包机制
 * Created by jj on 2019/3/4.
 */

public class HeartBeat {

    private static final long HEART_BEAT_TIME = 5000;
    private static final int MSG_BEAT = 0;
    private static final String TAG = HeartBeat.class.getSimpleName();
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
        LogUtil.d(TAG, "startBeat");

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Writer.send(new HeartBeatProtocol());
                mHandler.obtainMessage(MSG_BEAT).sendToTarget();
            }
        }, HEART_BEAT_TIME);

    }

    public static void stop() {
        LogUtil.d(TAG, "stop");
        mHandler.removeCallbacksAndMessages(null);
    }
}
