package com.jay.cloud_board.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * created by Jay
 * desc:触摸点绘制类
 */
public class TouchPointDrawer {

    private static Paint sPaint1;
    private final static float RADIO1 = 15;
    private final static float RADIO2 = 25;


    static {
        sPaint1 = new Paint();
        sPaint1.setColor(Color.parseColor("#22ffffff"));
        sPaint1.setStyle(Paint.Style.FILL);
        sPaint1.setAntiAlias(true);
        sPaint1.setShadowLayer(RADIO2, 0, 0, Color.parseColor("#ffffffff"));
    }


    private TouchPointDrawer() {
    }


    public static void drawTouchPoint(Context context, Canvas canvas, float cx, float cy) {
        canvas.drawCircle(cx,cy,RADIO1,sPaint1);
    }

}
