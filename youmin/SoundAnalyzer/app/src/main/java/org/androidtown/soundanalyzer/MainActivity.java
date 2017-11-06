package org.androidtown.soundanalyzer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 소리를 실시간으로 듣는 기능의 코드입니다
 * 들은 소리를 pcm파일로 저장하게 됩니다.
 *
 */

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "SEBA";

    // sample rate : 1초에 측정되는 각 채널의 오디오 샘플의 갯수
    // render 또는 produce 하는 sample rate 과는 다른 개념이다
    // 일반적으로 44100 과 22050 을 많이 사용한다
    private static final int RECORDER_SAMPLERATE = 44100; //8000, 11025, 16000, 22050, (44100??) work

    // 채널의 갯수를 정한다
    // channel count : 1 ~ 8
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;

    // AudioFormat.ENCODING_PCM_FLOAT : 32bit float 는 더 정확(float)
    // AudioFormat.ENCODING_PCM_16BIT : 16bit는 signed 형식 (short)
    // AudioFormat.ENCODING_PCM_8BIT : 8bit 는 0 ~ 255 (unsigned int)
    // 인코딩 모드를 결정한다
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private AudioRecord recorder = null;
    private Thread recordingThread = null;
    private boolean isRecording = false;
    int bufferSize = RECORDER_SAMPLERATE * 2 * 5;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //오디오 권한 허가
        requestRecordAudioPermission();

        setButtonHandlers();
        enableButtons(false);


        //Returns the minimum buffer size required for the successful creation of an AudioRecord object
        //최소의 값 * 2 만큼 확보

        //bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE,RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING);
        //Galaxy Note5 찍어보니까 3840
        Log.e(TAG, Integer.toString(bufferSize));
        getValidSampleRates();
    }

    private void setButtonHandlers() {
        ((Button) findViewById(R.id.btnStart)).setOnClickListener(btnClick);
        ((Button) findViewById(R.id.btnStop)).setOnClickListener(btnClick);
    }

    private void enableButton(int id, boolean isEnable) {
        ((Button) findViewById(id)).setEnabled(isEnable);
    }

    private void enableButtons(boolean isRecording) {
        enableButton(R.id.btnStart, !isRecording);
        enableButton(R.id.btnStop, isRecording);
    }


    //이게 뭐임??
    //int BufferElements2Rec = 1024; // want to play 2048 (2K) since 2 bytes we use only 1024
    //int BytesPerElement = 2; // 2 bytes in 16bit format

    private void startRecording() {
        recorder = new AudioRecord.Builder()
                .setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION)
                .setAudioFormat(new AudioFormat.Builder()
                        //16bit
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        //sample rate : 출력 sample rate ?????
                        .setSampleRate(RECORDER_SAMPLERATE)
                        .setChannelMask(AudioFormat.CHANNEL_IN_MONO)
                        .build())
                .setBufferSizeInBytes(bufferSize)
                .build();
        // Multiply by 1 or 2 or 3 in order this code to work for all sample rates

        recorder.startRecording();
        isRecording = true;
        recordingThread = new Thread(new Runnable() {
            public void run() {
                writeAudioDataToFile();
            }
        }, "AudioRecorder Thread");
        recordingThread.start();
    }

    //convert short to byte
    private byte[] short2byte(short[] sData) {
        int shortArrsize = sData.length;
        byte[] bytes = new byte[shortArrsize * 2];
        for (int i = 0; i < shortArrsize; i++) {
            bytes[i * 2] = (byte) (sData[i] & 0x00FF);
            bytes[(i * 2) + 1] = (byte) (sData[i] >> 8);
            sData[i] = 0;
        }
        return bytes;

    }

    private void writeAudioDataToFile() {
        // Write the output audio in byte
//        String filePath =  Environment.getExternalStorageDirectory().getPath()+"/wav_voice"+RECORDER_SAMPLERATE+"16bitmono.pcm";
        String filePath = "/sdcard/voice8K16bitmono.pcm";
        //String filePath_txt = "/sdcard/voice8K16bitmono.txt";
        Log.d(TAG, "path: " + filePath);
        short sData[] = new short[bufferSize];

        FileOutputStream os = null;
        try {
            os = new FileOutputStream(filePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        while (isRecording) {
            // gets the voice output from microphone to byte format

            recorder.read(sData, 0, bufferSize);
            System.out.println("Short wirting to file" + sData.toString());
            try {
                 // writes the data to file from buffer
                 // stores the voice buffer
                byte bData[] = short2byte(sData);
                Log.e(TAG, "b data length : " + Integer.toString(bData.length));
                os.write(bData, 0, bufferSize);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopRecording() {
        // stops the recording activity
        if (null != recorder) {
            isRecording = false;
            recorder.stop();
            recorder.release();
            recorder = null;
            recordingThread = null;
        }
    }

    private View.OnClickListener btnClick = new View.OnClickListener() {
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btnStart: {
                    enableButtons(true);
                    startRecording();
                    break;
                }
                case R.id.btnStop: {
                    enableButtons(false);
                    stopRecording();
                    break;
                }
            }
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    /*
    이 디바이스에서 사용가능한 sample rate 을 찍어줌
     */
    public void getValidSampleRates() {
        for (int rate : new int[]{8000, 11025, 16000, 22050, 44100}) {  // add the rates you wish to check against
            int bufferSize = AudioRecord.getMinBufferSize(rate, AudioFormat.CHANNEL_CONFIGURATION_DEFAULT, AudioFormat.ENCODING_PCM_16BIT);
            if (bufferSize > 0) {
                // buffer size is valid, Sample rate supported
                Log.d(TAG, "valid sample rate" + rate);
            }
        }
    }

    private void requestRecordAudioPermission() {
        //check API version, do nothing if API version < 23!
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion > android.os.Build.VERSION_CODES.LOLLIPOP){

            //오디오 퍼미션
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {

                    // Show an expanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.

                } else {

                    // No explanation needed, we can request the permission.

                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
                }
            }

            //sd 카드 퍼미션
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)){

                }
                else{
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Log.d("Activity", "Granted!");

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Log.d("Activity", "Denied!");
                    finish();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}