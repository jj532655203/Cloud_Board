package com.jay.cloud_board;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Process;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.jay.cloud_board.base.Constant;
import com.jay.cloud_board.base.Global;
import com.jay.cloud_board.eventbus.NetWorkStateChangedEvent;
import com.jay.cloud_board.eventbus.Reconnect2ServerEvent;
import com.jay.cloud_board.eventbus.ServerDeadEvent;
import com.jay.cloud_board.meeting_protocal.LoginProtocol;
import com.jay.cloud_board.receiver.NetWorkStateReceiver;
import com.jay.cloud_board.service.TcpService;
import com.jay.cloud_board.tcp.HeartBeat;
import com.jay.cloud_board.tcp.Writer;
import com.jay.cloud_board.util.LogUtil;
import com.jay.cloud_board.widget.BoardView;
import com.jay.cloud_board.widget.BoardWriting;
import com.tbruyelle.rxpermissions2.Permission;
import com.tbruyelle.rxpermissions2.RxPermissions;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.WindowFeature;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import io.reactivex.functions.Consumer;

@EActivity(R.layout.activity_main)
@WindowFeature(Window.FEATURE_NO_TITLE)
public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    @ViewById(R.id.boardView)
    public BoardView mBoardView;
    @ViewById(R.id.boardWriting)
    public BoardWriting mBoardWriting;
    @ViewById(R.id.switchRole)
    public TextView mSwitchRole;

    private TcpService.ClientBinder mTcpService;
    private ServiceConnection mTcpConn = new ServiceConnection() {

        /**
         * 主逻辑步骤:3
         */
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LogUtil.d(TAG, "onServiceConnected");

            //服务绑定成功:服务中开启tcp连接
            mTcpService = (TcpService.ClientBinder) service;
            mTcpService.startConnect();

            //开启心跳机制
            HeartBeat.startBeat();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            LogUtil.d(TAG, "onServiceDisconnected");

            //服务解绑了:断开tcp连接
            mTcpService.disConnect();
        }
    };
    private NetWorkStateReceiver mNetReceiver;

    @Override
    protected void onStart() {
        super.onStart();

        mBoardView.mBoardWriting = mBoardWriting;
    }

    /**
     * 主逻辑步骤:1
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //本项目通信不方便处大多使用Eventbus,此处注册,相应的在onDestroy中注销
        EventBus.getDefault().register(this);

        //申请INTERNET权限(socket连接需要)
        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions.requestEach(Manifest.permission.INTERNET)
                .subscribe(new Consumer<Permission>() {
                    @Override
                    public void accept(Permission permission) throws Exception {
                        if (permission.granted) {

                            /**
                             * 主逻辑步骤:2
                             */
                            //一旦获得网络权限:即可绑定TcpService
                            // (该服务复杂与服务器的tcp连接,事实证明:服务中开工作线程保持tcp连接比在Mainactivity组件中开工作线程体验更流畅)
                            //绑定成功后会走 mTcpConn的onServiceConnected方法
                            Intent intent = new Intent(MainActivity.this, TcpService.class);
                            bindService(intent, mTcpConn, BIND_AUTO_CREATE);
                            return;
                        }
                        if (permission.shouldShowRequestPermissionRationale) {
                            Toast.makeText(MainActivity.this, "网络权限被拒绝,笔划协同功能不可用!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        //注册:监听网络状态变更广播;相应的在onDestroy中注销
        mNetReceiver = new NetWorkStateReceiver();
        IntentFilter filter = new IntentFilter(NetWorkStateReceiver.CONNECTIVITY_CHANGE);
        registerReceiver(mNetReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mTcpConn);
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
        if (mNetReceiver != null)
            unregisterReceiver(mNetReceiver);
    }

    /**
     * 布局中switchRole按钮的点击事件
     * 切换用户角色:A-->B或B-->A
     */
    @Click(R.id.switchRole)
    public void switchRole(View view) {

        Global.switchRole();

        //发送:切换账号协议
        LoginProtocol switchRoleProtocol = new LoginProtocol(Global.getUserRole(), Constant.PROTOCOL_TYPE_LOGIN);
        Writer.send(switchRoleProtocol);

        mSwitchRole.setText("切换账号:" + Global.getUserRole());

        LogUtil.d(TAG, "switchRole role=" + Global.getUserRole());
    }

    /**
     * 布局中exit按钮的点击事件
     */
    @Click(R.id.exit)
    public void exit(View view) {
        LogUtil.d(TAG, "exit");
        //        finish();
        Process.killProcess(Process.myPid());
    }

    /**
     * 重新连接服务器
     */
    private void reconn2Server() {
        LogUtil.d(TAG, "reconn2Server");
        Toast.makeText(MainActivity.this, "正在尝试重连服务器!", Toast.LENGTH_SHORT).show();
        mTcpService.disConnect();
        mTcpService.startConnect();

        //开启心跳机制
        HeartBeat.startBeat();
    }

    /**
     * 此处会接收NetWorkStateReceiver类中发出的NetWorkStateChangedEvent(网络状态变更事件)
     * 处理:网络状态变更
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleNetworkStateChangedException(NetWorkStateChangedEvent event) {
        LogUtil.d(TAG, "handleNetworkStateChangedException");
        if (Global.getNetWorkState() == NetWorkStateChangedEvent.NetStateType.TYPE_NONE_CONNECTED)
            Toast.makeText(MainActivity.this, "请检查网络!", Toast.LENGTH_SHORT).show();
    }

    /**
     * 心跳机制发现:与服务器无法重新连接
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleServerDeadEvent(ServerDeadEvent event) {
        LogUtil.d(TAG, "handleServerDeadEvent");
        Toast.makeText(MainActivity.this, "与服务器失去连接!", Toast.LENGTH_SHORT).show();
    }

    /**
     * 心跳机制发现与服务器失去连接,尝试重新连接
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleReconnect2ServerEvent(Reconnect2ServerEvent event) {
        LogUtil.d(TAG, "handleReconnect2ServerEvent");
        reconn2Server();
    }

}
