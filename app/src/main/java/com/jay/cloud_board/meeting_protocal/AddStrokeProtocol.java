package com.jay.cloud_board.meeting_protocal;

import com.jay.cloud_board.bean.Point;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * desc:添加笔划协议
 * Created by Jay on 2019/3/3.
 */

public class AddStrokeProtocol extends MeetingProtocol implements Serializable {

    private String receiverUserId;
    private ArrayList<Point> mPoints = new ArrayList<>();


    public ArrayList<Point> getPoints() {
        return mPoints;
    }

    public void setPoints(ArrayList<Point> points) {
        for (Point point : points) {
            mPoints.add(point.clone());
        }
    }

    public String getReceiverUserId() {
        return receiverUserId;
    }

    public void setReceiverUserId(String receiverUserId) {
        this.receiverUserId = receiverUserId;
    }

    @Override
    public String toString() {
        return super.toString() +
                "AddStrokeProtocol{" +
                "receiverUserId='" + receiverUserId + '\'' +
                ", mPoints=" + mPoints +
                '}';
    }
}
