package com.github.pocmo.pingpongkim.TutorialPages;


import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.github.pocmo.pingpongkim.R;


/**
 * A simple {@link Fragment} subclass.
 * Tutorial1 : 회원가입 페이지
 *
 *
 * TODO :
 * 회원가입 페이지 (사용자 닉네임, 나이, 키, 몸무게, 탁구 실력정도 등)
 * 서버 쪽에 데이터를 보내서 가능하면 다음 페이지로 넘어가고,
 * 데이터가 보내지지 않으면 다음 페이지로 넘어가지 않음
 */
public class Tutorial1 extends Tutorial {

    private Tutorial.TutorialNextPage mListener;

    //뷰
    private EditText userName, userPassword, userWeight, userHeight;    //사용자 정보
    private Button buttonUserInfoSubmit;    //제출 버튼

    private Button s1, s2, s3;



    public Tutorial1() {}

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

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            ViewGroup rootView = (ViewGroup) inflater.inflate(
                    R.layout.fragment_tutorial1, container, false);
            //뷰 객체화
            userName = rootView.findViewById(R.id.userNickname);
            userPassword = rootView.findViewById(R.id.userPassword);
            userHeight = rootView.findViewById(R.id.userHeight);
            userWeight = rootView.findViewById(R.id.userWeight);

            s1 = rootView.findViewById(R.id.button_sang);
            s2 = rootView.findViewById(R.id.button_joong);
            s3 = rootView.findViewById(R.id.button_ha);

            s1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    s1.setBackground(getResources().getDrawable(R.drawable.skill_choice_sang));
                }
            });

            s2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                s2.setBackground(getResources().getDrawable(R.drawable.skill_choice_joong));
            }
        });

        //TODO : 서버로 전송

        //TODO : 사용자 정보 입력 뷰 메모리에 올리기
        //버튼
        buttonUserInfoSubmit = (Button)rootView.findViewById(R.id.buttonUserInfoSubmit);
        buttonUserInfoSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO : 사용자 정보 서버로 전송
                boolean isSuccess = true;

                if(isSuccess){
                    //다음 페이지로 이동
                    //액티비티로 메시지 전송
                    Toast.makeText(getContext(), "로그인 되었습니다~", Toast.LENGTH_SHORT).show();
                    mListener.onMoveNextPage(1);
                }
            }
        });




        return rootView;
    }
}

