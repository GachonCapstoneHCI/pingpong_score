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
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.github.pocmo.pingpongkim.R;


/**
 * A simple {@link Fragment} subclass.
 * 회원가입 페이지 (사용자 닉네임, 나이, 키, 몸무게, 탁구 실력정도 등)
 * 서버 쪽에 데이터를 보내서 가능하면 다음 페이지로 넘어가고,
 * 데이터가 보내지지 않으면 다음 페이지로 넘아가지 않음
 */
public class Tutorial1 extends Tutorial {

    Tutorial.TutorialNextPage mListener;

    //뷰
    EditText userName, userPassword, userWeight, userHeight;
    RadioGroup userSkills;

    //Spinner userAbilityLevelSpinner;
    Button buttonUserInfoSubmit;

    public Tutorial1() {
        // Required empty public constructor
    }

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
        userSkills = rootView.findViewById(R.id.userSkill);
        int id = userSkills.getCheckedRadioButtonId();

        //서버로 전송



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
                    mListener.onMoveNextPage(1);
                }
            }
        });
        return rootView;
    }
}

