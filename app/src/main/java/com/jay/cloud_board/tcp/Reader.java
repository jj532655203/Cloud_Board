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
                    if (socket == null || socket.isClosed())
                        continue;

                    BufferedInputStream bis = null;
                    try {

                        socket.setReceiveBufferSize(10240);

                        byte[] bytes = new byte[1024];
                        int size;

                        //保存某协议的数据
                        StringBuilder dataSb = new StringBuilder();

                        //代表某协议的长度,如"$00022"中的22位
                        int[] dataLength = new int[1];

                        bis = new BufferedInputStream(socket.getInputStream());
                        while ((size = bis.read(bytes)) > 0) {

                            //更新最新收到服务器消息的时间
                            HeartBeat.sLastServerBeatTime = System.currentTimeMillis();

                            String piece = new String(bytes, 0, size).trim();
                            LogUtil.d(TAG, "piece包:" + piece);

                            /**
                             * 解包逻辑:
                             * 与Writer中组包规则相对应
                             * 组包规则:给需要发出的协议添加前缀:"$00022",其中"$"是起始标识,后5位为本协议转成byte数组后的长度
                             * 请先阅读:https://www.jianshu.com/p/45957e180925
                             * 并理解 自动拆包/粘包
                             *
                             * 到达unpack()方法,传入的数据包(参数piece), 有以下几种情况:
                             * 1.dataSb长度==0,该数据包以"$*****"的6位为前缀,之后为协议的原始内容;长度为*****(取值范围0-99999),赋值给dataLength
                             *      1.1.piece包余下没有 dataLength 位,即协议数据 被拆包了
                             *      1.2.piece包余下超过 dataLength 位,即协议数据 后面粘包了
                             *      1.3.piece包余下刚好 dataLength 位
                             *
                             * 2.dataSb长度!=0,即旧协议数据没读完,旧协议数据还缺  absentLength = dataLength.length - dataSb.length 位
                             *      1.1.piece包没有 absentLength 位,即协议数据 再次被拆包了
                             *      1.2.piece包超过 absentLength 位,即协议数据 后面粘包了
                             *      1.3.piece包刚好 absentLength 位
                             *
                             */

                            boolean success = unpack(dataSb, dataLength, piece);
                            if (!success) {
                                LogUtil.e(TAG, "unpack failed");
                                dataSb.delete(0, dataSb.length());
                                dataLength[0] = 0;
                            }
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
             */
            private boolean unpack(StringBuilder dataSb, int[] dataLength, String piece) {

                /*1.dataSb长度==0,该数据包以"$*****"的6位为前缀,之后为协议的原始内容;长度为*****(取值范围0-99999),赋值给dataLength*/
                if (dataSb.length() == 0) {

                    //协议前缀就有6位
                    if (piece.length() < 6)
                        return false;

					/*//不可能的情况:"$*****"6位字符被分拆,或被接在piece包旧协议数据的最末尾*/

                    //异常情况
                    if (piece.indexOf("$") > piece.length() - 6)
                        return false;

                    //取出协议的长度值(5位数)
                    try {
                        dataLength[0] = Integer.parseInt(piece.substring(piece.indexOf("$") + 1, piece.indexOf("$") + 6));
                        LogUtil.e(TAG, "新协议长度=" + dataLength[0]);
                    } catch (Exception e) {
                        LogUtil.e(TAG, "协议书写不规范");
                        return false;
                    }

                    /*1.1.piece包余下没有 dataLength 位,即协议数据 被拆包了*/
                    //取前缀"$10002"之后的真实数据
                    piece = piece.substring(piece.indexOf("$") + 6, piece.length());
                    if (piece.length() < dataLength[0]) {

                        dataSb.append(piece);
                        LogUtil.d(TAG, "一条piece解包后 dataSb=" + dataSb);
                        return true;

                    /*1.2.piece包余下超过 dataLength 位,即协议数据 后面粘包了*/
                    } else if (piece.length() > dataLength[0]) {

                        //读取旧协议数据,处理掉旧协议
                        int absentLength = dataLength[0] - dataSb.length();
                        return interceptAndRecursiveRemaining(dataSb, dataLength, piece, absentLength);

                    /*1.3.piece包余下刚好 dataLength 位*/
                    } else {
                        appendPiece(dataSb, dataLength, piece);
                        LogUtil.d(TAG, "一条piece解包后 dataSb=" + dataSb);
                        return true;
                    }

                /*2.dataSb长度!=0,即旧协议数据没读完,旧协议数据还缺  absentLength = dataLength.length - dataSb.length 位*/
                } else {
                    int absentLength = dataLength[0] - dataSb.length();

                    /*2.1.piece包没有 absentLength 位,即协议数据 再次被拆包了*/
                    if (piece.length() < absentLength) {
                        dataSb.append(piece);
                        LogUtil.d(TAG, "一条piece解包后 dataSb=" + dataSb);
                        return true;

                    /*2.2.piece包超过 absentLength 位,即协议数据 后面粘包了*/
                    } else if (piece.length() > absentLength) {

                        //读取旧协议数据,处理掉旧协议
                        return interceptAndRecursiveRemaining(dataSb, dataLength, piece, absentLength);
                    /*2.3.piece包刚好 absentLength 位*/
                    } else {

                        appendPiece(dataSb, dataLength, piece);
                        LogUtil.d(TAG, "一条piece解包后 dataSb=" + dataSb);
                        return true;
                    }

                }
            }

            /**
             * piece包前面部分是旧协议数据,后面递归处理
             */
            private boolean interceptAndRecursiveRemaining(StringBuilder dataSb, int[] dataLength, String piece, int absentLength) {
                dataSb.append(piece, 0, absentLength);
                handler.handleProtocol(dataSb.toString());

                //恢复变量值
                dataSb.delete(0, dataSb.length());
                dataLength[0] = 0;

                //递归处理余下的piece包数据
                piece = piece.substring(absentLength, piece.length());
                boolean success = unpack(dataSb, dataLength, piece);
                if (!success) {
                    dataSb.delete(0, dataSb.length());
                    dataLength[0] = 0;
                    return false;
                }
                LogUtil.d(TAG, "一条piece解包后 dataSb=" + dataSb);
                return true;
            }

            /**
             * 整个piece包都属于旧协议数据
             */
            private void appendPiece(StringBuilder dataSb, int[] dataLength, String piece) {
                dataSb.append(piece);
                handler.handleProtocol(dataSb.toString());

                //恢复变量值
                dataSb.delete(0, dataSb.length());
                dataLength[0] = 0;
            }

        };
        JobExecutor.getInstance().execute(sReadRunnable);
    }
}
