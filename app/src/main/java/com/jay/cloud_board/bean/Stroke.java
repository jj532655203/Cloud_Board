package com.jay.cloud_board.bean;

import android.graphics.Path;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * desc:笔划
 * Created by Jay on 2019/3/2.
 */

public class Stroke {

    private String id;
    private List<Point> points = new ArrayList<>();
    public float width;
    private Path mPath;
    private Float minX = 100000.0f;
    private Float maxX = 0.0f;
    private Float minY = 100000.0f;
    private Float maxY = 0.0f;
    private boolean hasmPath = false;


    public Stroke() {
        mPath = new Path();
        id = UUID.randomUUID().toString();
    }


    public Stroke clone() {
        Stroke stroke = new Stroke();
        stroke.id = this.id;
        for (int i = 0; i < this.points.size(); i++) {
            if (i < this.points.size() - 1) {
                stroke.addPoint(this.points.get(i).clone(), false);
            } else {
                stroke.addPoint(this.points.get(i).clone(), true);
            }
        }
        stroke.mPath = this.mPath;
        return stroke;
    }

    public List<Point> getPoints() {
        return points;
    }

    public void setPoints(List<Point> _points) {
        points.clear();
        for (Point point : _points) {
            points.add(point.clone());
        }
    }

    public void generatePath() {
        if (this.points.size() == 1) {
            Point m = this.points.get(0).clone();
            mPath.reset();
            mPath.moveTo(m.X, m.Y);
            mPath.lineTo(m.X, m.Y);
            return;
        }
        initMaxMinPoint();
        for (int i = 0; i < this.points.size() - 1; i++) {
            Point m = this.points.get(i);
            Point l = this.points.get(i + 1);
            if (i == 0) {
                setMaxMinPoint(m);
                mPath.reset();
                mPath.moveTo(m.X, m.Y);
                if (this.points.size() == 2) {
                    mPath.lineTo(l.X, l.Y);
                }
            } else {
                if (i < this.points.size() - 2) {
                    mPath.quadTo(m.X, m.Y, (m.X + l.X) / 2, (m.Y + l.Y) / 2);
                } else {
                    //mPath.quadTo(m.X, m.Y, (m.X + l.X) / 2, (m.Y + l.Y) / 2);
                    mPath.quadTo(m.X, m.Y, l.X, l.Y);
                }
            }
            setMaxMinPoint(l);
        }
        hasmPath = true;
    }

    public Path getPath() {
        return mPath;
    }

    private float getAbsoluteValue2(Point point1, Point point2) {
        return (float) Math.sqrt(Math.pow(point1.X - point2.X, 2) + Math.pow(point1.Y - point2.Y, 2));
    }

    public void addPoint(Point mPoint, boolean isLast) {
        if (this.points.size() > 0) {
            Point lastPoint = this.points.get(this.points.size() - 1);
            if (getAbsoluteValue2(mPoint, lastPoint) < this.width + 0.2 && !isLast) {
                return;
            } else if (getAbsoluteValue2(mPoint, lastPoint) < this.width + 0.2 && isLast) {
                int index = this.points.size() - 1;
                if (index > -1) {
                    this.points.remove(index);
                }
            }
        }

        this.points.add(mPoint);
        setMaxMinPoint(mPoint);
        if (this.points.size() == 1) {
            mPath.reset();
            this.mPath.moveTo(mPoint.X, mPoint.Y);
            this.mPath.lineTo(mPoint.X, mPoint.Y);
        } else if (!isLast) {
            Point a1 = this.points.get(this.points.size() - 2);
            Point a2 = this.points.get(this.points.size() - 1);
            //this.mPath.quadTo(a1.X, a1.Y, (a1.X + a2.X) / 2, (a1.Y + a2.Y) / 2);
            this.mPath.quadTo(a1.X, a1.Y, (a1.X + a2.X) / 2, (a1.Y + a2.Y) / 2);
        } else {
            Point a1 = this.points.get(this.points.size() - 2);
            Point a2 = this.points.get(this.points.size() - 1);
            //this.mPath.quadTo(a1.X, a1.Y, (a1.X + a2.X) / 2, (a1.Y + a2.Y) / 2);
            this.mPath.quadTo(a1.X, a1.Y, a2.X, a2.Y);
        }

        hasmPath = true;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public Float getWidth() {
        return width;
    }

    public void setWidth(Float width) {
        this.width = width;
    }

    private void initMaxMinPoint() {
        maxX = 0f;
        maxY = 0f;
        minX = 1000000f;
        minY = 1000000f;
    }


    private void setMaxMinPoint(Point p) {

        if (p.X > maxX) {
            maxX = p.X;
        }

        if (p.X < minX) {
            minX = p.X;
        }

        if (p.Y > maxY) {
            maxY = p.Y;
        }

        if (p.Y < minY) {
            minY = p.Y;
        }
    }

    public Float getMinX() {
        return minX;
    }

    public Float getMaxX() {
        return maxX;
    }

    public Float getMinY() {
        return minY;
    }

    public Float getMaxY() {
        return maxY;
    }

    private byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }


    public boolean hasmPath() {
        return hasmPath;
    }

}
