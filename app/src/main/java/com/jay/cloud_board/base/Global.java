package com.jay.cloud_board.base;

import android.text.TextUtils;

/**
 * Created by Jay on 2019/3/3.
 */

public class Global {

    private static String sUserRole = "A";
    public static final String ROLE_USER_A = "A";
    public static final String ROLE_USER_B = "B";


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

}
