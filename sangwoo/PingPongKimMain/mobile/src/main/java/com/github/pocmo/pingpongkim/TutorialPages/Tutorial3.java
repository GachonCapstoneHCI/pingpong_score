package com.github.pocmo.pingpongkim.TutorialPages;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.pocmo.pingpongkim.R;
import com.github.pocmo.pingpongkim.SensorReceiverService;

/**
 * A simple {@link Fragment} subclass.
 */
public class Tutorial3 extends Tutorial {
    Tutorial.TutorialNextPage mListener;
    MyReceiver myReceiver;
    ViewGroup rootView;
    TextView[] tutorialGuideTextViews;
    private int count = 0;

    //뷰
    private Button buttonTemp;
    private Button buttonTemp2;

    @Override
    public void onAttach(Activity activity) {

        //액티비티가 인터페이스를 구현하지 않으면 에러를 발생시킨다
        super.onAttach(activity);
        try{
            mListener = (Tutorial.TutorialNextPage)activity;
        }
        catch (ClassCastException e){
            throw new ClassCastException(activity.toString() + " must implement TutorialNextPage");
        }
    }


    public Tutorial3() {}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_tutorial3, container, false);

        //뷰 초기화
        buttonTemp = (Button)rootView.findViewById(R.id.buttonTemp);
        //id 가 3이면 메인 액티비티(개발용 그래프)를 보여준다
        buttonTemp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onMoveNextPage(3);
            }
        });
        //id 가 4면 메인 액티비티2(메인 페이지)를 보여준다
        buttonTemp2 = (Button)rootView.findViewById(R.id.buttonTemp2);
        buttonTemp2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onMoveNextPage(4);
            }
        });

        //튜토리얼 가이드 텍스트 (1~5단계까지 있음)
        tutorialGuideTextViews = new TextView[5];
        //tutorialGuideTextViews[0] = rootView.findViewById(R.id.tutorialSwingGuideText1);
        //tutorialGuideTextViews[1] = rootView.findViewById(R.id.tutorialSwingGuideText2);
        //tutorialGuideTextViews[2] = rootView.findViewById(R.id.tutorialSwingGuideText3);
        //tutorialGuideTextViews[3] = rootView.findViewById(R.id.tutorialSwingGuideText4);
        //tutorialGuideTextViews[4] = rootView.findViewById(R.id.tutorialSwingGuideText5);

        //리시버 등록 : 센서 읽는 서비스에서 튜토리얼 스윙했다는 메시지를 보내면 받는다
        myReceiver = new MyReceiver();
        rootView.getContext().registerReceiver(myReceiver, makeIntentFilter());
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        rootView.getContext().unregisterReceiver(myReceiver);
    }

    /**
     * 튜토리얼 스윙에 대한 메시지 받는 브로드캐스트 리시버
     * (리시버에서 받는 건 '스윙했다!' 임)
      */
    class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action != null && action.equals(SensorReceiverService.ACTION_TUTORIAL_CALB)){
                if(count <= 5){
                    count++;
                    //텍스트를 하나씩 보여준다
                    tutorialGuideTextViews[count-1].setVisibility(View.GONE);
                    tutorialGuideTextViews[count].setVisibility(View.VISIBLE);
                }
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
