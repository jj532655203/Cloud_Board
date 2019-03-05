package com.jay.cloud_board.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.util.Log;

import com.jay.cloud_board.base.Global;
import com.jay.cloud_board.eventbus.NetWorkStateChangedEvent;

import org.greenrobot.eventbus.EventBus;

/**
 * @Description 监听网络状态变更
 * Created by jj on 2019/3/4.
 */

public class NetWorkStateReceiver extends BroadcastReceiver {

    public static final String CONNECTIVITY_CHANGE = "android.net.conn.CONNECTIVITY_CHANGE";
    public static final String TAG = NetWorkStateReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null)
            return;
        if (!action.equalsIgnoreCase(CONNECTIVITY_CHANGE))
            return;

        Log.d(TAG, "网络接收者action:" + intent.getAction());

        NetWorkStateChangedEvent.NetStateType netStateType = null;
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {

            NetworkInfo wifiNetworkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            NetworkInfo dataNetworkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if (wifiNetworkInfo.isConnected() && dataNetworkInfo.isConnected()) {
                netStateType = NetWorkStateChangedEvent.NetStateType.TYPE_WIFI_CONNECTED;
            } else if (dataNetworkInfo.isConnected()) {
                netStateType = NetWorkStateChangedEvent.NetStateType.TYPE_MOBILE_DATA_CONNECTED;
            } else if (!wifiNetworkInfo.isConnected() && !dataNetworkInfo.isConnected()) {
                netStateType = NetWorkStateChangedEvent.NetStateType.TYPE_NONE_CONNECTED;
            }
            //API大于23时使用下面的方式进行网络监听
        } else {

            Network[] networks = connMgr.getAllNetworks();
            if (networks == null || networks.length == 0) {
                netStateType = NetWorkStateChangedEvent.NetStateType.TYPE_NONE_CONNECTED;
            } else {
                for (Network network : networks) {
                    NetworkInfo networkInfo = connMgr.getNetworkInfo(network);
                    if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                        netStateType = NetWorkStateChangedEvent.NetStateType.TYPE_WIFI_CONNECTED;
                        break;
                    } else if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                        netStateType = NetWorkStateChangedEvent.NetStateType.TYPE_MOBILE_DATA_CONNECTED;
                    }
                }
            }
        }

        //有网-->无网
        //无网-->有网
        if ((Global.getNetWorkState() != NetWorkStateChangedEvent.NetStateType.TYPE_NONE_CONNECTED
                && netStateType == NetWorkStateChangedEvent.NetStateType.TYPE_NONE_CONNECTED)
                ||
                (Global.getNetWorkState() == NetWorkStateChangedEvent.NetStateType.TYPE_NONE_CONNECTED
                        && netStateType != NetWorkStateChangedEvent.NetStateType.TYPE_NONE_CONNECTED)) {

            Log.d(TAG, "网络状态变更:" + netStateType);

            Global.setNetWorkState(netStateType);

            EventBus.getDefault().post(new NetWorkStateChangedEvent(netStateType));
        }
    }
}
