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
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Queue;

/**
 * 경기중 스코어나 경기 상태를 보여주는 페이지
 */
public class PlayActivity extends AppCompatActivity {

    Button buttonStartGame, buttonEndGame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SensorReceiverService.ACTION_DETECT_SWING);
        registerReceiver(SwingBroadcastReceiver, intentFilter);


        buttonStartGame = findViewById(R.id.buttonStartGame);
        buttonEndGame = findViewById(R.id.buttonEndGame);
        buttonStartGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(mContentView.getContext(), PlayActivity.class);
//                startActivity(intent);
                //서버 모드 실행
                if(GlobalClass.isServer){
                    //API 18 이상 부터는 싱글 쓰레드로 작동하지 않게 만들기 위해서 아래와같은 코드를 넣어줘야한다
                    new ServerAsyncTask(getApplicationContext()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    new ClientAsyncTask(getApplicationContext()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
                else{
                    new ClientAsyncTask(getApplicationContext()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    new ServerAsyncTask(getApplicationContext()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }

            }
        });
//        buttonCounter.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(getApplicationContext(), "counter!", Toast.LENGTH_SHORT).show();
//                new ClientAsyncTask(getApplicationContext()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//            }
//        });
        buttonEndGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //end message 보내기
                GlobalClass.isPlaying = false;
            }
        });
    }

    BroadcastReceiver SwingBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action != null){
                switch (action) {
                    case SensorReceiverService.ACTION_DETECT_SWING:
                        String result = intent.getStringExtra("swingdetect");
                        if(!result.equals("-")) Toast.makeText(context, result , Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }
    };




    /**
     * A simple server socket that accepts connection and writes some data on
     * the stream.
     */
    public static class ServerAsyncTask extends AsyncTask<Void, Void, String> {

        private Context context;
        //private TextView statusText;

        /**
         * @param context
         * //@param statusText
         */
        public ServerAsyncTask(Context context) {
            this.context = context;
            //this.statusText = (TextView) statusText;
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                ServerSocket serverSocket = new ServerSocket(GlobalClass.myPort);
                if(!GlobalClass.isServer) GlobalClass.myIP = serverSocket.getInetAddress().toString().substring(1);
                Log.e(GlobalClass.TAG, "Server: Socket opened - " + GlobalClass.myIP + "/" + GlobalClass.myPort);
                //Toast.makeText(context, "Server: Socket opened", Toast.LENGTH_SHORT).show();

                while(GlobalClass.isPlaying){
                    //연결 요청을 기다린다
                    Socket clientSocket = serverSocket.accept();
                    Log.e(GlobalClass.TAG, "Server: Client address : " + clientSocket.getInetAddress().toString().substring(1));
                    //clientsocket : 통신 작업 수행!!
                    GlobalClass.serverIP = clientSocket.getInetAddress().toString().substring(1);

                    //System.out.println( addr.getHostAddress() + "님이 접속 !!");
                    //Toast.makeText(context, addr.getHostAddress() , Toast.LENGTH_SHORT).show();

                    InputStream is = clientSocket.getInputStream();  // 클라이언트와 연결된 입력 통로. 입력받은 것을 저장시킴.
                    BufferedReader br = new BufferedReader( new InputStreamReader( is ));
                    //readLine 받을 때까지 기다리나?
                    String msg = br.readLine();
                    Log.e(GlobalClass.TAG, "Server: message received - " + msg);

                    br.close();
                    is.close();
                    clientSocket.close();
                }
                serverSocket.close();

                return "server connection end!";
            } catch (IOException e) {
                Log.e(GlobalClass.TAG, e.getMessage());
                return null;
            }
        }

        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
         */
        @Override
        protected void onPostExecute(String result) {
            //if (result != null) {
            //statusText.setText("File copied - " + result);
//                Intent intent = new Intent();
//                intent.setAction(Intent.ACTION_VIEW);
//                intent.setDataAndType(Uri.parse("file://" + result), "image/*");
//                context.startActivity(intent);
            // }

        }

        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#onPreExecute()
         */
        @Override
        protected void onPreExecute() {
            //statusText.setText("Opening a server socket");
        }

    }



    /**
     * A simple server socket that accepts connection and writes some data on
     * the stream.
     */
    public static class ClientAsyncTask extends AsyncTask<Void, Void, String> {

        private Context context;
        //private TextView statusText;

        /**
         * @param context
         * //@param statusText
         */
        public ClientAsyncTask(Context context) {
            this.context = context;
            //this.statusText = (TextView) statusText;
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                //아직 serverIP 에 대한 초기화가 안되면 기다린다.
                while(GlobalClass.isServer && GlobalClass.serverIP.equals(""));
                Socket socket = new Socket(GlobalClass.serverIP, GlobalClass.serverPort);
                Log.e(GlobalClass.TAG, "Client: Socket opened - " + GlobalClass.serverIP + "/" + GlobalClass.serverPort);
                //clientsocket : 통신 작업 수행!!

                OutputStream os = socket.getOutputStream();  // 클라이언트와 연결된 입력 통로. 입력받은 것을 저장시킴.
                PrintWriter pw = new PrintWriter( new OutputStreamWriter( os ));
                //버퍼에 값이 생길 때만 메시지를 전송한다
                //먼저 시작 메시지를 보낸다
                pw.write(Integer.toString(GlobalClass.counter++));
                Log.e(GlobalClass.TAG, "Client: write - " + Integer.toString(GlobalClass.counter-1));
                //readStatus 가  false 가 되면 reading 을 중지한다
                pw.close();
                os.close();
                socket.close();

                return "server connection end!";
            } catch (IOException e) {
                Log.e(GlobalClass.TAG, e.getMessage());
                return null;
            }
        }

        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
         */
        @Override
        protected void onPostExecute(String result) {
            //if (result != null) {
            //statusText.setText("File copied - " + result);
//                Intent intent = new Intent();
//                intent.setAction(Intent.ACTION_VIEW);
//                intent.setDataAndType(Uri.parse("file://" + result), "image/*");
//                context.startActivity(intent);
            // }

        }

        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#onPreExecute()
         */
        @Override
        protected void onPreExecute() {
            //statusText.setText("Opening a server socket");
        }

    }
}
