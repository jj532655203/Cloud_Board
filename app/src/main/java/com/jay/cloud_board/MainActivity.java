package com.jay.cloud_board;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Process;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.jay.cloud_board.base.Global;
import com.jay.cloud_board.eventbus.FailedConn2ServerEvent;
import com.jay.cloud_board.eventbus.NetWorkStateChangedEvent;
import com.jay.cloud_board.meeting_protocal.LoginProtocol;
import com.jay.cloud_board.receiver.NetWorkStateReceiver;
import com.jay.cloud_board.service.TcpService;
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

import java.lang.ref.WeakReference;

import io.reactivex.functions.Consumer;

@EActivity(R.layout.activity_main)
@WindowFeature(Window.FEATURE_NO_TITLE)
public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int MSG_RE_CONN_2_SERVER = 0;
    @ViewById(R.id.boardView)
    public BoardView mBoardView;
    @ViewById(R.id.boardWriting)
    public BoardWriting mBoardWriting;
    @ViewById(R.id.switchRole)
    public TextView mSwitchRole;

    private Handler mHandler = new UiHandler(new WeakReference<>(this));

    private TcpService.ClientBinder mTcpService;
    private ServiceConnection mTcpConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LogUtil.d(TAG, "onServiceConnected");

            mTcpService = (TcpService.ClientBinder) service;
            mTcpService.startConnect();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            LogUtil.d(TAG, "onServiceDisconnected");

            mTcpService.disConnect();
        }
    };
    private NetWorkStateReceiver mNetReceiver;

    @Override
    protected void onStart() {
        super.onStart();

        mBoardView.mBoardWriting = mBoardWriting;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions.requestEach(Manifest.permission.INTERNET)
                .subscribe(new Consumer<Permission>() {
                    @Override
                    public void accept(Permission permission) throws Exception {
                        if (permission.granted) {

                            Intent intent = new Intent(MainActivity.this, TcpService.class);
                            bindService(intent, mTcpConn, BIND_AUTO_CREATE);
                            return;
                        }
                        if (permission.shouldShowRequestPermissionRationale) {
                            Toast.makeText(MainActivity.this, "网络权限被拒绝,笔划协同功能不可用!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        rxPermissions.requestEach(Manifest.permission.INTERNET)
                .subscribe(new Consumer<Permission>() {
                    @Override
                    public void accept(Permission permission) throws Exception {
                    }
                });
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
        mHandler.removeCallbacksAndMessages(null);
    }

    /**
     * 切换用户角色:A-->B或B-->A
     */
    @Click(R.id.switchRole)
    public void switchRole(View view) {

        Global.switchRole();

        //切换账号
        LoginProtocol switchRoleProtocol = new LoginProtocol();
        switchRoleProtocol.setUserId(Global.getUserRole());
        Writer.send(switchRoleProtocol);

        mSwitchRole.setText("切换账号:" + Global.getUserRole());

        LogUtil.d(TAG, "switchRole role=" + Global.getUserRole());
    }

    @Click(R.id.exit)
    public void exit(View view) {
        LogUtil.d(TAG, "exit");
        //        finish();
        Process.killProcess(Process.myPid());
    }

    private void reconn2Server() {
        LogUtil.d(TAG,"reconn2Server");
        Toast.makeText(MainActivity.this, "正在尝试重连服务器!", Toast.LENGTH_SHORT).show();
        mTcpService.disConnect();
        mTcpService.startConnect();
    }

    /**
     * 处理:网络状态变更
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleNetworkStateChangedException(NetWorkStateChangedEvent event) {
        LogUtil.d(TAG,"handleNetworkStateChangedException");
        if (Global.getNetWorkState() == NetWorkStateChangedEvent.NetStateType.TYPE_NONE_CONNECTED)
            Toast.makeText(MainActivity.this, "请检查网络!", Toast.LENGTH_SHORT).show();
        else{
            reconn2Server();
        }
    }

    /**
     * 处理:无法连接服务器
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleFailedConn2ServerEvent(FailedConn2ServerEvent event) {
        LogUtil.d(TAG,"handleFailedConn2ServerEvent");
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mHandler.obtainMessage(MSG_RE_CONN_2_SERVER).sendToTarget();
            }
        }, 5000);
    }

    private static class UiHandler extends Handler {
        private WeakReference<MainActivity> mActivityRef;

        public UiHandler(WeakReference<MainActivity> reference) {
            mActivityRef = reference;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG_RE_CONN_2_SERVER) {
                mActivityRef.get().reconn2Server();
            }
        }
    }
}
