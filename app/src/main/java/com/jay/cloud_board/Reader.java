package com.jay.cloud_board;

import com.jay.cloud_board.util.LogUtil;

import java.io.BufferedInputStream;
import java.net.Socket;

/**
 * Created by jj on 2019/3/3.
 */

public class Reader {

    private static final String TAG = Reader.class.getSimpleName();
    private static Runnable sReadRunnable;

    public static void read(final Socket socket) {
        if (sReadRunnable != null)
            return;

        sReadRunnable = new Runnable() {
            @Override
            public void run() {

                //死循环:读服务端数据
                ReaderHandler protocolHandler = new ReaderHandler();
                while (true) {

                    //读取数据
                    byte[] data = new byte[20480];
                    int size;
                    try {
                        LogUtil.d(TAG,"新一轮循环读取");
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
