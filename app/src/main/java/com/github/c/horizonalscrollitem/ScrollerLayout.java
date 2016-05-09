package com.github.c.horizonalscrollitem;

import android.content.Context;
import android.support.v4.view.ViewConfigurationCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

/**
 * 注意：此自定义viewgroup只适用于第一个子控件为match_parent的情况，其实一般情况也都是这种情况
 * 注意：此自定义viewgroup，没有考虑padding的情况，使用者不要在ScrollerLayout里使用任何padding，否则你看到的不是你想要的，
 * 为了实现padding效果，你可以为ScrollerLayout的外层再套一层线性布局（或其他布局），在外层布局里使用padding值
 * 此自定义viewgroup基于郭霖博客改编，想了解具体实现细节，请参照：
 * Android Scroller完全解析，关于Scroller你所需知道的一切
 * http://blog.csdn.net/guolin_blog/article/details/48719871
 */
public class ScrollerLayout extends ViewGroup {


    /**
     * 速度轨迹追踪器
     */
    private VelocityTracker mVelocityTracker;

    /**
     * 此次计算速度你想要的最大值
     */
    private final int mMaxVelocity;

    /**
     * 第一个触点的id， 此时可能有多个触点，但至少一个
     */
    private int mPointerId;

    /**
     * 计算出的横向滚动速率
     */
    private float velocityX;

    /**
     * 手指横向滑动的速率临界值，大于这个值时，不考虑手指滑动的距离，直接滚动到最左边或者最右边
     */
    private int criticalVelocityX = 2500;

    /**
     * 用于完成滚动操作的实例
     */
    private Scroller mScroller;

    /**
     * 判定为拖动的最小移动像素数
     */
    private int mTouchSlop;

    /**
     * 手机按下时的屏幕坐标
     */
    private float mXDown;

    /**
     * 手机当时所处的屏幕坐标
     */
    private float mXMove;

    /**
     * 上次触发ACTION_MOVE事件时的屏幕坐标
     */
    private float mXLastMove;

    /**
     * 界面可滚动的左边界
     */
    private int leftBorder;

    /**
     * 界面可滚动的右边界
     */
    private int rightBorder;

    /**
     * 所有子控件的宽度
     */
    private int totalChildWidth;

    /**
     * 第二个子控件的宽度
     */
    private int secondChildWidth;

    /**
     * 最后一个子控件的宽度
     */
    private int lastChildWidth;

    /**
     * 第一个子控件的宽度
     */
    private int firstChildWidth;

    /**
     * 手指是否是向右滑动
     */
    private boolean scrollToRight;


    public ScrollerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        // 第一步，创建Scroller的实例
        mScroller = new Scroller(context);
        ViewConfiguration configuration = ViewConfiguration.get(context);
        // 获取TouchSlop值
        mTouchSlop = ViewConfigurationCompat.getScaledPagingTouchSlop(configuration);
        //此次计算速度你想要的最大值
        mMaxVelocity = ViewConfiguration.get(context).getMaximumFlingVelocity();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childView = getChildAt(i);
            // 为ScrollerLayout中的每一个子控件测量大小
            measureChild(childView, widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (changed) {
            int childCount = getChildCount();
            /**
             * 当前子控件之前的所有子控件的总宽度
             */
            int preChildViewTotalWidth = 0;
            for (int i = 0; i < childCount; i++) {
                View childView = getChildAt(i);
                // 为ScrollerLayout中的每一个子控件在水平方向上进行布局
                if (i == 0) {
                    childView.layout(
                            0,
                            0,
                            childView.getMeasuredWidth(),
                            childView.getMeasuredHeight());

                } else {
                    childView.layout(
                            preChildViewTotalWidth,
                            0,
                            preChildViewTotalWidth + childView.getMeasuredWidth(),
                            childView.getMeasuredHeight());
                }
                preChildViewTotalWidth += childView.getMeasuredWidth();

            }
            // 初始化左右边界值
            leftBorder = getChildAt(0).getLeft();
            rightBorder = getChildAt(getChildCount() - 1).getRight();

//            获取第一个控件的宽度
            firstChildWidth = getChildAt(0).getMeasuredWidth();

//            获取第二个控件的宽度，为了让下面在判断在整个布局滑动到第二个控件完全显示后放手时，
//            让布局继续往右滚动，直至滚动显示出所有最右边的所有子控件
            secondChildWidth = getChildAt(1).getMeasuredWidth();
//            获取最后一个控件的宽度，当向左滑动超过最后一个控件的宽度时，
//            整个布局向左滑动，完全显示所有的最左侧的控件
            lastChildWidth = getChildAt(getChildCount() - 1).getMeasuredWidth();
//            整个布局里的所有控件的总宽度：
            totalChildWidth = preChildViewTotalWidth;
//            Log.e("c","preChildViewTotalWidth"+preChildViewTotalWidth+"___rightBorder"+rightBorder);
        }
    }


    private int downX;
    private int downY;
//        告诉此ScrollLayout的父布局，什么时候该拦截触摸事件，什么时候不该拦截触摸事件
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //让当前ScrollerLayout对应的父控件不要去拦截事件
                getParent().requestDisallowInterceptTouchEvent(true);
                downX = (int) ev.getX();
                downY = (int) ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                int moveX = (int) ev.getX();
                int moveY = (int) ev.getY();

                //请求父控件recycleView拦截触摸事件，recycleView上下滚动
                if (Math.abs(moveY - downY) > Math.abs(moveX - downX)) {
                    getParent().requestDisallowInterceptTouchEvent(false);
                } else {
                    // //请求父控件recycleView不要拦截触摸事件，ScrollerLayout自己的可以左右滑动
                    getParent().requestDisallowInterceptTouchEvent(true);
                }


                break;

            case MotionEvent.ACTION_CANCEL:


                break;
            case MotionEvent.ACTION_UP:


                break;
        }
        return super.dispatchTouchEvent(ev);
    }



//      ScrollLayout告诉自己什么时候要拦截内部子View的触摸事件，什么时候不要拦截内部子View的触摸事件
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //▲▲▲1.求第一个触点的id， 此时可能有多个触点，但至少一个
                mPointerId = ev.getPointerId(0);
                mXDown = ev.getRawX();
                mXLastMove = mXDown;
                break;
            case MotionEvent.ACTION_MOVE:
                mXMove = ev.getRawX();
                float diff = Math.abs(mXMove - mXDown);
                mXLastMove = mXMove;
                // 当手指拖动值大于TouchSlop值时，认为应该进行滚动，拦截子控件的事件
                if (diff > mTouchSlop) {
                    return true;
                }
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //▲▲▲2.向VelocityTracker添加MotionEvent
        acquireVelocityTracker(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:

                //▲▲▲3.求伪瞬时速度
                mVelocityTracker.computeCurrentVelocity(1000, mMaxVelocity);
                velocityX = mVelocityTracker.getXVelocity(mPointerId);

                mXMove = event.getRawX();
                int scrolledX = (int) (mXLastMove - mXMove);//注意取的是负值，因为是整个布局在动，而不是控件在动
//              如果已经在最左侧了，就不让再往左滑动了
                if (getScrollX() + scrolledX < leftBorder) {
                    scrollTo(leftBorder, 0);
                    return true;//★★★★★★★★★★★★★★★★这里返回true或者false实践证明都可以，但是不能什么都不返回。
                } else if (getScrollX() + getWidth() + scrolledX > rightBorder) {//如果已经在最右侧了，就不让再往右滑动了
                    scrollTo(rightBorder - getWidth(), 0);
                    return true;//★★★★★★★★★★★★★★★★★这里返回true或者false实践证明都可以，但是不能什么都不返回。
                }

                scrollBy(scrolledX, 0);//手指move时，布局跟着滚动
                if (mXDown <= mXMove) {//★★★判断手指左滑动，还是右滑动，要用mXDown，而不是mXLastMove
                    scrollToRight = true;//手指往右滑动

                } else {
                    scrollToRight = false;//手指往左滑动
                }
                mXLastMove = mXMove;
                break;
            case MotionEvent.ACTION_UP:
//                4.▲▲▲释放VelocityTracker
                releaseVelocityTracker();
                // 第二步，当手指抬起时，根据当前的滚动值以及滚动方向来判定应该滚动到哪个子控件的界面，并且记得调用invalidate();

                if (Math.abs(velocityX) > criticalVelocityX) {
                    if (scrollToRight) {
                        mScroller.startScroll(getScrollX(), 0, 0 - getScrollX(), 0);
                    } else {
                        mScroller.startScroll(getScrollX(), 0, (totalChildWidth - firstChildWidth) - getScrollX(), 0);
                    }
                } else {
                    if ((getScrollX() >= secondChildWidth && !scrollToRight)//手指往左滑动，并且滑动完全显示第二个控件时，viewgroup滑动到最右端
                            || ((getScrollX() >= (totalChildWidth - firstChildWidth - lastChildWidth) && scrollToRight))) {//手指往右滑动，并且当滑动没有完全隐藏最后一个控件时，viewgroup滑动到最右端
//                    当滚动值大于某个数字时(大于第二个控件的宽度，即完全显示第二个控件时)并且是向左滑动，让这个viewgroup滑动到整个Viewgroup的最右侧，
//                    因为右侧的所有控件宽度是600，而现在已经滑动的距离是getScrollX，
//                    那么，还应该继续滑动的距离是600-getScrollX()，这里正值表示向右滑动
                        mScroller.startScroll(getScrollX(), 0, (totalChildWidth - firstChildWidth) - getScrollX(), 0);
                    } else if ((getScrollX() <= (totalChildWidth - firstChildWidth - lastChildWidth) && scrollToRight)//手指往右滑动，并且当滑动完全隐藏最后一个控件时，viewgroup滑动到最左端
                            || (getScrollX() <= secondChildWidth && !scrollToRight)) {//手指往左滑动，并且滑动没有完全显示第二个控件时，viewgroup滑动到最左端

//                    当滚动值小于某个数字时，让这个viewgroup滑动到整个Viewgroup的最左侧，
//                    因为滑动到最左侧时，就是让整个viewgroup的滑动量为0，而现在已经滑动的距离是getScrollX，
//                    那么，还应该继续滑动的距离是0-getScrollX()，这里负值表示向左滑动
                        mScroller.startScroll(getScrollX(), 0, 0 - getScrollX(), 0);
                    }
                }
//                必须调用invalidate()重绘
                invalidate();

                break;

            case MotionEvent.ACTION_CANCEL:
//              5.▲▲▲释放VelocityTracker
                releaseVelocityTracker();
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void computeScroll() {
        // 第三步，重写computeScroll()方法，并在其内部完成平滑滚动的逻辑
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            invalidate();
        }
    }

    /**
     * @param event 向VelocityTracker添加MotionEvent
     * @see android.view.VelocityTracker#obtain()
     * @see android.view.VelocityTracker#addMovement(MotionEvent)
     */
    private void acquireVelocityTracker(final MotionEvent event) {
        if (null == mVelocityTracker) {
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
        if (null != mVelocityTracker) {
            mVelocityTracker.clear();
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }


     /*   getScrollX()指的是由viewgroup调用View的scrollTo(int x, int y)或者scrollBy(int x, int y)产生的X轴的距离
//                换句话说，就是你手指每次滑动，引起的是viewgroup累计滑动的距离，右为正
//                指的是相当于控件的左上角的为原点的坐标值
                Log.e("qqq","getX():"+event.getX());
//                指的是相当于屏幕的左上角的为原点的坐标值
                Log.e("qqq","getRawX():"+event.getRawX());*/
}