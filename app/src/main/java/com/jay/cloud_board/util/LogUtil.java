package com.jay.cloud_board.util;

import android.text.TextUtils;
import android.util.Log;

import com.jay.cloud_board.base.Config;


/**
 * Created by Jay on 2019/3/2.
 */

public class LogUtil {

    public static void d(String tag, String content) {
        if (!Config.DEBUG || TextUtils.isEmpty(content))
            return;
        Log.d("jay---log---" + tag, content);
    }

    public static void i(String tag, String content) {
        if (!Config.DEBUG || TextUtils.isEmpty(content))
            return;
        Log.i("jay---log---" + tag, content);
    }

    public static void w(String tag, String content) {
        if (!Config.DEBUG || TextUtils.isEmpty(content))
            return;
        Log.w("jay---log---" + tag, content);
    }

    public static void e(String tag, String content) {
        if (!Config.DEBUG || TextUtils.isEmpty(content))
            return;
        Log.e("jay---log---" + tag, content);
    }
}
