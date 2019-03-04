package com.jay.cloud_board.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.jay.cloud_board.Global;
import com.jay.cloud_board.JobExecutor;
import com.jay.cloud_board.Reader;
import com.jay.cloud_board.Writer;
import com.jay.cloud_board.meeting_protocal.LoginProtocol;
import com.jay.cloud_board.util.LogUtil;

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
        private Socket mmSocket;

        /**
         * 连接服务端
         */
        public void startConnect() {
            LogUtil.d(TAG, "startConnect");
            JobExecutor.getInstance().execute(new Runnable() {
                @Override
                public void run() {
                    try {

                        // 建立Socket连接
                        mmSocket = new Socket();
                        mmSocket.connect(new InetSocketAddress("39.98.191.61", 3389), 5000);

                        Writer.setSocket(mmSocket);

                        //开启线程:发协议
                        LoginProtocol loginProtocol = new LoginProtocol();
                        loginProtocol.setUserId(Global.getUserRole());
                        Writer.send(loginProtocol);

                        //开启线程:读服务端协议
                        Reader.read(mmSocket);

                    } catch (Exception e) {
                        e.printStackTrace();
                        LogUtil.e(TAG, "mmConnectRun run() 连接服务端失败");
                    }
                }
            });
        }


        /**
         * 与服务端断开连接
         */
        private void disConnect() {
            JobExecutor.getInstance().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        LogUtil.d(TAG, "正在执行断连: disConnect");

                        if (mmSocket != null) {
                            mmSocket.shutdownInput();
                            mmSocket.shutdownOutput();
                            mmSocket.close();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}
