package com.jay.cloud_board.tcp;

import com.google.gson.Gson;
import com.jay.cloud_board.base.Global;
import com.jay.cloud_board.eventbus.FailedConn2ServerEvent;
import com.jay.cloud_board.eventbus.NetWorkStateChangedEvent;
import com.jay.cloud_board.meeting_protocal.MeetingProtocol;
import com.jay.cloud_board.util.LogUtil;

import org.greenrobot.eventbus.EventBus;

import java.io.BufferedOutputStream;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by jj on 2019/3/3.
 */

public class Writer {

    private static final String TAG = Writer.class.getSimpleName();
    private static ArrayBlockingQueue<MeetingProtocol> sBlockingQueue = new ArrayBlockingQueue<>(100);
    private static Runnable sWriteRunnable;
    private static boolean is2Stop;

    /**
     * 向服务端发送
     *
     * @param _protocol 对应的协议
     * @Descrepetion please setSocket() before.
     */
    public static void send(MeetingProtocol _protocol) {
        LogUtil.d(TAG, "send协议:" + _protocol);

        sBlockingQueue.add(_protocol);
    }

    public static void stop() {

        if (sWriteRunnable == null)
            return;

        is2Stop = true;
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        is2Stop = false;

        sWriteRunnable = null;
    }

    public static void restartWrite() {
        stop();
        startWrite();
    }

    /**
     * 开启线程死循环:发协议
     * 推荐在service组件调用
     */
    public static void startWrite() {
        if (sWriteRunnable != null)
            return;

        sWriteRunnable = new Runnable() {
            @Override
            public void run() {

                //死循环写协议
                while (!is2Stop) {

                    Socket socket = Global.getSocket();

                    if (socket == null || sBlockingQueue.isEmpty())
                        continue;

                    LogUtil.d(TAG, "死循环内开始写");
                    try {

                        MeetingProtocol protocol = sBlockingQueue.take();
                        BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());
                        bos.write(new Gson().toJson(protocol).getBytes());
                        bos.flush();

                    } catch (Exception e) {
                        e.printStackTrace();
                        if (Global.getNetWorkState() != NetWorkStateChangedEvent.NetStateType.TYPE_NONE_CONNECTED) {

                            //与服务器断连
                            EventBus.getDefault().post(new FailedConn2ServerEvent());
                        }
                    }
                }
            }
        };
        JobExecutor.getInstance().execute(sWriteRunnable);
    }
}
