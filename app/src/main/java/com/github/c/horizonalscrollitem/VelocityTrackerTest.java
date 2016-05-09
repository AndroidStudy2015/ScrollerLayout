package com.github.c.horizonalscrollitem;
import android.app.Activity;  
import android.graphics.Color;  
import android.os.Bundle;  
import android.view.MotionEvent;  
import android.view.VelocityTracker;  
import android.view.ViewConfiguration;  
import android.view.ViewGroup.LayoutParams;  
import android.widget.TextView;  
  
public class VelocityTrackerTest extends Activity {  
    private TextView mInfo;  
  
    private VelocityTracker mVelocityTracker;  
    private int mMaxVelocity;  
  
    private int mPointerId;  
  
    @Override  
    protected void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);  
  
        mInfo = new TextView(this);  
        mInfo.setLines(4);  
        mInfo.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));  
        mInfo.setTextColor(Color.WHITE);  
  
        setContentView(mInfo);  
//      0.获得此次最大速率
        mMaxVelocity = ViewConfiguration.get(this).getMaximumFlingVelocity();  
    }  
  
    @Override  
    public boolean onTouchEvent(MotionEvent event) {  
        final int action = event.getAction();  
        acquireVelocityTracker(event);  //1.向VelocityTracker添加MotionEvent
        final VelocityTracker verTracker = mVelocityTracker;  
        switch (action) {  
            case MotionEvent.ACTION_DOWN:  
                //2.求第一个触点的id， 此时可能有多个触点，但至少一个
                mPointerId = event.getPointerId(0);  
                break;  
  
            case MotionEvent.ACTION_MOVE:  
                //3.求伪瞬时速度
                verTracker.computeCurrentVelocity(1000, mMaxVelocity);  
                final float velocityX = verTracker.getXVelocity(mPointerId);  
                final float velocityY = verTracker.getYVelocity(mPointerId);  
                recodeInfo(velocityX, velocityY);  
                break;  
  
            case MotionEvent.ACTION_UP:
//                4.ACTION_UP释放VelocityTracker,交给其他控件使用
                releaseVelocityTracker();  
                break;
//                5.ACTION_CANCEL释放VelocityTracker ,交给其他控件使用
            case MotionEvent.ACTION_CANCEL:  
                releaseVelocityTracker();  
                break;  
  
            default:  
                break;  
        }  
        return super.onTouchEvent(event);  
    }  
  
    /**  
     *  
     * @param event 向VelocityTracker添加MotionEvent  
     *  
     * @see android.view.VelocityTracker#obtain()  
     * @see android.view.VelocityTracker#addMovement(MotionEvent)  
     */  
    private void acquireVelocityTracker(final MotionEvent event) {  
        if(null == mVelocityTracker) {  
            mVelocityTracker = VelocityTracker.obtain();  
        }  
        mVelocityTracker.addMovement(event);  
    }  
  
    /**  
     * 释放VelocityTracker  
     *  
     * @see android.view.VelocityTracker#clear()  
     * @see android.view.VelocityTracker#recycle()  
     */  
    private void releaseVelocityTracker() {  
        if(null != mVelocityTracker) {  
            mVelocityTracker.clear();  
            mVelocityTracker.recycle();  
            mVelocityTracker = null;  
        }  
    }  
  
    private static final String sFormatStr = "velocityX=%f\nvelocityY=%f";  
  
    /**  
     * 记录当前速度  
     *  
     * @param velocityX x轴速度  
     * @param velocityY y轴速度  
     */  
    private void recodeInfo(final float velocityX, final float velocityY) {  
        final String info = String.format(sFormatStr, velocityX, velocityY);  
        mInfo.setText(info);  
    }  
}  