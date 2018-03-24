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
        final ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_tutorial2, container, false);
        buttonCheckWatchConnection = (Button)rootView.findViewById(R.id.buttonCheckWatchConnection);
        remoteSensorManager = RemoteSensorManager.getInstance(rootView.getContext());
        //final List<Sensor> sensors = RemoteSensorManager.getInstance(rootView.getContext()).getSensors();


        buttonCheckWatchConnection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Sensor> sensors = RemoteSensorManager.getInstance(rootView.getContext()).getSensors();
                for(Sensor s : sensors){
                    remoteSensorManager.filterBySensorId((int)s.getId());
                }

                BusProvider.getInstance().register(rootView.getContext());

                //연결 시작
                remoteSensorManager.startMeasurement();
                remoteSensorManager.getNodes(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                    @Override
                    public void onResult(final NodeApi.GetConnectedNodesResult pGetConnectedNodesResult) {
                        mNodes = pGetConnectedNodesResult.getNodes();
                        for(Node n : mNodes){
                            Log.e("nogary : ", n.getDisplayName());
                        }
                    }
                });
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
        });
        return rootView;
    }
    @Subscribe
    public void onNewSensorEvent(final NewSensorEvent event) {
        Log.e("nogary", "new sensor detected");
    }

}
