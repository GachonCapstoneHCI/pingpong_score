package com.github.pocmo.pingpongkim;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by garyNoh on 2018. 3. 12..
 * 뷰페이저 슬라이드로 넘어가는 것 방지
 */

public class CustomViewPager extends ViewPager {
    public CustomViewPager(Context context) {super(context);}
    public CustomViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    //false 로 설정해서 스와이프를 방지한다
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {return false;}
    @Override
    public boolean onTouchEvent(MotionEvent ev) {return false;}
}
