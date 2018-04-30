package com.github.pocmo.pingpongkim;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.pocmo.pingpongkim.SoundAnalyzer.RecordAudio;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * PlayActivity :
 * 경기중 스코어나 경기 상태를 보여주는 페이지
 */
public class PlayActivity extends AppCompatActivity {

    //뷰
    private Button buttonStartGame, buttonEndGame;
    private TextView textLog;
    private RecordAudio recordTask;
    public static StringBuilder txtLog;
    private ArrayList<String> expectedList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        //뷰 객체화
        buttonStartGame = findViewById(R.id.buttonStartGame);
        buttonEndGame = findViewById(R.id.buttonEndGame);
        textLog = findViewById(R.id.textLog);
        //경기 시작 버튼이 눌렸을 때
        buttonStartGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GlobalClass.isPlaying = true;   //경기 중으로 상태 변경
                //이 디바이스가 서버 역할이면
                if(GlobalClass.isServer){
                    //API 18 이상 부터는 싱글 쓰레드로 작동하지 않게 만들기 위해서 아래와같은 코드를 넣어줘야함
                    new ServerAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);    //서버 모드 쓰레드 실행
                    new ClientAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);    //클라이언트 모드 쓰레드 실행
                }
                else{
                    new ClientAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);    //클라이언트 모드 쓰레드 실행
                    new ServerAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);    //서버 모드 쓰레드 실행
                }

                //오디오 모드 실행
                recordTask = new RecordAudio(getApplicationContext());
                recordTask.execute();
            }
        });
        buttonEndGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GlobalClass.isPlaying = false;
                recordTask.cancel(true);    //오디오 종료
                Toast.makeText(PlayActivity.this, "경기를 종료합니다", Toast.LENGTH_SHORT).show();
            }
        });

        //리시버 등록
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SensorReceiverService.ACTION_DETECT_SWING);
        registerReceiver(SwingBroadcastReceiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(SwingBroadcastReceiver); //리시버 등록 해제
    }

    /**
     * SensorReceiverServer 에서 브로드캐스트하는 메시지를 받음
     */
    BroadcastReceiver SwingBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action != null){
                switch (action) {
                    case SensorReceiverService.ACTION_DETECT_SWING:
                        String result = intent.getStringExtra("swingdetect");
                        if(!result.equals("-")) {
                            Toast.makeText(context, result , Toast.LENGTH_SHORT).show();
                            //처음인데 서브 스윙이라고 판단이 될 때 A로 설정
                            if(GlobalClass.isFirst){
                                initialize("A", true);
                            }
                        }
                        break;
                }
            }
        }
    };

    /**
     * 경기 처음일 때 설정
     */
    static void initialize(String type, boolean isServe){
        GlobalClass.isFirst = false;
        GlobalClass.isServe = isServe; //현재 서브임
        GlobalClass.playerType = type;   //플레이어 타입 설정
        //A 가 기대하는 값 리스트 설정
        GlobalClass.expectedList.add(GlobalClass.playerType + "TABLE");
        GlobalClass.expectedList.add(GlobalClass.playerType + "SWING");
    }

    /**
     * 서버 역할의 쓰레드, 메시지 리시버 역할 담당
     */
    public static class ServerAsyncTask extends AsyncTask<Void, Void, Void> {

        public ServerAsyncTask() {}

        @Override
        protected Void doInBackground(Void... params) {
            try {
                //내 포트 번호로 소켓 엶
                ServerSocket serverSocket = new ServerSocket(GlobalClass.myPort);
                if(!GlobalClass.isServer) GlobalClass.myIP = serverSocket.getInetAddress().toString().substring(1); // 스트링에 / 가 들어있어서 자름
                Log.e(GlobalClass.TAG, "Server: Socket opened - " + GlobalClass.myIP + "/" + GlobalClass.myPort);

                //경기 중
                while(GlobalClass.isPlaying){
                    Socket clientSocket = serverSocket.accept();    //클라이언트 요청 대기
                    Log.e(GlobalClass.TAG, "Server: Client address : " + clientSocket.getInetAddress().toString().substring(1));
                    if(GlobalClass.serverIP.equals("")) GlobalClass.serverIP = clientSocket.getInetAddress().toString().substring(1);

                    //메시지 읽음
                    InputStream is = clientSocket.getInputStream();
                    BufferedReader br = new BufferedReader( new InputStreamReader( is ));
                    String msg = br.readLine();
                    Log.e(GlobalClass.TAG, "Server: message received - " + msg);
                    //처음인데 메시지 받으면 B 이므로 B 로 설정
                    if(GlobalClass.isFirst){
                        initialize("B", false);
                    }

                    //리소스 반환
                    br.close();
                    is.close();
                    clientSocket.close();
                }
                serverSocket.close();

            } catch (IOException e) {
                Log.e(GlobalClass.TAG, e.getMessage());
            }
            return null;
        }
    }

    /**
     * 클라이언트 역할의 쓰레드, 메시지 전송기능 담당
     */
    public static class ClientAsyncTask extends AsyncTask<Void, Void, Void> {

        public ClientAsyncTask() {}

        @Override
        protected Void doInBackground(Void... params) {
            try {
                //아직 serverIP 에 대한 초기화가 안되면 기다린다.
                while(GlobalClass.isServer && GlobalClass.serverIP.equals(""));
                Socket socket = new Socket(GlobalClass.serverIP, GlobalClass.serverPort);
                Log.e(GlobalClass.TAG, "Client: Socket opened - " + GlobalClass.serverIP + "/" + GlobalClass.serverPort);

                OutputStream os = socket.getOutputStream();
                PrintWriter pw = new PrintWriter( new OutputStreamWriter( os ));
                pw.write(Integer.toString(GlobalClass.counter++));
                Log.e(GlobalClass.TAG, "Client: write - " + Integer.toString(GlobalClass.counter-1));
                //

                //리소스 반환
                pw.close();
                os.close();
                socket.close();
            } catch (IOException e) {
                Log.e(GlobalClass.TAG, e.getMessage());
            }
            return null;
        }
    }
}
