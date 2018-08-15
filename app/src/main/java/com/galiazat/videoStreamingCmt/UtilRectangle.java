package com.galiazat.videoStreamingCmt;

import org.opencv.core.Point;
import org.opencv.core.Rect;

/**
 * @author Azat Galiullin.
 */

public class UtilRectangle {

    private Point left;
    private Point right;

    public void addPoint(Point point){
        if (left == null){
            left = new Point(point.x, point.y);
            right = new Point(point.x, point.y);
            return;
        }
        if (point.x < left.x){
            left.x = point.x;
        }
        if (point.y < left.y){
            left.y = point.y;
        }
        if (point.x > right.x){
            right.x = point.x;
        }
        if (point.y > right.y){
            right.y = point.y;
        }
    }

    public int getLeft(){
        return (int) left.x;
    }

    public int getRight(){
        return (int) right.x;
    }

    public int getTop(){
        return (int) left.y;
    }

    public int getBottom(){
        return (int) right.y;
    }

    public boolean isAreaNotEqualsZero(){
        return (getLeft() - getRight()) != 0 && (getTop() - getBottom()) != 0;
    }

    public Rect toOpenCvRect(){
        return new Rect(left, right);
    }

}
