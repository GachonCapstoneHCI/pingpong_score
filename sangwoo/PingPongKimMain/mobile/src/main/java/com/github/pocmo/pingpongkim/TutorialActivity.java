package com.github.pocmo.pingpongkim;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.widget.Toast;

import com.github.pocmo.pingpongkim.TutorialPages.Tutorial;
import com.github.pocmo.pingpongkim.TutorialPages.Tutorial1;
import com.github.pocmo.pingpongkim.TutorialPages.Tutorial2;
import com.github.pocmo.pingpongkim.TutorialPages.Tutorial3;

public class TutorialActivity extends FragmentActivity implements Tutorial.TutorialNextPage{
    /**
     * The number of pages (wizard steps) to show in this demo.
     */
    private static final int NUM_PAGES = 3;

    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    private CustomViewPager mPager;

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private PagerAdapter mPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);

        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (CustomViewPager) findViewById(R.id.pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());

        mPager.setAdapter(mPagerAdapter);
    }

    @Override
    public void onBackPressed() {
        if (mPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
        }
    }

    //슬라이드를 한 페이지 넘겨준다
    @Override
    public void onMoveNextPage(int id) {
        if(mPager.getCurrentItem() < NUM_PAGES-1)
            mPager.setCurrentItem(mPager.getCurrentItem()+1);
        else {
            Toast.makeText(TutorialActivity.this, "메인 페이지로 이동", Toast.LENGTH_SHORT).show();
            if(id == 3){
                startActivity(new Intent(this, MainActivity.class));
            }
            else{
                startActivity(new Intent(this, MainActivity2.class));
            }

            finish();
        }
    }

    /**
     * A simple pager adapter that represents ScreenSlidePageFragment objects, in
     * sequence.
     */
    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        /**
         * FragmentStatePageAdapter은 getItem 에서 position 으로 구분한다
         * 여기에 서로 다른 fragment 를 추가할 수 있다
         *
         */
        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0:
                    return new Tutorial1();
                case 1:
                    return new Tutorial2();
                case 2:
                    return new Tutorial3();
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }
}
