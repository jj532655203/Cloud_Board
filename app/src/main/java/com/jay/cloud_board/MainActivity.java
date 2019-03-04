package com.jay.cloud_board;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
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

import com.jay.cloud_board.base.Global;
import com.jay.cloud_board.meeting_protocal.LoginProtocol;
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

    private ServiceConnection mTcpConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LogUtil.d(TAG, "onServiceConnected");

            TcpService.ClientBinder clientBinder = (TcpService.ClientBinder) service;
            clientBinder.startConnect();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            LogUtil.d(TAG, "onServiceDisconnected");
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mTcpConn);
    }

    @Override
    protected void onStart() {
        super.onStart();

        mBoardView.mBoardWriting = mBoardWriting;
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

        Process.killProcess(Process.myPid());

    }
}
