package com.jay.cloud_board.tcp;

import com.google.gson.Gson;
import com.jay.cloud_board.base.Global;
import com.jay.cloud_board.meeting_protocal.ProtocolShell;
import com.jay.cloud_board.util.LogUtil;

import java.io.BufferedOutputStream;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by jj on 2019/3/3.
 */

public class Writer {

    private static final String TAG = Writer.class.getSimpleName();
    private static ArrayBlockingQueue<Object> sBlockingQueue = new ArrayBlockingQueue<>(1000);
    private static Runnable sWriteRunnable;
    private static boolean is2Stop;

    /**
     * 向服务端发送
     *
     * @param _protocol 对应的协议
     * @Descrepetion please setSocket() before.
     */
    public static void send(ProtocolShell _protocol) {
        LogUtil.d(TAG, "send协议:" + _protocol.getBody().toString());

        sBlockingQueue.add(_protocol.getBody());
    }

    public static void stop() {
        LogUtil.d(TAG, "stop");

        if (sWriteRunnable == null)
            return;

        is2Stop = true;
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        is2Stop = false;

        sWriteRunnable = null;
    }

    public static void restartWrite() {
        LogUtil.d(TAG, "restartWrite");
        stop();
        startWrite();
    }

    /**
     * 开启线程死循环:发协议
     * 推荐在service组件调用
     */
    public static void startWrite() {
        LogUtil.d(TAG, "startWrite");
        if (sWriteRunnable != null)
            return;

        sWriteRunnable = new Runnable() {
            @Override
            public void run() {

                //死循环:写协议
                while (!is2Stop) {


                    if (sBlockingQueue.isEmpty())
                        continue;

                    Socket socket = Global.getSocket();
                    if (socket == null || socket.isClosed())
                        continue;

                    BufferedOutputStream bos = null;
                    try {

                        //                        socket.setSendBufferSize(10240);

                        //完整协议(数据)
                        String msg = new Gson().toJson(sBlockingQueue.take());


                        /**
                         * 以下为组包逻规则
                         * 添加组包逻辑的原因见:https://www.jianshu.com/p/45957e180925
                         * 1.给需要发出的协议添加前缀:"$00022",其中"$"是起始标识,后5位为本协议转成byte数组后的长度
                         * 2.相应的解包逻辑见:Reader类
                         */


                        //完整协议数据长度
                        int length = msg.getBytes().length;
                        if (length > 99999) {
                            LogUtil.e(TAG, "协议太长,超出前后台组包规范!");
                            continue;
                        }

                        //配上前缀信息,如"$1002"表示完整协议的byte数组长度是1002(暂时支持byte数组长度在4位数以内)
                        StringBuilder lengthStr = new StringBuilder(String.valueOf(length));
                        while (lengthStr.length() < 5) {
                            lengthStr.insert(0, "0");
                        }
                        msg = "$" + lengthStr + msg;

                        LogUtil.d(TAG, "要发的协议:" + msg);

                        //写
                        bos = new BufferedOutputStream(socket.getOutputStream());
                        bos.write(msg.getBytes());
                        bos.flush();

                        LogUtil.d(TAG, "写出去一个完整的协议:" + msg);

                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (bos == null)
                                bos.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };
        JobExecutor.getInstance().execute(sWriteRunnable);

        //todo-->改用protobuf传输数据
        //        AddressBookProtos.Person.Builder builder = AddressBookProtos.Person.newBuilder();
    }
}
