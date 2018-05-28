package com.github.pocmo.pingpongkim;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.pocmo.pingpongkim.SoundAnalyzer.RecordAudio;

import org.w3c.dom.Text;

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
import java.util.Timer;
import java.util.TimerTask;

import javax.microedition.khronos.opengles.GL;

/**
 * PlayActivity :
 * 경기중 스코어나 경기 상태를 보여주는 페이지
 */
public class PlayActivity extends AppCompatActivity {

    //뷰
//    private Button buttonEndGame;
    static private TextView textLog;
    private RecordAudio recordTask;
    //public static StringBuilder txtLog;
    private TextView score1, score2;

    //게임 시간 체크
    private TimerTask second;
    private final Handler handler = new Handler();
    private int timer_sec = 0;
    private int count = 0;
    private int pointA = 0;
    private int pointB = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        //뷰 객체화
//        buttonEndGame = findViewById(R.id.buttonEndGame);
        //textLog = findViewById(R.id.textLog);
        score1 = (TextView)findViewById(R.id.score_p1);
        score2 = (TextView)findViewById(R.id.score_p2);
        //txtLog = new StringBuilder();

        testStart();
        //스윙 데이터 측정하기 시작하면 isPlaying = true 로 변경
        //GlobalClass.isPlaying = true;   //경기 중으로 상태 변경
        //이 디바이스가 서버 역할이면
        if(GlobalClass.isServer){
            //API 18 이상 부터는 싱글 쓰레드로 작동하지 않게 만들기 위해서 아래와같은 코드를 넣어줘야함
            new ServerAsyncTask(getApplicationContext()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);    //서버 모드 쓰레드 실행
            new ClientAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);    //클라이언트 모드 쓰레드 실행
        }
        else{
            new ClientAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);    //클라이언트 모드 쓰레드 실행
            new ServerAsyncTask(getApplicationContext()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);    //서버 모드 쓰레드 실행
        }
        //오디오 모드 실행
        recordTask = new RecordAudio(getApplicationContext());
        recordTask.execute();

//        buttonEndGame.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                GlobalClass.isPlaying = false;
//                recordTask.cancel(true);    //오디오 종료
//                Toast.makeText(PlayActivity.this, "경기를 종료합니다", Toast.LENGTH_SHORT).show();
//                finish();
//            }
//        });

        //리시버 등록
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SensorReceiverService.ACTION_DETECT_SWING);
        intentFilter.addAction(RecordAudio.ACTION_BALL_DETECTION);
        registerReceiver(SwingBallBroadcastReceiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(SwingBallBroadcastReceiver); //리시버 등록 해제
        deinitialize(); //경기시작할 때 설정했던 것 초기화
        //recordTask.cancel(true);
    }

    /**
     * SensorReceiverServer 에서 브로드캐스트하는 메시지를 받음
     */
    BroadcastReceiver SwingBallBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action != null){
                switch (action) {
                    case SensorReceiverService.ACTION_DETECT_SWING:
                        String result = intent.getStringExtra("swingdetect");
                        Log.e(GlobalClass.TAG, "서비스에서 스윙을 받았습니다");
                        if(!result.equals("-")) {
                            Toast.makeText(context, result , Toast.LENGTH_SHORT).show();
                            //처음인데 서브 스윙이라고 판단이 될 때 A로 설정
                            if(GlobalClass.isFirst) {
                                initialize("A", true);
                            }
                            //스윙이 감지되면 상대방에게 메시지 전송
                            Log.e(GlobalClass.TAG, "스윙메시지 전송");
                            new ClientAsyncTask(GlobalClass.playerType + "SWING").executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        }
                        break;
                    case RecordAudio.ACTION_BALL_DETECTION:
                        Log.e(GlobalClass.TAG, "공 메시지 전송");
                        new ClientAsyncTask(GlobalClass.playerType + "TABLE").executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        break;
                }
            }
        }
    };

    /**
     * 경기 처음일 때 설정
     */
    static void initialize(String type, boolean isServe){
        Log.e("PLAYING", "initialize " + type);
        GlobalClass.isFirst = false;
        GlobalClass.isServe = isServe; //현재 서브임
        GlobalClass.playerType = type;   //플레이어 타입 설정
        //초기화할 때 B type 이면 expected list를 서브용으로 설정
        GlobalClass.expectedList = new ArrayList<>();
        if(GlobalClass.playerType.equals("B"))  {
            //TODO : 여기 index + 1 로만 바꿔서 같은 기능할 수 있음
            setExpectedList("B");
            GlobalClass.expectingIndex = 1;
        }
        //초기화할 때 A type 이면 expected list 를 기본으로 설정
        else setExpectedList("A");
    }

    static void deinitialize(){
        Log.e("PLAYING", "deinitialize " + GlobalClass.playerType);
        GlobalClass.isFirst = true;
        GlobalClass.isServe = false; //현재 서브임
        GlobalClass.playerType = "";   //플레이어 타입 설정
        GlobalClass.nowExpecting = "";
        GlobalClass.expectingIndex = 0;
        if(GlobalClass.expectedList != null) GlobalClass.expectedList.clear();
    }


    static void setExpectedList(String type){
        if(!GlobalClass.expectedList.isEmpty()) GlobalClass.expectedList.clear();
        switch (type){
            case "A":
                GlobalClass.expectedList.add("BTABLE");
                GlobalClass.expectedList.add("BSWING");
                break;
            case "B":
                GlobalClass.expectedList.add("ATABLE");
                GlobalClass.expectedList.add("ASWING");
                break;
        }
    }

    /**
     * 서버 역할의 쓰레드, 메시지 리시버 역할 담당
     */
    public static class ServerAsyncTask extends AsyncTask<Void, Void, Void> {

        Context context;
        public ServerAsyncTask() {}
        public ServerAsyncTask(Context context) {
            this.context = context;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                //내 포트 번호로 소켓 엶
                ServerSocket serverSocket = new ServerSocket(GlobalClass.myPort);
                serverSocket.setReuseAddress(true);
                if(!GlobalClass.isServer) GlobalClass.myIP = serverSocket.getInetAddress().toString().substring(1); // 스트링에 / 가 들어있어서 자름
                Log.e(GlobalClass.TAG, "Server: Socket opened - " + GlobalClass.myIP + "/" + GlobalClass.myPort);

                //경기 중
                while(GlobalClass.isPlaying){
                    Socket clientSocket = serverSocket.accept();    //클라이언트 요청 대기

                    //처음 handshake 일 때
                    if(GlobalClass.isFirstNoti){
                        GlobalClass.isFirstNoti = false;
                        Log.e(GlobalClass.TAG, "Server: Client address : " + clientSocket.getInetAddress().toString().substring(1));
                        if(GlobalClass.serverIP.equals("")) GlobalClass.serverIP = clientSocket.getInetAddress().toString().substring(1);
                    }
                    else{
                        //메시지 읽음
                        InputStream is = clientSocket.getInputStream();
                        BufferedReader br = new BufferedReader( new InputStreamReader( is ));
                        String msg = br.readLine();
                        Log.e(GlobalClass.TAG, "Server: message received - " + msg);
                        //처음인데 메시지 받으면 B 이므로 B 로 설정
                        if(GlobalClass.isFirst){
                            initialize("B", false);
                        }
                        //서브일 때 처음 바로 다음에 오는 메시지에 대해서 앞으로 진행될 Expected list 를 설정한다
                        if(!GlobalClass.isFirst && GlobalClass.isServing){
                            GlobalClass.isServing = false;
                        }

                        if(msg == null) continue;

                        //now expecting 업데이트
                        GlobalClass.nowExpecting = GlobalClass.expectedList.get((GlobalClass.expectingIndex++)%2);

                        //기대하는 결과와 일치하는지 확인한다
                        if(msg.equals(GlobalClass.nowExpecting)) {
                            Log.e("PLAYING", "expecting OK");
                            msg += "expecting OK";
                        }
                        else {
                            Log.e("PLAYING", "expecting NOT OK");
                            msg += "expecting NOT OK";
                            GlobalClass.isPlaying = false;
                            msg += "\nGAME OVER";
                        }
//                        txtLog.append(msg + "\n");
//                        new Handler(context.getMainLooper()).post(new Runnable() {
//                            @Override
//                            public void run() {
//                                textLog.setText(txtLog);
//                            }
//                        });
                        //리소스 반환
                        br.close();
                        is.close();
                    }
                    //연결 종료
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
        String msg = "";
        public ClientAsyncTask() {}
        public ClientAsyncTask(String strMsg) {
            Log.e(GlobalClass.TAG, strMsg);
            msg = strMsg;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                //아직 serverIP 에 대한 초기화가 안되면 기다린다.
                Log.e(GlobalClass.TAG, "doInBackground");
                while(GlobalClass.isServer && GlobalClass.serverIP.equals(""));
                Socket socket = new Socket(GlobalClass.serverIP, GlobalClass.serverPort);
                Log.e(GlobalClass.TAG, "Client: Socket opened - " + GlobalClass.serverIP + "/" + GlobalClass.serverPort);

                OutputStream os = socket.getOutputStream();
                PrintWriter pw = new PrintWriter( new OutputStreamWriter( os ));
                pw.write(msg);
                Log.e(GlobalClass.TAG, "Client : write - " + msg);

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

    final int[] times = {23100, 33200, 43000, 54100, 65300, 71000};
    final String [][] scores = {{"1", "0"}, {"2", "0"}, {"2", "1"}, {"2","2"}, {"3","2"}};

    public void testStart(){
//        second = new TimerTask() {
//            @Override
//            public void run() {
//                Update();
//            }
//        };
//        Timer timer = new Timer();
//        timer.schedule(second, 0, 5000);
        final Handler mHandler = new Handler();
        Runnable[] mRunnable = new Runnable[6];
        int size = times.length;
        mRunnable[0] = new Runnable() {
            @Override
            public void run() {
                score2.setText(scores[0][0]);
                score1.setText(scores[0][1]);
            }
        };
        mRunnable[1] = new Runnable() {
            @Override
            public void run() {
                score2.setText(scores[1][0]);
                score1.setText(scores[1][1]);
            }
        };
        mRunnable[2] = new Runnable() {
            @Override
            public void run() {
                score2.setText(scores[2][0]);
                score1.setText(scores[2][1]);
            }
        };
        mRunnable[3] = new Runnable() {
            @Override
            public void run() {
                score2.setText(scores[3][0]);
                score1.setText(scores[3][1]);

            }
        };
        mRunnable[4] = new Runnable() {
            @Override
            public void run() {
                score2.setText(scores[4][0]);
                score1.setText(scores[4][1]);
            }
        };

        mRunnable[5] = new Runnable() {
            @Override
            public void run() {
                GlobalClass.isGameOver = true;
                Toast.makeText(PlayActivity.this, "경기가 종료되었습니다!", Toast.LENGTH_SHORT).show();
                finish();
            }
        };


        for (int i = 0 ; i < size; i++){
            mHandler.postDelayed(mRunnable[i], times[i]);
        }
    }

//    protected void Update(){
//        Runnable updater = new Runnable() {
//            @Override
//            public void run() {
//                if(count == 1) {
//                    score1.setText("1");
//                }
//                else if(count == 2){
//                    score1.setText("2");
//                }
//                else if(count == 3){
//                    score2.setText("1");
//                }
//                else if(count == 4){
//                    score1.setText("3");
//                    GlobalClass.isGameOver = true;
//                    new Handler().postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            Toast.makeText(PlayActivity.this, "경기가 종료되었습니다!", Toast.LENGTH_SHORT).show();
//                            finish();
//                        }
//                    }, 2000);
//                }
//                count++;
//            }
//        };
//        handler.post(updater);
//    }
}
