package com.jay.cloud_board.meeting_protocal;

import com.jay.cloud_board.base.Constant;
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
    private String userRole;

    public AddStrokeProtocol() {
        setProtocolType(Constant.PROTOCOL_TYPE_ADD_STROKE);
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

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
                "userRole='" + userRole + '\'' +
                ", mPoints=" + mPoints +
                '}';
    }
}
