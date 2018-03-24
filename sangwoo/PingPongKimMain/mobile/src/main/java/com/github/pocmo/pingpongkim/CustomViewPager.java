package com.github.pocmo.pingpongkim;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by garyNoh on 2018. 3. 12..
 */

public class CustomViewPager extends ViewPager {
    public CustomViewPager(Context context) {

        super(context);
    }

    public CustomViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        //손가락 swipe 방지
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        //손가락 swipe 방지
        return false;
    }
}
