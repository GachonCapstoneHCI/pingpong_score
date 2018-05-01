package com.github.pocmo.pingpongkim.TutorialPages;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.github.pocmo.pingpongkim.R;
import com.github.pocmo.pingpongkim.SensorReceiverService;
import com.github.pocmo.pingpongkim.TutorialActivity;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Wearable;

/**
 * A simple {@link Fragment} subclass.
 * Tutorial3 : 사용자의 스윙 폼 데이터 수집하여 칼리브레이션하는 페이지
 */
public class Tutorial3 extends Tutorial {
    private Tutorial.TutorialNextPage mListener;    //다음 페이지 이동 리스너
    private MyReceiver myReceiver;  //튜토리얼 스윙에 대한 브로드캐스트 리시버
    private ViewGroup rootView;
    private TableRow[] tutorialGuides;  //5가지의 텍스트
    private int count = 0;  //스윙 횟수

    Handler mHandler;

    //뷰
    private Button buttonTemp;
    private Button buttonTemp2;
    private ImageView swingImg;

    @Override
    public void onAttach(Activity activity) {

        //다음 페이지로 이동하는 리스너가 구현되었는지 확인
        super.onAttach(activity);
        try{
            mListener = (Tutorial.TutorialNextPage)activity;
        }
        catch (ClassCastException e){
            throw new ClassCastException(activity.toString() + " TutorialNextPage를 구현해아 함");
        }
    }

    public Tutorial3() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_tutorial3, container, false);

        //뷰 객체화

        //id 가 3이면 메인 액티비티(개발용 그래프)를 보여줌

        //id 가 4면 메인 액티비티2(메인 페이지)를 보여줌
        buttonTemp2 = (Button)rootView.findViewById(R.id.buttonTemp2);
        swingImg = (ImageView)rootView.findViewById(R.id.swingImg);
        buttonTemp2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //mListener.onMoveNextPage(4);
                //실행
                mHandler = new Handler();
                playTutorialCounter();
            }
        });

        //튜토리얼 가이드 텍스트 (1~5단계)
        tutorialGuides = new TableRow[5];
        tutorialGuides[0] = rootView.findViewById(R.id.tableRow1);
        tutorialGuides[1] = rootView.findViewById(R.id.tableRow2);
        tutorialGuides[2] = rootView.findViewById(R.id.tableRow3);
        tutorialGuides[3] = rootView.findViewById(R.id.tableRow4);
        tutorialGuides[4] = rootView.findViewById(R.id.tableRow5);

        //리시버 등록 : 센서 읽는 서비스에서 튜토리얼 스윙했다는 메시지를 보내면 받음
        myReceiver = new MyReceiver();
        rootView.getContext().registerReceiver(myReceiver, makeIntentFilter());




        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    void playTutorialCounter(){
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {

                tutorialGuides[0].setVisibility(View.VISIBLE);
                swingImg.setBackground(getResources().getDrawable(R.drawable.tutorial_swing1));

            }
        }, 3000);

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {

                tutorialGuides[1].setVisibility(View.VISIBLE);
                swingImg.setBackground(getResources().getDrawable(R.drawable.tutorial_swing2));
            }
        }, 5000);

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {

                tutorialGuides[2].setVisibility(View.VISIBLE);
                swingImg.setBackground(getResources().getDrawable(R.drawable.tutorial_swing3));
            }
        }, 7500);

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {

                tutorialGuides[3].setVisibility(View.VISIBLE);
                swingImg.setBackground(getResources().getDrawable(R.drawable.tutorial_swing4));
            }
        }, 10000);

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                swingImg.setImageResource(0);
                tutorialGuides[4].setVisibility(View.VISIBLE);
                swingImg.setBackground(getResources().getDrawable(R.drawable.tutorial_swing5));
                Toast.makeText(rootView.getContext(), "학습이 종료되었습니다. 메인 페이지로 이동합니다", Toast.LENGTH_SHORT).show();
            }
        }, 12000);

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mListener.onMoveNextPage(4);
            }
        }, 15000);
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        rootView.getContext().unregisterReceiver(myReceiver);   //리시버 해제
    }

    /**
     * 튜토리얼 스윙에 대한 메시지 받는 브로드캐스트 리시버
      */
    class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action != null && action.equals(SensorReceiverService.ACTION_TUTORIAL_CALB)){
                if(count <= 5){
                    count++;
                    //텍스트를 하나씩 보여준다
                    tutorialGuides[count-1].setVisibility(View.GONE);
                    tutorialGuides[count].setVisibility(View.VISIBLE);
                }
                //마지막 페이지에서는 메인 페이지로 이동한다
                if(count == 5){
                    Toast.makeText(getContext(), "칼리브레이션이 끝났습니다. 메인페이지로 이동하세요", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    /**
     * 브로드캐스트에 등록을 할 때 intentFilter 를 보내는데 여기에 3가지 action 을 추가함
     * @return
     */
    private static IntentFilter makeIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SensorReceiverService.ACTION_TUTORIAL_CALB);
        return intentFilter;
    }

}
