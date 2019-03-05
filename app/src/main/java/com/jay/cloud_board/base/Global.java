package com.jay.cloud_board.base;

import android.text.TextUtils;

import com.jay.cloud_board.eventbus.NetWorkStateChangedEvent;

import java.net.Socket;

/**
 * Created by Jay on 2019/3/3.
 */

public class Global {

    private static String sUserRole = "A";
    public static final String ROLE_USER_A = "A";
    public static final String ROLE_USER_B = "B";
    private static Socket sSocket;
    private static NetWorkStateChangedEvent.NetStateType sNetWorkState = NetWorkStateChangedEvent.NetStateType.TYPE_MOBILE_DATA_CONNECTED;


    public static void switchRole() {
        if (TextUtils.equals(sUserRole, ROLE_USER_A)) {
            sUserRole = ROLE_USER_B;
        } else {
            sUserRole = ROLE_USER_A;
        }
    }

    public static String getUserRole() {
        return sUserRole;
    }

    public static Socket getSocket() {
        return sSocket;
    }

    public static void setSocket(Socket socket) {
        sSocket = socket;
    }

    public static void setNetWorkState(NetWorkStateChangedEvent.NetStateType netWorkState) {
        sNetWorkState = netWorkState;
    }

    public static NetWorkStateChangedEvent.NetStateType getNetWorkState() {
        return sNetWorkState;
    }
}
