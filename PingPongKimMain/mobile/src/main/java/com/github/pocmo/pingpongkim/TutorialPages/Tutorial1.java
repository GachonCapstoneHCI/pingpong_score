package com.github.pocmo.pingpongkim.TutorialPages;


import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.github.pocmo.pingpongkim.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class Tutorial1 extends Tutorial {

//    public interface OnMoveToNextPage{
//        void OnMoveToNextPage(int currentItem);
//    }

    //
    Tutorial.TutorialNextPage mListener;

    //뷰
    Spinner userAbilityLevelSpinner;
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


        //TODO : 사용자 정보 입력 뷰 메모리에 올리기


        //스피너 뷰
        userAbilityLevelSpinner = (Spinner)rootView.findViewById(R.id.userAbilityLevel);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(rootView.getContext(),
                R.array.userAbilityLevel, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        userAbilityLevelSpinner.setAdapter(adapter);

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

