package com.jay.cloud_board.tcp;

import com.jay.cloud_board.base.Global;
import com.jay.cloud_board.util.LogUtil;

import java.io.BufferedInputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

/**
 * Created by jj on 2019/3/3.
 */

public class Reader {

    private static final String TAG = Reader.class.getSimpleName();
    private static Runnable sReadRunnable;
    private static boolean is2Stop;
    private static ByteBuffer mByteBuffer = ByteBuffer.allocate(10240);

    public static void stop() {
        LogUtil.d(TAG, "stop");

        if (sReadRunnable == null)
            return;

        is2Stop = true;
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        is2Stop = false;

        sReadRunnable = null;
    }

    public static void restartRead() {
        LogUtil.d(TAG, "restartRead");
        stop();
        startRead();
    }

    /**
     * 开启工作线程死循环:从服务端读协议
     * 推荐在service组件调用
     */
    public static void startRead() {
        LogUtil.d(TAG, "startRead");
        if (sReadRunnable != null)
            return;

        final ReaderHandler handler = new ReaderHandler();
        sReadRunnable = new Runnable() {
            @Override
            public void run() {

                //死循环:读服务端数据
                while (!is2Stop) {

                    Socket socket = Global.getSocket();
                    if (socket == null||socket.isClosed())
                        continue;

                    BufferedInputStream bis = null;
                    try {

//                        socket.setReceiveBufferSize(10240);

                        byte[] bytes = new byte[1024];
                        int size;

                        //保存某协议的数据
                        StringBuilder dataSb = new StringBuilder();

                        //保存某协议的长度
                        int[] dataLength = new int[1];

                        bis = new BufferedInputStream(socket.getInputStream());
                        while ((size = bis.read(bytes)) > 0) {

                            String piece = new String(bytes, 0, size).trim();

                            //解包
                            unpack(dataSb, dataLength, piece);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (bis != null)
                                bis.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            /**
             * 解包
             * @param dataSb 保存某协议的数据
             * @param dataLength 保存某协议的长度
             * @param piece 尚未被处理的数据
             */
            private void unpack(StringBuilder dataSb, int[] dataLength, String piece) {

                if (piece.contains("$")) {

                    //未读取旧协议情况下,开始读一条新协议
                    if (dataSb.length() == 0) {

                        //协议前缀就有6位
                        if (piece.length() < 6)
                            return;

                        //异常情况
                        if (piece.indexOf("$") > piece.length() - 6)
                            return;

                        //取出协议的长度值(5位数)
                        try {
                            dataLength[0] = Integer.parseInt(piece.substring(piece.indexOf("$") + 1, piece.indexOf("$") + 6));
                        } catch (Exception e) {
                            LogUtil.e(TAG, "协议书写不规范");
                        }

                        //取前缀"$10002"之后的真实数据
                        piece = piece.substring(piece.indexOf("$") + 6, piece.length());

                        //有粘包
                        //有第二个"$"标识
                        if (piece.contains("$")) {

                            //截取出第二个标识之前的内容,处理掉旧协议
                            dataSb.append(piece.substring(0, piece.indexOf("$")));
                            handler.handleProtocol(dataSb.toString());

                            dataSb = new StringBuilder();
                            dataLength[0] = 0;

                            //递归处理粘包
                            piece = piece.substring(piece.indexOf("$"), piece.length());
                            unpack(dataSb, dataLength, piece);

                            //没有粘包:根据协议长度--dataLength[0]处理
                        } else {

                            handleByProtocolLength(dataSb, dataLength, piece);

                        }
                    } else {

                        //旧协议结尾,读到新协议,旧协议拿去处理
                        dataSb.append(piece.substring(0, piece.indexOf("$")));
                        handler.handleProtocol(new String(dataSb));

                        dataSb = new StringBuilder();
                        piece = piece.substring(piece.indexOf("$"), piece.length());
                        unpack(dataSb, dataLength, piece);
                    }


                    //拆包 不粘包
                } else {
                    handleByProtocolLength(dataSb, dataLength, piece);

                }
            }

            /**
             * 根据协议长度--dataLength[0]处理
             * @param dataSb
             * @param dataLength
             * @param piece
             */
            private void handleByProtocolLength(StringBuilder dataSb, int[] dataLength, String piece) {

                //未记录协议的长度
                if (dataLength[0] == 0)
                    return;

                dataSb.append(piece);

                //旧协议数据到本包为止
                if (dataSb.length() > dataLength[0]) {
                    dataSb.delete(dataLength[0], dataSb.length());
                }
                if (dataSb.length() == dataLength[0]) {
                    handler.handleProtocol(dataSb.toString());

                    dataSb.delete(0, dataSb.length());
                    dataLength[0] = 0;
                }
            }
        };
        JobExecutor.getInstance().execute(sReadRunnable);
    }
}
