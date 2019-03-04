package com.jay.cloud_board.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.jay.cloud_board.base.Constant;
import com.jay.cloud_board.base.Global;
import com.jay.cloud_board.tcp.Writer;
import com.jay.cloud_board.bean.Point;
import com.jay.cloud_board.bean.Stroke;
import com.jay.cloud_board.meeting_protocal.AddStrokeProtocol;
import com.jay.cloud_board.util.LogUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

/**
 * desc:最终显示所有笔划的类
 * Created by Jay on 2019/3/2.
 */

public class BoardView extends View {

    private static final String TAG = BoardView.class.getSimpleName();
    private ArrayList<Point> mTouchPoints = new ArrayList<>();
    private ArrayList<Stroke> mStrokes = new ArrayList<>();
    private Paint mPaint = new Paint();
    public BoardWriting mBoardWriting;

    public BoardView(Context context) {
        this(context, null);
    }

    public BoardView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setAntiAlias(true);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        LogUtil.d(TAG, "onTouchEvent");

        //角色USER_B不能输入笔划
        if (TextUtils.equals(Global.getUserRole(), Global.ROLE_USER_B))
            return true;

        getTouchPoints(event);

        //一次手势滑动手抬起
        if (event.getActionMasked() == MotionEvent.ACTION_UP) {
            LogUtil.d(TAG, "ontouchEvent action_up");
            addStroke(mTouchPoints);

            //笔划数据发给服务端
            AddStrokeProtocol addStrokeProtocol = new AddStrokeProtocol();
            addStrokeProtocol.setPoints(mTouchPoints);

            //一期需求:暂时使用用户角色作为用户id,因为只有用户A和B
            addStrokeProtocol.setUserId(Global.getUserRole());
            String receiverId = TextUtils.equals(Global.getUserRole(), Global.ROLE_USER_A) ? Global.ROLE_USER_B : Global.ROLE_USER_A;
            addStrokeProtocol.setReceiverUserId(receiverId);
            addStrokeProtocol.setProtocolType(Constant.PROTOCOL_TYPE_ADD_STROKE);

            //向服务端发送
            Writer.send(addStrokeProtocol);
            mTouchPoints.clear();

            //恢复BoardWriting空屏状态
            mBoardWriting.clearPath();

            //添加本次笔划,绘制所有笔划
            invalidate();

        } else {

            //笔划的实时显示:交给BoardWriting
            mBoardWriting.updateTouchPoints(mTouchPoints);
            mBoardWriting.invalidate();
        }

        return true;
    }

    private void addStroke(ArrayList<Point> points) {
        Stroke stroke = new Stroke();
        stroke.setPoints(points);
        stroke.generatePath();
        mStrokes.add(stroke);
    }

    private void getTouchPoints(MotionEvent event) {

        Point point;

        for (int i = 0; i < event.getPointerCount(); i++) {
            point = new Point();
            point.X = event.getX(i);
            point.Y = event.getY(i);
            mTouchPoints.add(point);
        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        LogUtil.d(TAG, "onDraw mStrokes.size=" + mStrokes.size());

        for (Stroke stroke : mStrokes) {
            canvas.drawPath(stroke.getPath(), mPaint);
        }

    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleAddStroke(AddStrokeProtocol protocol) {
        ArrayList<Point> points = protocol.getPoints();
        addStroke(points);
        BoardView.this.invalidate();
    }
}
