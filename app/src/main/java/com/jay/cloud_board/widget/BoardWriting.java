package com.jay.cloud_board.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.jay.cloud_board.bean.Point;
import com.jay.cloud_board.bean.Stroke;
import com.jay.cloud_board.util.LogUtil;

import java.util.ArrayList;

/**
 * desc:手势滑动时,单个笔划轨迹的实时绘制类
 * Created by Jay on 2019/3/2.
 */

public class BoardWriting extends View {

    private static final String TAG = BoardWriting.class.getSimpleName();
    private final Paint mPaint = new Paint();
    private boolean isClearPath;
    private Stroke stroke;

    public BoardWriting(Context context) {
        super(context, null);
    }

    public BoardWriting(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setAntiAlias(true);
    }

    public void updateTouchPoints(ArrayList<Point> touchPoints) {

        stroke = new Stroke();
        stroke.setPoints(touchPoints);
        stroke.generatePath();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        LogUtil.d(TAG, "onDraw");

        if (isClearPath || stroke == null || !stroke.hasmPath()) {
            LogUtil.d(TAG, "onDraw 清空屏幕");
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.OVERLAY);
            return;
        }

        canvas.drawPath(stroke.getPath(), mPaint);
    }

    public void clearPath() {
        LogUtil.d(TAG,"clearPath");
        isClearPath = true;
        stroke = null;
        invalidate();
        isClearPath = false;
    }
}
