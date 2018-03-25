package com.github.pocmo.pingpongkim.TutorialPages;


import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.github.pocmo.pingpongkim.R;
import com.github.pocmo.pingpongkim.RemoteSensorManager;
import com.github.pocmo.pingpongkim.data.Sensor;
import com.github.pocmo.pingpongkim.events.BusProvider;
import com.github.pocmo.pingpongkim.events.NewSensorEvent;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.squareup.otto.Subscribe;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class Tutorial2 extends Tutorial {

    private RemoteSensorManager remoteSensorManager;
    private List<Node> mNodes;


    Tutorial.TutorialNextPage mListener;

    //뷰
    Button buttonCheckWatchConnection;

    private TextView txtProgress;
    private ProgressBar progressBar;
    private int pStatus = 0;
    private Handler handler = new Handler();

    //프로그레스바
    RelativeLayout buttonLayout;
    RelativeLayout progressBarLayout;
    TextView textView1, textView2, textView3;

    ViewGroup rootView;


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


    public Tutorial2() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_tutorial2, container, false);
        buttonCheckWatchConnection = (Button)rootView.findViewById(R.id.buttonCheckWatchConnection);
        remoteSensorManager = RemoteSensorManager.getInstance(rootView.getContext());
        //final List<Sensor> sensors = RemoteSensorManager.getInstance(rootView.getContext()).getSensors();

        buttonLayout = rootView.findViewById(R.id.buttonLayout);
        progressBarLayout = rootView.findViewById(R.id.progressBarLayout);


        //스마트 워치 연결 확인
        List<Sensor> sensors = RemoteSensorManager.getInstance(rootView.getContext()).getSensors();
        for(Sensor s : sensors){
            remoteSensorManager.filterBySensorId((int)s.getId());
        }

        buttonCheckWatchConnection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //연결 버튼을 누르면 진행바 이동
                buttonLayout.setVisibility(View.INVISIBLE);
                progressBarLayout.setVisibility(View.VISIBLE);
                txtProgress = (TextView) rootView.findViewById(R.id.txtProgress);
                progressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (pStatus <= 100) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    progressBar.setProgress(pStatus);
                                    txtProgress.setText(pStatus + " %");
                                }
                            });
                            try {
                                Thread.sleep(30);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            pStatus++;
                        }
                        //BusProvider.getInstance().register(rootView.getContext());

                        //연결 시작
                        //remoteSensorManager.startMeasurement();
                        remoteSensorManager.getNodes(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                            @Override
                            public void onResult(final NodeApi.GetConnectedNodesResult pGetConnectedNodesResult) {
                                mNodes = pGetConnectedNodesResult.getNodes();
                                if(mNodes.size() > 0){
                                    //Log.e("nogary : ", n.getDisplayName());
                                    Toast.makeText(rootView.getContext(), "스마트 워치 "+ mNodes.get(0).getDisplayName() + " 와 연결되어있네용! 5초 후 다음 페이지로 이동합니다" , Toast.LENGTH_SHORT).show();
                                    //3초 후에 다음 페이지로 이동
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            //remoteSensorManager.stopMeasurement();
                                            //중지
                                            //BusProvider.getInstance().unregister(this);
                                            mListener.onMoveNextPage(2);
                                        }
                                    }, 5000);
                                }
                                else{
                                    Toast.makeText(rootView.getContext(), "스마트 워치와 연결되어있지 않습니다!" , Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }).start();



            }
        });
        return rootView;
    }
    @Subscribe
    public void onNewSensorEvent(final NewSensorEvent event) {
        Log.e("nogary", "new sensor detected");
    }

}
