package com.yuva.wifihotspotsharing;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;

public class MessageActivity extends Activity implements View.OnClickListener {

    static final int SocketServerPORT = 8080;
    private static final int SHARE_PICTURE = 2;
    private static final int REQUEST_PATH = 1;
    private static final int TIMEOUT = 3000;
    private static final int MAXFILELEN = 65000;

    final int portNum = 3238;
    InetAddress ip = null;
    NetworkInterface networkInterface = null;
    ServerSocket serverSocket;
    String curFileName;
    EditText edittext;
    private ArrayList<String> recQue;
    private String[] values;
    private ListView listView;
    private ArrayAdapter adapter;
    private MulticastSocket socket;
    private InetAddress group;
    private MulticastSocket fileSocket;
    private InetAddress fileGroup;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        username = (String) getIntent().getExtras().get("name");
        TextView userNm = (TextView) findViewById(R.id.usrName);
        userNm.setText(username);

        listView = (ListView) findViewById(R.id.listView);

        WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if (wifi != null) {
            WifiManager.MulticastLock lock =
                    wifi.createMulticastLock("cSharing");
            lock.setReferenceCounted(true);
            lock.acquire();
        } else {
            Log.e("cSharing", "Unable to acquire multicast lock");
            Toast.makeText(getApplicationContext(), "Unable to acquire multicast lock", Toast.LENGTH_SHORT).show();

            finish();
        }
        recQue = new ArrayList<>();

        try {
            if (socket == null) {


                Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface.getNetworkInterfaces();
                while (enumNetworkInterfaces.hasMoreElements()) {

                    networkInterface = enumNetworkInterfaces.nextElement();
                    Enumeration<InetAddress> enumInetAddress = networkInterface.getInetAddresses();

                    while (enumInetAddress.hasMoreElements()) {
                        InetAddress inetAddress = enumInetAddress.nextElement();

                        if (inetAddress.isSiteLocalAddress()) {
                            ip = inetAddress;
                            break;
                        }
                    }
                    if (ip != null) {
                        break;
                    }
                }
                socket = new MulticastSocket(portNum);
                socket.setInterface(ip);
                socket.setBroadcast(true);

                group = InetAddress.getByName("224.0.0.1");
                socket.joinGroup(new InetSocketAddress(group, portNum), networkInterface);

                fileSocket = new MulticastSocket(portNum+1);
                fileSocket.setInterface(ip);
                fileSocket.setBroadcast(true);

                fileGroup = InetAddress.getByName("224.0.0.2");
                fileSocket.joinGroup(new InetSocketAddress(fileGroup, (portNum+1)), networkInterface);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        receiverMessage recvMsgThread = new receiverMessage(recQue);
        recvMsgThread.execute((Void) null);
        Button send = (Button) findViewById(R.id.buttonSend);
        send.setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        try {
            socket = new MulticastSocket(portNum);
            socket.setInterface(ip);
            socket.setBroadcast(true);

            group = InetAddress.getByName("224.0.0.1");
            socket.joinGroup(new InetSocketAddress(group, portNum), networkInterface);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.buttonSend:
                EditText text = (EditText) findViewById(R.id.editText2);
                String textMsg = text.getText().toString();
                text.setText("");
                sendMessage sendMessage = new sendMessage(textMsg);
                sendMessage.execute((Void) null);
                break;
        }
    }

    private String getIpAddress() {
        String ip = "";
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {

                NetworkInterface networkInterface = enumNetworkInterfaces.nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface.getInetAddresses();

                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress.nextElement();

                    if (inetAddress.isSiteLocalAddress()) {
                        ip += inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
            ip += "Something Wrong! " + e.toString() + "\n";
        }
        return ip;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // See which child activity is calling us back.
        //이따 이거 한 번 없애보자
    }

    private class receiverMessage extends AsyncTask<Void, Void, Boolean> {
        ArrayList<String> msgList;

        receiverMessage(ArrayList<String> msgList) {
            recQue = msgList;
            this.msgList = msgList;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {

            Thread newThread = new Thread() {

                public void run() {
                    while (true) {
                        byte[] recvPkt = new byte[1024];
                        DatagramPacket recv = new DatagramPacket(recvPkt, recvPkt.length);
                        try {
                            socket.receive(recv);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        final String medd = new String(recvPkt, 0, recv.getLength());
                        recQue.add(medd);
                        updateListView(medd);
                    }
                }
            };
            newThread.start();
            return null;
        }
    }

    private class sendMessage extends AsyncTask<Void, Void, Boolean> {

        String textMsg;

        sendMessage(String message) {

            textMsg = username + " : " + message;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            byte[] data = textMsg.getBytes();
            DatagramPacket packet = new DatagramPacket(data, data.length, group, portNum);

            try {
                socket.send(packet);
                return true;
            } catch (IOException e) {
                return false;
            }

        }
    }

    private void updateListView(final String message) {

        values = new String[recQue.size()];
        for (int x = 0; x < recQue.size(); x++) {
            values[x] = recQue.get(x);
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), message + " : " + recQue.size(), Toast.LENGTH_SHORT).show();
                adapter = new ArrayAdapter<>(MessageActivity.this, R.layout.list_white_text, R.id.list_content, values);
                listView.setAdapter(adapter);
                listView.setSelection(adapter.getCount() - 1);
            }
        });
        Log.d("cSharing", "Send : " + message);

    }
}
