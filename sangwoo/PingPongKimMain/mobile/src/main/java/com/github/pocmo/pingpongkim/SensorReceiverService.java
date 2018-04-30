package com.github.pocmo.pingpongkim;

import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.github.pocmo.pingpongkim.shared.DataMapKeys;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.Arrays;

/**
 * 센서에서 정보를 얻어와서 처리하는 부분
 */
public class SensorReceiverService extends WearableListenerService {
    public static final String ACTION_RECEIVE_SENSOR_DATA = "sensorservice.sensordata";
    public static final String ACTION_DETECT_SWING = "sensorservice.detectswing";
    public static final String ACTION_TUTORIAL_CALB = "sensorservice.tutorialcalb";
    public static final String ACTION_START_PLAY = "sensorservice.startplay";

    //현재 경기 중인지 관리하는 변수
    boolean isPlaying = false;
    //현재 튜토리얼 중인지 관리하는 변수
    boolean isTutorial = true;
    private int tutorialCount = 0; //튜토리얼 스윙 횟수 (최대 5회)

    //이전 중력센서값과 현재 중력센서값의 평균
    double prev_gravity_z = 9;

    //스윙이 포핸드인지 백핸드인지 판별하는 변수
    boolean isHorizontalSwing = true;
    boolean isBackSwing = false;

    //원격 센서 매니저
    private RemoteSensorManager sensorManager;

    @Override
    public void onCreate() {
        super.onCreate();
        sensorManager = RemoteSensorManager.getInstance(this);
    }

    @Override
    public void onPeerConnected(Node peer) {
        super.onPeerConnected(peer);
        Log.i(GlobalClass.TAG, "Connected: " + peer.getDisplayName() + " (" + peer.getId() + ")");
    }

    @Override
    public void onPeerDisconnected(Node peer) {
        super.onPeerDisconnected(peer);
        Log.i(GlobalClass.TAG, "Disconnected: " + peer.getDisplayName() + " (" + peer.getId() + ")");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isPlaying = false;
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.d(GlobalClass.TAG, "onDataChanged()");
        //튜토리얼도 아니고 경기중도 아닌데 데이터가 들어오기 시작하면 경기중으로 간주한다
        if(!isTutorial && !isPlaying){
            isPlaying = true;
            //메인액티비티2로 브로드캐스트를 날려서 PlayActivity를 실행하게 만든다
            Intent intent = new Intent();
            intent.setAction(ACTION_START_PLAY);
            sendBroadcast(intent);
        }

        //데이터 이벤트에 대해서 데이터를 파싱한다
        for (DataEvent dataEvent : dataEvents) {
            if (dataEvent.getType() == DataEvent.TYPE_CHANGED) {
                DataItem dataItem = dataEvent.getDataItem();
                Uri uri = dataItem.getUri();
                String path = uri.getPath();

                if (path.startsWith("/sensors/")) {
                    //파싱 함수
                    unpackSensorData(
                        Integer.parseInt(uri.getLastPathSegment()),
                        DataMapItem.fromDataItem(dataItem).getDataMap()
                    );
                }
            }
        }
    }

    /**
     *
     * @param sensorType
     * @param dataMap
     */
    private void unpackSensorData(int sensorType, DataMap dataMap) {
        int accuracy = dataMap.getInt(DataMapKeys.ACCURACY);
        long timestamp = dataMap.getLong(DataMapKeys.TIMESTAMP);
        float[] values = dataMap.getFloatArray(DataMapKeys.VALUES);

        //signal vector magnitude 를 저장하는 리스트
        float[] svm_values = new float[1];

        //Log.d(GlobalClass.TAG, "Received sensor data " + sensorType + " = " + Arrays.toString(values));

        Log.e(GlobalClass.TAG, "isHorizontal : " + Boolean.toString(isHorizontalSwing) + " isBackSwing : " + Boolean.toString(isBackSwing));

        //sensorType 1 은 가속도
        if(sensorType == 1){
            //필요한 정보보고 싶을 때 주석해제하기
//            String s = Integer.toString(accuracy) + " / " + Long.toString(timestamp) + " / " + '\n'
//                    + Float.toString(values[0]) + " , " + Float.toString(values[1]) + " , " + Float.toString(values[2]);
//            Intent intent = new Intent(ACTION_RECEIVE_SENSOR_DATA);
//            intent.putExtra("sensordata", s);
//            sendBroadcast(intent);

            //핵심 알고리즘 : 가속도와 중력센서 데이터에 기반한 스윙 동작 계산
            Intent intent = new Intent(ACTION_DETECT_SWING);
            double result = checkIsSwing(values);
            //String msg = result > 0 ? "SWING" : "-";

            String msg = "";
            if(result > 0){
                if(isHorizontalSwing)msg = "SWING";
                else if(isBackSwing) msg = "BACK SWING";
                else msg = "NONE";
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

                //튜토리얼일 때는 broadcast 를 뿌려준다
                if(isTutorial && (msg.equals("SWING") || msg.equals("BACK SWING"))){
                    tutorialCount++;
                    Log.e(GlobalClass.TAG, Integer.toString(tutorialCount) );
                    if(tutorialCount <= 5){
                        Intent tutorial_intent = new Intent();
                        intent.setAction(ACTION_TUTORIAL_CALB);
                        sendBroadcast(tutorial_intent);
                        if(tutorialCount == 5) isTutorial = false;
                    }
                }
            }
            else{
                msg = "-";
            }
            if(!msg.equals("-")){
                intent.putExtra("sensordata", result);
                intent.putExtra("swingdetect", msg);
                intent.putExtra("timestamp", timestamp);
                sendBroadcast(intent);
            }
            //swing 이 아니면 0 을 넣고 swing 이면 svm 값을 넣어서 그래프를 하나만 찍도록 한다
            if(msg.equals("-") || msg.equals("NONE"))
                svm_values[0] = 0;
            else {
                svm_values[0] = (float) (Math.pow(values[0], 2) + Math.pow(values[1], 2));
            }
            values = svm_values;
        }
        //gravity 센서
        else if(sensorType == 9){
            //TODO : 일정 interval의 데이터를 구한다
            double gravity_z_mean = (prev_gravity_z + values[2])/2.0;
            prev_gravity_z = values[2];
            Log.e(GlobalClass.TAG, "Z_GRAVITY MEAN -> " + Double.toString(gravity_z_mean));
            Log.e(GlobalClass.TAG, "Z_GRAVITY CURRENT -> " + Double.toString(values[2]));

            //펜홀더의 경우 horizontal 인 경우 대체로 8 이상의 값은 나온다 평균을 냈을 때 8 이상이라고 하면
            if(gravity_z_mean >= 7){
                isHorizontalSwing = true;
                isBackSwing = false;
            }
            //펜홀더의 경우 vertical 인 경우 대체로 -0 ~ -8 사이의 값이 나옴
            else if(gravity_z_mean >= -7 && gravity_z_mean <= 0){
                isHorizontalSwing = false;
                isBackSwing = false;
            }
            //펜홀더의 경우 back swing 인 경우 대체로 -8 ~ -15 사이의 값이 나옴
            else if(gravity_z_mean <= -8){
                isHorizontalSwing = false;
                isBackSwing = true;
            }
        }
        sensorManager.addSensorData(sensorType, accuracy, timestamp, values);
    }


    //swing detect algorithm
    boolean isSkipping = false;
    int skipCount = 0;
    final int baseThreshold = 200;
    final int skipInterval = 20;
    /**
     * 핵심 알고리즘
     * 입력 데이터에 signal vector magnitude를 저장해서 그 값을 반환해준다
     * @param values = [가속도 x, 가속도 y, 가속도 z]
     * @return
     */
    double checkIsSwing(float[] values){
        Log.e(GlobalClass.TAG, "skip count : " + Integer.toString(skipCount) + " isSkipping : " + Boolean.toString(isSkipping));

        //x와 y에 대해서만 svm 을 적용한다
        //z를 넣지 않는 이유는 피크가 스윙과 무관하게 발생해서 무시하기 위함
        //15미만이 정상적인 값임. 원래 가속도 값은 10보다 작은 값을 가진다
        if(values[0] < 15 && values[1] < 15) {
            //제곱
            double svmVal = Math.pow(values[0], 2) + Math.pow(values[1], 2) + Math.pow(values[2], 2);
            Log.e(GlobalClass.TAG, "SVM_VALUE -> " + Double.toString(svmVal));

            //추가적인 N개의 데이터를 무시하는 구간에 있는지 확인
            //무시하는 구간에 있으면
            if (isSkipping) {
                skipCount++;
                if (skipCount > skipInterval) {
                    //무시하는 구간을 벗어난다
                    skipCount = 0;
                    isSkipping = false;
                }
            }
            //무시하는 구간에 있지 않는 경우
            else {
                //일정 threshold를 넘으면 무시하는 구간으로 들어감
                //TODO : Adaptive Thresholding을 적용해야한다
                if (svmVal > baseThreshold) {
                    isSkipping = true;
                    skipCount++;
                    return svmVal;
                }
            }
        }
        return 0;
    }
}
