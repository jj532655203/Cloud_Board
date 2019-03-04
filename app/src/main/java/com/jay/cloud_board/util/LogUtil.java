package com.jay.cloud_board.util;

import android.text.TextUtils;
import android.util.Log;

import com.jay.cloud_board.BuildConfig;

/**
 * Created by Jay on 2019/3/2.
 */

public class LogUtil {

    public static void d(String tag, String content) {
        if (!BuildConfig.DEBUG || TextUtils.isEmpty(content))
            return;
        Log.d(tag, content);
    }

    public static void i(String tag, String content) {
        if (!BuildConfig.DEBUG || TextUtils.isEmpty(content))
            return;
        Log.i(tag, content);
    }

    public static void w(String tag, String content) {
        if (!BuildConfig.DEBUG || TextUtils.isEmpty(content))
            return;
        Log.w(tag, content);
    }

    public static void e(String tag, String content) {
        if (!BuildConfig.DEBUG || TextUtils.isEmpty(content))
            return;
        Log.e(tag, content);
    }
}
