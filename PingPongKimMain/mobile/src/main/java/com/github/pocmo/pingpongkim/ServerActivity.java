package com.github.pocmo.pingpongkim;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ServerActivity extends AppCompatActivity {

    public static String TAG = "PingPongBoy/SA";

    TextView textViewServerIp;
    TextView textViewLog;

    //서버 세팅
    //서버세팅
    private ServerSocket serverSocket;
    private Socket socket;
    private String msg;
    private Map<String, DataOutputStream> clientsMap = new HashMap<String, DataOutputStream>();


    //서버가 데이터 받을 때 실행되는 핸들러
    private static final int SERVER_TEXT_UPDATE = 100;
    private static final int CLIENT_TEXT_UPDATE = 200;

    private StringBuilder serverMsg = new StringBuilder();
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msgg) {
            super.handleMessage(msgg);
            switch (msgg.what){
                case SERVER_TEXT_UPDATE:
                    serverMsg.append(msg+"\n");
                    textViewLog.setText(serverMsg.toString());
                    break;
                case CLIENT_TEXT_UPDATE:
                    //client message builder 는 필요없다. 서버만 데이터를 받기 때문에
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);

        textViewServerIp = (TextView)findViewById(R.id.textViewServerIp);
        textViewLog = (TextView)findViewById(R.id.textLog);

        //서버 IP 주소를 등록한다
        textViewServerIp.setText(getLocalIpAddress() + ":7777");
        //서버를 생성한다
        serverCreate();

        //이건 클라이언트용이잖아
//        Intent intent = getIntent();
//        textViewServerIp.setText(intent.getStringExtra("server_ip"));
    }

    public String getLocalIpAddress() {
        WifiManager wifiMgr = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
        int ip = wifiInfo.getIpAddress();
        String ipAddress = String.format("%d.%d.%d.%d"
                , (ip & 0xff)
                , (ip >> 8 & 0xff)
                , (ip >> 16 & 0xff)
                , (ip >> 24 & 0xff));
        return ipAddress;
    }


    public void serverCreate() {
        Collections.synchronizedMap(clientsMap);
        try {
            //7777 번 포트로 서버를 실행한다
            Toast.makeText(getApplicationContext(), "서버를 시작합니다", Toast.LENGTH_SHORT).show();
            serverSocket = new ServerSocket(7777);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        /** XXX 01. 첫번째. 서버가 할일 분담. 계속 접속받는것. */
                        Log.e(TAG, "서버 대기중...");
                        try {
                            socket = serverSocket.accept();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Log.e(TAG, socket.getInetAddress() + "에서 접속했습니다.");
                        msg = socket.getInetAddress() + "에서 접속했습니다.\n";
                        handler.sendEmptyMessage(SERVER_TEXT_UPDATE);

                        new Thread(new Runnable() {
                            private DataInputStream in;
                            private DataOutputStream out;
                            private String nick;

                            @Override
                            public void run() {

                                try {
                                    out = new DataOutputStream(socket.getOutputStream());
                                    in = new DataInputStream(socket.getInputStream());
                                    nick = in.readUTF();
                                    addClient(nick, out);

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                try {// 계속 듣기만!!
                                    while (in != null) {
                                        msg = in.readUTF();

                                        // 클라이언트한테 뿌려주는 것 같은데 필요없다
                                        //sendMessage(msg);
                                        handler.sendEmptyMessage(SERVER_TEXT_UPDATE);
                                    }
                                } catch (IOException e) {
                                    // 사용접속종료시 여기서 에러 발생. 그럼나간거에요.. 여기서 리무브 클라이언트 처리 해줍니다.
                                    removeClient(nick);
                                }
                            }
                        }).start();
                    }
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addClient(String nick, DataOutputStream out) throws IOException {
        Toast.makeText(getApplicationContext(), nick+"님이 입장하셨습니다", Toast.LENGTH_SHORT).show();
        sendMessage(nick + "님이 접속하셨습니다.");
        clientsMap.put(nick, out);
    }

    public void removeClient(String nick) {
        Toast.makeText(getApplicationContext(), nick+"님이 나가셨습니다", Toast.LENGTH_SHORT).show();
        sendMessage(nick + "님이 나가셨습니다.");
        clientsMap.remove(nick);
    }

    // 메시지 내용 전파
    public void sendMessage(String msg) {
        Iterator<String> it = clientsMap.keySet().iterator();
        String key = "";
        while (it.hasNext()) {
            key = it.next();
            try {
                clientsMap.get(key).writeUTF(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



}
