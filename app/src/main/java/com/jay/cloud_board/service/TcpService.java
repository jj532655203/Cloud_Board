package com.jay.cloud_board.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.jay.cloud_board.base.Config;
import com.jay.cloud_board.base.Constant;
import com.jay.cloud_board.base.Global;
import com.jay.cloud_board.meeting_protocal.LoginProtocol;
import com.jay.cloud_board.meeting_protocal.ProtocolShell;
import com.jay.cloud_board.tcp.HeartBeat;
import com.jay.cloud_board.tcp.JobExecutor;
import com.jay.cloud_board.tcp.Reader;
import com.jay.cloud_board.tcp.Writer;
import com.jay.cloud_board.util.LogUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * desc:tcp长连接
 * Created by Jay on 2019/3/3.
 */

public class TcpService extends Service {

    public static final String TAG = TcpService.class.getSimpleName();
    private ClientBinder mBinder;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        mBinder = new ClientBinder();
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mBinder.disConnect();
        return super.onUnbind(intent);
    }


    public class ClientBinder extends Binder {

        /**
         * 连接服务端
         */
        public void startConnect() {
            LogUtil.d(TAG, "startConnect");
            JobExecutor.getInstance().execute(new Runnable() {
                @Override
                public void run() {
                    try {

                        /**
                         * 主逻辑步骤:4
                         */
                        // 建立Socket连接
                        Socket _socket = new Socket();
                        _socket.connect(new InetSocketAddress(Config.serverIp, Config.port), 5000);

                        Global.setSocket(_socket);

                        /**
                         * 主逻辑步骤:5
                         */
                        //开启线程:发协议
                        Writer.startWrite();

                        //登录服务器
                        LoginProtocol loginProtocol = new LoginProtocol(Global.getUserRole(), Constant.PROTOCOL_TYPE_LOGIN);
                        Writer.send(new ProtocolShell(loginProtocol));

                        /**
                         * 主逻辑步骤:6
                         */
                        //开启线程:读服务端协议
                        Reader.startRead();

                        //开启心跳机制
                        HeartBeat.startBeat();

                    } catch (IOException e) {
                        e.printStackTrace();
                        LogUtil.e(TAG, "mmConnectRun run() 连接服务器异常");
                    }
                }
            });
        }


        /**
         * 与服务端断开连接
         */
        public void disConnect() {

            Writer.stop();
            Reader.stop();

            JobExecutor.getInstance().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        LogUtil.d(TAG, "正在执行断连: disConnect");

                        Socket _socket = Global.getSocket();
                        Global.setSocket(null);
                        if (_socket != null) {
                            _socket.shutdownInput();
                            _socket.shutdownOutput();
                            _socket.close();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}
