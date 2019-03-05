package com.jay.cloud_board.meeting_protocal;

import com.jay.cloud_board.bean.Point;

import java.util.ArrayList;

/**
 * desc:添加笔划协议
 * Created by Jay on 2019/3/3.
 */

public class AddStrokeProtocol {


    private String userId;
    private int protocolType;
    private String receiverUserId;
    private String userRole;
    private ArrayList<Point> points = new ArrayList<>();


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getProtocolType() {
        return protocolType;
    }

    public void setProtocolType(int protocolType) {
        this.protocolType = protocolType;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    public String getReceiverUserId() {
        return receiverUserId;
    }

    public void setReceiverUserId(String receiverUserId) {
        this.receiverUserId = receiverUserId;
    }


    public AddStrokeProtocol(String userId, int protocolType) {
        this.userId = userId;
        this.protocolType = protocolType;
    }

    public ArrayList<Point> getPoints() {
        return points;
    }

    public void setPoints(ArrayList<Point> points) {
        this.points = points;
    }

    @Override
    public String toString() {
        return "AddStrokeProtocol{" +
                "userId='" + userId + '\'' +
                ", protocolType=" + protocolType +
                ", receiverUserId='" + receiverUserId + '\'' +
                ", userRole='" + userRole + '\'' +
                ", points=" + points +
                '}';
    }
}
