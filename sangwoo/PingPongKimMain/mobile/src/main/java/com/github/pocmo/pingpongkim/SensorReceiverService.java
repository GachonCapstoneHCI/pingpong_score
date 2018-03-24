package com.github.pocmo.pingpongkim;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.github.pocmo.pingpongkim.shared.DataMapKeys;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.Arrays;

public class SensorReceiverService extends WearableListenerService {
    public static final String ACTION_RECEIVE_SENSOR_DATA = "pingpongboy.sensordata";
    public static final String ACTION_DETECT_SWING = "pingpongboy.detectswing";
    private static final String TAG = "PingPongBoy";
    private boolean isPlaying = false;

    double prev_gravity_z = 9;
    boolean isHorizontalSwing = true;
    boolean isBackSwing = false;


    private RemoteSensorManager sensorManager;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "SensorReceiverService onCreate");
        sensorManager = RemoteSensorManager.getInstance(this);
    }

    @Override
    public void onPeerConnected(Node peer) {
        super.onPeerConnected(peer);

        Log.i(TAG, "Connected: " + peer.getDisplayName() + " (" + peer.getId() + ")");
    }

    @Override
    public void onPeerDisconnected(Node peer) {
        super.onPeerDisconnected(peer);

        Log.i(TAG, "Disconnected: " + peer.getDisplayName() + " (" + peer.getId() + ")");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isPlaying = false;
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.d(TAG, "onDataChanged()");
        if(!isPlaying){
            //새로운 액티비티 실행
            Intent intent = new Intent();
            intent.setAction("play");
            isPlaying = true;
            sendBroadcast(intent);
        }

        for (DataEvent dataEvent : dataEvents) {
            if (dataEvent.getType() == DataEvent.TYPE_CHANGED) {
                DataItem dataItem = dataEvent.getDataItem();
                Uri uri = dataItem.getUri();
                String path = uri.getPath();

                if (path.startsWith("/sensors/")) {
                    unpackSensorData(
                        Integer.parseInt(uri.getLastPathSegment()),
                        DataMapItem.fromDataItem(dataItem).getDataMap()
                    );
                }
            }
        }
    }




    private void unpackSensorData(int sensorType, DataMap dataMap) {
        int accuracy = dataMap.getInt(DataMapKeys.ACCURACY);
        long timestamp = dataMap.getLong(DataMapKeys.TIMESTAMP);
        float[] values = dataMap.getFloatArray(DataMapKeys.VALUES);
        float[] svm_values = new float[1];

        Log.d(TAG, "Received sensor data " + sensorType + " = " + Arrays.toString(values));

        Log.e(TAG, "isHorizontal : " + Boolean.toString(isHorizontalSwing) + " isBackSwing : " + Boolean.toString(isBackSwing));

        //sensorType 1 은 가속도?
        if(sensorType == 1){
//            String s = Integer.toString(accuracy) + " / " + Long.toString(timestamp) + " / " + '\n'
//                    + Float.toString(values[0]) + " , " + Float.toString(values[1]) + " , " + Float.toString(values[2]);
//            Intent intent = new Intent(ACTION_RECEIVE_SENSOR_DATA);
//            intent.putExtra("sensordata", s);
//            sendBroadcast(intent);

            //스윙 동작 계산

            Intent intent = new Intent(ACTION_DETECT_SWING);
            double result = checkIsSwing(values);
            //String msg = result > 0 ? "SWING" : "-";
            String msg;

            if(result > 0){
                if(isHorizontalSwing)msg = "SWING";
                else if(isBackSwing) msg = "BACK SWING";
                else msg = "NONE";
            }
            else{
                msg = "-";
            }
            intent.putExtra("sensordata", result);
            intent.putExtra("swingdetect", msg);
            intent.putExtra("timestamp", timestamp);
            sendBroadcast(intent);


            //swing 이 아니면 0 을 넣고 swing 이면 svm 값을 넣어서 그래프를 하나만 찍도록 한다
            if(msg.equals("-") || msg.equals("NONE"))
                svm_values[0] = 0;
            else {
                svm_values[0] = (float) (Math.pow(values[0], 2) + Math.pow(values[1], 2));
                //가속도 값 values 를 svm 값 하나로 만들자
            }

            values = svm_values;
        }
        //gravity 센서
        else if(sensorType == 9){
            //TODO : 일단 두 값의 평균을 구한다. 그리고 나중에는 일정 interval의 데이터를 구한다
            double gravity_z_mean = (prev_gravity_z + values[2])/2.0;
            prev_gravity_z = values[2];
            Log.e(TAG, "Z_GRAVITY MEAN -> " + Double.toString(gravity_z_mean));
            Log.e(TAG, "Z_GRAVITY CURRENT -> " + Double.toString(values[2]));

            //펜홀더의 경우 horizontal 인 경우 대체로 8 이상의 값은 나온다 평균을 냈을 때 8 이상이라고 하면
            if(gravity_z_mean >= 7){
                isHorizontalSwing = true;
                isBackSwing = false;
            }
            //펜홀더의 경우 vertical 인 경우 대체로 -0 ~ -8 사이의 값이 나온다
            else if(gravity_z_mean >= -7 && gravity_z_mean <= 0){
                isHorizontalSwing = false;
                isBackSwing = false;
            }
            //펜홀더의 경우 back swing 인 경우 대체로 -8 ~ -15 사이의 값이 나온다
            else if(gravity_z_mean <= -8){
                isHorizontalSwing = false;
                isBackSwing = true;
            }
        }
        sensorManager.addSensorData(sensorType, accuracy, timestamp, values);
    }


    //swing detect alogrithm
    boolean isSkipping = false;
    int skipCount = 0;
    //int swingCount = 0;
    final int baseThreshold = 120;
    final int skipInterval = 10;


    double checkIsSwing(float[] values){
        Log.e(TAG, "check is swing");
        Log.e(TAG, "skip count : " + Integer.toString(skipCount) + " isSkipping : " + Boolean.toString(isSkipping));

        if(values[0] < 15 && values[1] < 15) {
            double svm_val = Math.pow(values[0], 2) + Math.pow(values[1], 2) + Math.pow(values[2], 2);
            Log.e(TAG, "SVM_VALUE -> " + Double.toString(svm_val));
            if (isSkipping) {
                skipCount++;
                if (skipCount > skipInterval) {
                    skipCount = 0;
                    isSkipping = false;
                }
            } else {
                if (svm_val > baseThreshold) {
                    isSkipping = true;
                    skipCount++;
                    return svm_val;
                }
            }
        }
        return 0;
    }
}
