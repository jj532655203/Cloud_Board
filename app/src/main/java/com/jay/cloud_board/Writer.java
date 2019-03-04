package com.jay.cloud_board;

import com.google.gson.Gson;
import com.jay.cloud_board.meeting_protocal.MeetingProtocol;
import com.jay.cloud_board.util.LogUtil;

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
    private static Socket sSocket;

    /**
     * 向服务端发送
     *
     * @Descrepetion please setSocket() before.
     * @param _protocol 对应的协议
     */
    public static void send(MeetingProtocol _protocol) {
        LogUtil.d(TAG,"send协议:" + _protocol);

        sBlockingQueue.add(_protocol);

        if (sWriteRunnable != null)
            return;

        sWriteRunnable = new Runnable() {
            @Override
            public void run() {

                //死循环写协议
                while (true) {

                    if (sBlockingQueue.isEmpty())
                        continue;

                    LogUtil.d(TAG,"死循环内开始写");
                    try {
                        MeetingProtocol protocol = sBlockingQueue.take();
                        BufferedOutputStream bos = new BufferedOutputStream(sSocket.getOutputStream());
                        bos.write(new Gson().toJson(protocol).getBytes());
                        bos.flush();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        JobExecutor.getInstance().execute(sWriteRunnable);
    }

    public static void setSocket(Socket socket) {
        sSocket = socket;
    }
}
