package com.github.pocmo.pingpongkim;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.github.pocmo.pingpongkim.MainPages.Tab1;
import com.github.pocmo.pingpongkim.MainPages.Tab2;

/**
 * Tab1, Tab2 이 있는 메인 페이지
 */
public class MainActivity2 extends AppCompatActivity implements Tab1.OnFragmentInteractionListener,
        Tab2.OnFragmentInteractionListener2{

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private MyReceiver myReceiver;
    private ViewPager mViewPager;
    private final int numOfPage = 2;    //탭 개수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        //툴바 설정
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // 뷰페이저 설정
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        //탭설정
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        //리시버 등록
        myReceiver = new MyReceiver();
        registerReceiver(myReceiver, makeIntentFilter());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    //Tab1 fragment
    @Override
    public void onFragmentInteraction(Uri uri) {}

    /**
     * 뷰페이저 관리
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0:
                    return new Tab1();
                case 1:
                    return new Tab2();
                default:
                    return null;
            }
        }
        @Override
        public int getCount() {
            return numOfPage;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(myReceiver); //리시버 해제
    }

    /*
     PLAY 액션이 들어오면 PLAY ACTIVITY 를 띄움
     TODO : PLAY 액션 : 경기 시작 버튼 누르면?
     */
    class MyReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action != null && action.equals(SensorReceiverService.ACTION_START_PLAY)){
                //경기중 액티비티를 띄운다
                Intent playActivity = new Intent(MainActivity2.this, PlayActivity.class);
                startActivity(playActivity);
            }
        }
    }

    /**
     * SensorService에서 전송한다
     * @return
     */
    private static IntentFilter makeIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SensorReceiverService.ACTION_START_PLAY);
        return intentFilter;
    }
}
