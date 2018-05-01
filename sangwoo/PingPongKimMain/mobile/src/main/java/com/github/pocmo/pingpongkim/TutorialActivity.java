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

/**
 * Tutorial1,2,3을 보여주는 튜토리얼 액티비티
 */
public class TutorialActivity extends FragmentActivity implements Tutorial.TutorialNextPage{

    private static final int NUM_PAGES = 3;
    private CustomViewPager mPager;
    private PagerAdapter mPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);

        mPager = findViewById(R.id.pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
    }

    @Override
    public void onBackPressed() {
        //첫 페이지일 때 뒤로가기를 누르면 액티비티 종료
        if (mPager.getCurrentItem() == 0) {super.onBackPressed();}
        //첫 페이지가 아니면 이전단계로 이동
        else {mPager.setCurrentItem(mPager.getCurrentItem() - 1);}
    }

    @Override
    public void onMoveNextPage(int id) {
        //마지막 페이지가 아니라면 다음 페이지로 이동
        if(mPager.getCurrentItem() < NUM_PAGES-1){
            mPager.setCurrentItem(mPager.getCurrentItem()+1);
        }
        //마지막 페이지라면 메인액티비티2로 이동
        else {

            //id 가 3이면 개발용 그래프 페이지를 띄워줌
            if(id == 3){startActivity(new Intent(this, MainActivity.class));}
            //id가 3이 아니면 메인페이지를 띄워줌
            else{startActivity(new Intent(this, MainActivity2.class));}
            finish();   //튜토리얼 페이지 종료
        }
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0:
                    return new Tutorial1(); //튜토리얼 1
                case 1:
                    return new Tutorial2(); //튜토리얼 2
                case 2:
                    return new Tutorial3(); //튜토리얼 3
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
