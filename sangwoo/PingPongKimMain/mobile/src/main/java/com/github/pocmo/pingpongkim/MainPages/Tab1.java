package com.github.pocmo.pingpongkim.MainPages;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.github.pocmo.pingpongkim.MainActivity2;
import com.github.pocmo.pingpongkim.MatchActivity;
import com.github.pocmo.pingpongkim.R;

/**
 * A simple {@link Fragment} subclass.
 * Tab1 : 메인 페이지
 */
public class Tab1 extends Fragment {

    private Button buttonStartGame;
    private OnFragmentInteractionListener mListener;

    public Tab1() {}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_tab1, container, false);

        //뷰 객체화
        buttonStartGame = (Button)rootView.findViewById(R.id.buttonStartGame);

        //경기 상대를 찾기 위한  matchactivity 로 이동
        buttonStartGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent matchStartIntent = new Intent(getActivity(), MatchActivity.class);
                startActivity(matchStartIntent);
            }
        });
        return rootView;
    }

    // TODO: Rename method, update argument and hook method into UI event

    @Override
    public void onAttach(Activity context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
