package com.github.c.horizonalscrollitem;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

/**
 * Created by a on 2016/5/6.
 */
public class MyRecyclerView extends RecyclerView {

    private float downX;
    private float downY;
    private float moveX;
    private float moveY;
    private float lastX;
    private float lastY;

    public MyRecyclerView(Context context) {
        super(context);
        init(context);
    }

    public MyRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {

    }
//    // 滑动距离及坐标
//    private float xDistance, yDistance, xLast, yLast;
//    @Override
//    public boolean onInterceptTouchEvent(MotionEvent ev) {
//        // TODO Auto-generated method stub
//        switch (ev.getAction()) {
//            case MotionEvent.ACTION_DOWN:
//                xDistance = yDistance = 0f;
//                xLast = ev.getX();
//                yLast = ev.getY();
//                break;
//            case MotionEvent.ACTION_MOVE:
//                final float curX = ev.getX();
//                final float curY = ev.getY();
//
//                xDistance += Math.abs(curX - xLast);
//                yDistance += Math.abs(curY - yLast);
//                xLast = curX;
//                yLast = curY;
//
//                if(xDistance > yDistance){
//                    return false;
//                }
//        }
//
//        return super.onInterceptTouchEvent(ev);
//    }
  /*  @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        switch (e.getAction()){
            case MotionEvent.ACTION_DOWN:
                downX = e.getX();
                downY = e.getY();

                lastX = downX;
                lastY = downY;

                break;

            case MotionEvent.ACTION_MOVE:
                moveX = e.getX();
                moveY = e.getY();

                if (Math.abs(moveX-lastX)< Math.abs(moveY-lastY)){
//                    左右滑动

                    lastX = moveX;
                    lastY = moveY;
                    Log.e("q","进来了asdad");
                    return true;
                }


                break;

            case MotionEvent.ACTION_UP:

                break;

            case MotionEvent.ACTION_CANCEL:

                break;
        }

        return super.onInterceptTouchEvent(e);
    }*/
}
