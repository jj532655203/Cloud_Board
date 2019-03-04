package com.jay.cloud_board.tcp;

import com.jay.cloud_board.base.Global;
import com.jay.cloud_board.util.LogUtil;

import java.io.BufferedInputStream;
import java.net.Socket;

/**
 * Created by jj on 2019/3/3.
 */

public class Reader {

    private static final String TAG = Reader.class.getSimpleName();
    private static Runnable sReadRunnable;
    private static boolean is2Stop;

    public static void stop() {
        LogUtil.d(TAG,"stop");

        if (sReadRunnable == null)
            return;

        is2Stop = true;
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        is2Stop = false;

        sReadRunnable = null;
    }

    public static void restartRead() {
        LogUtil.d(TAG,"restartRead");
        stop();
        startRead();
    }

    /**
     * 开启工作线程死循环:从服务端读协议
     * 推荐在service组件调用
     */
    public static void startRead() {
        LogUtil.d(TAG,"startRead");
        if (sReadRunnable != null)
            return;

        sReadRunnable = new Runnable() {
            @Override
            public void run() {

                //死循环:读服务端数据
                ReaderHandler protocolHandler = new ReaderHandler();
                while (!is2Stop) {

                    Socket socket = Global.getSocket();
                    if (socket == null)
                        continue;

                    //读取数据
                    byte[] data = new byte[20480];
                    int size;
                    try {
                        LogUtil.d(TAG, "新一轮循环读取");
                        BufferedInputStream bis = new BufferedInputStream(socket.getInputStream());
                        while ((size = bis.read(data)) > 0) {

                            String json = new String(data, 0, size).trim();


                            //交给handler类处理各种协议
                            protocolHandler.handleProtocol(json);
                            System.out.println(TAG + "从服务端接收到的json----" + json);
                        }

                        //收到客户端发送的请求后，立马回一条心跳给客户端
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        JobExecutor.getInstance().execute(sReadRunnable);
    }
}
