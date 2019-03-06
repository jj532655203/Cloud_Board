package com.jay.cloud_board.base;

public interface Constant {

    //心跳协议
    int PROTOCOL_TYPE_BEART_HEAT = 0;

    //登录/切换账号  协议
    int PROTOCOL_TYPE_LOGIN = 1;

    //添加笔划协议
    int PROTOCOL_TYPE_ADD_STROKE = 2;

    /*需要序列化的类的 serialVersionUid 需前后端保持一致*/
    long SERIAL_UID_ADD_STROKE = 3L;
    long SERIAL_UID_HEART_BEAT = 4L;
    long SERIAL_UID_STROKE = 5L;
    long SERIAL_UID_POINT = 6L;
    long SERIAL_UID_PROXY_PROTOCOL = 1L;
}
