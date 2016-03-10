package com.stickylayout.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.LinearLayout;
import android.widget.Scroller;

import com.stickylayout.listener.IGiveUpTouchListener;

import java.util.NoSuchElementException;

/**
 * Created by weiguangmeng on 16/3/10.
 */
public class StickyLayout extends LinearLayout {
    private final static String TAG = "StickyLayout";
    public static final int LAYOUT_MODE = 1;  //布局模式
    public static final int SCROLL_MODE = 2;  //滑动模式

    private int slideMode = LAYOUT_MODE;

    private View mHeader;  //隐藏的header
    private int mScaleSlop;
    private int mHeaderHeight;
    private int mOriginalHeaderHeight;
    private int mLastY;
    private int mLastYIntercept;
    private IGiveUpTouchListener giveUpTouchListener;
    private Scroller mScroller;


    public StickyLayout(Context context) {
        super(context);
        mScroller = new Scroller(getContext());
    }

    public StickyLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mScroller = new Scroller(getContext());
    }

    @TargetApi(11)
    public StickyLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mScroller = new Scroller(getContext());
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        initData();
    }

    private void initData() {
        int headerId = getResources().getIdentifier("sticky_header", "id", getContext().getPackageName());

        if(headerId != 0) {
            mHeader = findViewById(headerId);
            mScaleSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
            mHeaderHeight = mOriginalHeaderHeight = mHeader.getMeasuredHeight();
            Log.d(TAG, "init header height is " + mHeaderHeight);
        } else {
            throw new NoSuchElementException("sticky header is not exist");
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        boolean intercepted = false;

        int y = (int) event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.d(TAG, " intercepted touch action down");
                mLastY = y;  //这里面如果没有这句就是一个坑啊,因为在action down事件的时候,因为返回为false,所以不会调用StickLayout的onTouchEvent
                mLastYIntercept = y;
                if(!mScroller.isFinished() && SCROLL_MODE == slideMode) {
                    intercepted = true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                int deltaY = y - mLastYIntercept;
                if(giveUpTouchListener.giveUpTouchEvent(event) && deltaY > mScaleSlop) { // 当ListView滑动到顶部并且向向下滑动时,父容器拦截事件
                    intercepted = true;
                }
                break;
            case MotionEvent.ACTION_UP:
                mLastYIntercept = 0;
                break;
        }
        return intercepted;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int y = (int) event.getY();
        int deltaY = 0;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                if(mHeaderHeight > 0) {
                    deltaY = y - mLastY;
                    mHeaderHeight -= deltaY;
                } else {
                    deltaY = 0;
                }

                if(mHeaderHeight < 0) {
                    deltaY = mHeaderHeight + deltaY;
                    mHeaderHeight = 0;
                }

                if(LAYOUT_MODE == slideMode) {
                    MarginLayoutParams p = (MarginLayoutParams) mHeader.getLayoutParams();
                    p.setMargins(0, 0 - mHeaderHeight, 0, 0);
                    mHeader.requestLayout();
                } else if (SCROLL_MODE == slideMode) {
                    scrollBy(0, -deltaY);   //相对距离
                }
                break;
            case MotionEvent.ACTION_UP:
                if(LAYOUT_MODE == slideMode) {
                    smoothSetHeaderHeight(0 - mHeaderHeight, 0 - mOriginalHeaderHeight, 500);
                } else if(SCROLL_MODE == slideMode) {
                    smoothScrollBy(0, mOriginalHeaderHeight - mHeaderHeight, 500);
                }

                mHeaderHeight = mOriginalHeaderHeight;
                break;
        }
        mLastY = y;

        return true;

    }

    @Override
    public void computeScroll() {
        if(mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
        }
    }

    private void smoothScrollBy(int dx, int dy, int duration) {
        mScroller.startScroll(dx, getScrollY(), dx, dy, duration);
        invalidate(); //一定要加上,此时invalidate()会导致View重绘,在View的draw方法中又会调用computeScroll方法
    }

    public void smoothSetHeaderHeight(final int from, final int to, long duration) {
        final int frameCount = (int) ((duration /1000f * 30) + 1); //一秒钟大约30帧
        final float partition = (to -from) /(float)frameCount;  //每一帧的距离
        new Thread() {
            @Override
            public void run() {
                for(int i = 0; i < frameCount; i++) {
                    final int height;
                    if(i == frameCount - 1) {
                        height = to;
                    } else {
                        height = (int) (from + partition * i);
                    }

                    post(new Runnable() {
                        @Override
                        public void run() {
                            MarginLayoutParams p = (MarginLayoutParams) mHeader.getLayoutParams();
                            p.setMargins(0, height, 0, 0);
                            mHeader.requestLayout();
                        }
                    });

                    try {
                        sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    public void setGiveUpTouchListener(IGiveUpTouchListener giveUpTouchListener) {
        this.giveUpTouchListener = giveUpTouchListener;
    }
}
