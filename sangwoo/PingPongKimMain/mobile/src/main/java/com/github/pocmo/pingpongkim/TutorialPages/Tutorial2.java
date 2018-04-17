package com.github.pocmo.pingpongkim.TutorialPages;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.github.pocmo.pingpongkim.MainActivity2;
import com.github.pocmo.pingpongkim.PlayActivity;
import com.github.pocmo.pingpongkim.R;
import com.github.pocmo.pingpongkim.RemoteSensorManager;
import com.github.pocmo.pingpongkim.SensorReceiverService;
import com.github.pocmo.pingpongkim.data.Sensor;
import com.github.pocmo.pingpongkim.events.BusProvider;
import com.github.pocmo.pingpongkim.events.NewSensorEvent;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.squareup.otto.Subscribe;

import java.net.URL;
import java.util.List;

/**
 * A simple {@link Fragment} subclass
 * 스마트워치와 잘 연결돼어있는지 확인하는 페이지
 */
public class Tutorial2 extends Tutorial {

    private RemoteSensorManager remoteSensorManager;
    private List<Node> mNodes;

    Tutorial.TutorialNextPage mListener;

    //뷰
    ImageView buttonCheckWatchConnection;

    private TextView txtProgress;
    private ProgressBar progressBar;
    private int pStatus = 0;
    private Handler handler = new Handler();

    //프로그레스바
    RelativeLayout buttonLayout;
    RelativeLayout progressBarLayout;
    //TextView textView1, textView2, textView3;

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
        remoteSensorManager = RemoteSensorManager.getInstance(rootView.getContext());
        //final List<Sensor> sensors = RemoteSensorManager.getInstance(rootView.getContext()).getSensors();

        //뷰
        buttonCheckWatchConnection = (ImageView)rootView.findViewById(R.id.buttonCheckWatchConnection);
        buttonLayout = rootView.findViewById(R.id.buttonLayout);
        progressBarLayout = rootView.findViewById(R.id.progressBarLayout);

        //스마트 워치 연결 확인
        //필요한가
//        List<Sensor> sensors = RemoteSensorManager.getInstance(rootView.getContext()).getSensors();
//        for(Sensor s : sensors){
//            remoteSensorManager.filterBySensorId((int)s.getId());
//        }

        //연결 확인을 하기 위해 버튼을 누른다
        buttonCheckWatchConnection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //버튼은 없어지고 원형 진행바가 나타난다
                buttonLayout.setVisibility(View.INVISIBLE);
                progressBarLayout.setVisibility(View.VISIBLE);
                txtProgress = (TextView) rootView.findViewById(R.id.txtProgress);
                progressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
                //진행바가 0에서 100으로 이동한다
                new ProgressBarTask().execute();
            }
        });
        return rootView;
    }
    @Subscribe
    public void onNewSensorEvent(final NewSensorEvent event) {
    }

    private class ProgressBarTask extends AsyncTask<Void, Integer, String> {
        protected String doInBackground(Void... params) {

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

            return null;
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(String s){
            //start measurement  를 해야지 센서들을 가져올 수 있다
            remoteSensorManager.startMeasurement();
            BusProvider.getInstance().register(rootView.getContext());
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
                                remoteSensorManager.stopMeasurement();
                                //중지
                                BusProvider.getInstance().unregister(this);
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
    }




}
