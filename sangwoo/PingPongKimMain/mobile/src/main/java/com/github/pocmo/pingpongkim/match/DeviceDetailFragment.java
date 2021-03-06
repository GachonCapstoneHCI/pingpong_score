/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.pocmo.pingpongkim.match;



import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.pocmo.pingpongkim.GlobalClass;
import com.github.pocmo.pingpongkim.PlayActivity;
import com.github.pocmo.pingpongkim.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import javax.microedition.khronos.opengles.GL;

/**
 * A fragment that manages a particular peer and allows interaction with device
 * i.e. setting up network connection and transferring data.
 */
public class DeviceDetailFragment extends Fragment implements ConnectionInfoListener {

    protected static final int CHOOSE_FILE_RESULT_CODE = 20;
    private View mContentView = null;
    private WifiP2pDevice device;
    private WifiP2pInfo info;
    ProgressDialog progressDialog = null;
    public String partnerName = "NONE";


    TextView textMyname, textYourName;
    Button buttonPlay;
//    Button buttonPlay, buttonCounter, buttonEnd;
//    static Queue<String> msgBuffer;
//    static int counter = 0;
//    static boolean isPlaying = true;
//    static boolean isServer;
//    static int serverPort, myPort;
//    static String serverIP, myIP;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mContentView = inflater.inflate(R.layout.devices_detail, container);

//        mContentView.findViewById(R.id.btn_connect).setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                WifiP2pConfig config = new WifiP2pConfig();
//                config.deviceAddress = device.deviceAddress;
//                config.wps.setup = WpsInfo.PBC;
//                if (progressDialog != null && progressDialog.isShowing()) {
//                    progressDialog.dismiss();
//                }
//                progressDialog = ProgressDialog.show(getActivity(), "Press back to cancel",
//                        "Connecting to :" + device.deviceAddress, true, true
////                        new DialogInterface.OnCancelListener() {
////
////                            @Override
////                            public void onCancel(DialogInterface dialog) {
////                                ((DeviceActionListener) getActivity()).cancelDisconnect();
////                            }
////                        }
//                        );
//                ((DeviceListFragment.DeviceActionListener) getActivity()).connect(config);
//
//            }
//        });

//        mContentView.findViewById(R.id.btn_disconnect).setOnClickListener(
//                new View.OnClickListener() {
//
//                    @Override
//                    public void onClick(View v) {
//                        ((DeviceListFragment.DeviceActionListener) getActivity()).disconnect();
//                    }
//                });
//
//        mContentView.findViewById(R.id.btn_start_client).setOnClickListener(
//                new View.OnClickListener() {
//
//                    @Override
//                    public void onClick(View v) {
//                        // Allow user to pick an image from Gallery or other
//                        // registered apps
//                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//                        intent.setType("image/*");
//                        startActivityForResult(intent, CHOOSE_FILE_RESULT_CODE);
//                    }
//                });

        textMyname = mContentView.findViewById(R.id.myname);
        textYourName = mContentView.findViewById(R.id.yourname);

//        buttonPlay = mContentView.findViewById(R.id.buttonPlay);
//        buttonCounter = mContentView.findViewById(R.id.buttonCounter);
//        buttonEnd = mContentView.findViewById(R.id.buttonEnd);
//        msgBuffer = new LinkedList<>();

//        buttonPlay.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        Intent intent = new Intent(mContentView.getContext(), PlayActivity.class);
//                        startActivity(intent);
                //서버 모드 실행
//                if(isServer){
//                    //API 18 이상 부터는 싱글 쓰레드로 작동하지 않게 만들기 위해서 아래와같은 코드를 넣어줘야한다
//                    new ServerAsyncTask(mContentView.getContext()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//                    new ClientAsyncTask(mContentView.getContext()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//                }
//                else{
//                    new ClientAsyncTask(mContentView.getContext()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//                    new ServerAsyncTask(mContentView.getContext()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//                }
//            }
//        });
//        buttonCounter.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(mContentView.getContext(), "counter!", Toast.LENGTH_SHORT).show();
//                new ClientAsyncTask(mContentView.getContext()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//            }
//        });
//        buttonEnd.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                //end message 보내기
//                isPlaying = false;
//            }
//        });



        return mContentView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        // User has picked an image. Transfer it to group owner i.e peer using
        // FileTransferService.
//        Uri uri = data.getData();
//        TextView statusText = (TextView) mContentView.findViewById(R.id.status_text);
//        statusText.setText("Sending: " + uri);
//        Log.d(GlobalClass.TAG, "Intent----------- " + uri);
//        Intent serviceIntent = new Intent(getActivity(), FileTransferService.class);
//        serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);
//        serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_PATH, uri.toString());
//        serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS,
//                info.groupOwnerAddress.getHostAddress());
//        serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_PORT, 8988);
//        getActivity().startService(serviceIntent);
    }

    @Override
    public void onConnectionInfoAvailable(final WifiP2pInfo info) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        this.info = info;

        this.getView().setVisibility(View.VISIBLE);
        DeviceListFragment fragmentList = (DeviceListFragment) getFragmentManager()
                .findFragmentById(R.id.frag_list);
        fragmentList.getView().setVisibility(View.GONE);
        if(info.isGroupOwner){
            GlobalClass.isServer = true;
            GlobalClass.myPort = 3939;
            GlobalClass.serverPort = 4949;
            GlobalClass.serverIP = "";
            GlobalClass.myIP = info.groupOwnerAddress.toString().substring(1);
        }
        else{
            GlobalClass.isServer = false;
            GlobalClass.myPort = 4949;
            GlobalClass.serverPort = 3939;
            GlobalClass.myIP = "";
            GlobalClass.serverIP = info.groupOwnerAddress.toString().substring(1);
        }
        Log.e(GlobalClass.TAG,  "my IP : " + GlobalClass.myIP);
        Log.e(GlobalClass.TAG,  "server IP : " + GlobalClass.serverIP);

        //텍스트 설정
        //textYourName.setText(partnerName);

        // The owner IP is now known.
//        TextView view = (TextView) mContentView.findViewById(R.id.group_owner);
//        view.setText("group owner"
//                + ((info.isGroupOwner == true) ? "yes"
//                        : "no"));
//
//        // InetAddress from WifiP2pInfo struct.
//        view = (TextView) mContentView.findViewById(R.id.device_info);
//        view.setText("Group Owner IP - " + info.groupOwnerAddress.getHostAddress());
//
//        // After the group negotiation, we assign the group owner as the file
//        // server. The file server is single threaded, single connection server
//        // socket.
//        if (info.groupFormed && info.isGroupOwner) {
//            new FileServerAsyncTask(getActivity(), mContentView.findViewById(R.id.status_text))
//                    .execute();
//        } else if (info.groupFormed) {
//            // The other device acts as the client. In this case, we enable the
//            // get file button.
//            mContentView.findViewById(R.id.btn_start_client).setVisibility(View.VISIBLE);
//            ((TextView) mContentView.findViewById(R.id.status_text)).setText("client text");
//        }

        // hide the connect button
//        mContentView.findViewById(R.id.btn_connect).setVisibility(View.GONE);
    }

    /**
     * Updates the UI with device data
     * 이건 작동하지 않는 듯
     * @param device the device to be displayed
     */
    public void showDetails(WifiP2pDevice device) {
        this.device = device;
        this.getView().setVisibility(View.VISIBLE);
//        TextView view = (TextView) mContentView.findViewById(R.id.device_address);
//        view.setText(device.deviceAddress);
//        view = (TextView) mContentView.findViewById(R.id.device_info);
//        view.setText(device.toString());

        textYourName.setText(device.deviceName);
    }

    /**
     * Clears the UI fields after a disconnect or direct mode disable operation.
     */
    public void resetViews() {
//        mContentView.findViewById(R.id.btn_connect).setVisibility(View.VISIBLE);
//        TextView view = (TextView) mContentView.findViewById(R.id.device_address);
//        view.setText("empty");
//        view = (TextView) mContentView.findViewById(R.id.device_info);
//        view.setText("empty");
//        view = (TextView) mContentView.findViewById(R.id.group_owner);
//        view.setText("empty");
//        view = (TextView) mContentView.findViewById(R.id.status_text);
//        view.setText("empty");
//        mContentView.findViewById(R.id.btn_start_client).setVisibility(View.GONE);
        this.getView().setVisibility(View.GONE);
    }

    /**
     * A simple server socket that accepts connection and writes some data on
     * the stream.
     */
//    public static class FileServerAsyncTask extends AsyncTask<Void, Void, String> {
//
//        private Context context;
//        private TextView statusText;

        /**
//         * @param context
//         * @param statusText
//         */
//        public FileServerAsyncTask(Context context, View statusText) {
//            this.context = context;
//            this.statusText = (TextView) statusText;
//        }
//
//        @Override
//        protected String doInBackground(Void... params) {
//            try {
//                ServerSocket serverSocket = new ServerSocket(8988);
//                Log.d(GlobalClass.TAG, "Server: Socket opened");
//                Socket client = serverSocket.accept();
//                Log.d(GlobalClass.TAG, "Server: connection done");
//                final File f = new File(Environment.getExternalStorageDirectory() + "/"
//                        + context.getPackageName() + "/wifip2pshared-" + System.currentTimeMillis()
//                        + ".jpg");
//
//                File dirs = new File(f.getParent());
//                if (!dirs.exists())
//                    dirs.mkdirs();
//                f.createNewFile();
//
//                Log.d(GlobalClass.TAG, "server: copying files " + f.toString());
//                InputStream inputstream = client.getInputStream();
//                //copyFile(inputstream, new FileOutputStream(f));
//                serverSocket.close();
//                return f.getAbsolutePath();
//            } catch (IOException e) {
//                Log.e(GlobalClass.TAG, e.getMessage());
//                return null;
//            }
//        }
//
//        /*
//         * (non-Javadoc)
//         * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
//         */
//        @Override
//        protected void onPostExecute(String result) {
//            if (result != null) {
//                statusText.setText("File copied - " + result);
//                Intent intent = new Intent();
//                intent.setAction(Intent.ACTION_VIEW);
//                intent.setDataAndType(Uri.parse("file://" + result), "image/*");
//                context.startActivity(intent);
//            }
//
//        }
//
//        /*
//         * (non-Javadoc)
//         * @see android.os.AsyncTask#onPreExecute()
//         */
//        @Override
//        protected void onPreExecute() {
//            statusText.setText("Opening a server socket");
//        }
//
//    }

//    public static boolean copyFile(InputStream inputStream, OutputStream out) {
//        byte buf[] = new byte[1024];
//        int len;
//        try {
//            while ((len = inputStream.read(buf)) != -1) {
//                out.write(buf, 0, len);
//
//            }
//            out.close();
//            inputStream.close();
//        } catch (IOException e) {
//            Log.d(GlobalClass.TAG, e.toString());
//            return false;
//        }
//        return true;
//    }

//    /**
//     * A simple server socket that accepts connection and writes some data on
//     * the stream.
//     */
//    public static class ServerAsyncTask extends AsyncTask<Void, Void, String> {
//
//        private Context context;
//        //private TextView statusText;
//
//        /**
//         * @param context
//         * //@param statusText
//         */
//        public ServerAsyncTask(Context context) {
//            this.context = context;
//            //this.statusText = (TextView) statusText;
//        }
//
//        @Override
//        protected String doInBackground(Void... params) {
//            try {
//                ServerSocket serverSocket = new ServerSocket(myPort);
//                if(!isServer) myIP = serverSocket.getInetAddress().toString().substring(1);
//                Log.e(GlobalClass.TAG, "Server: Socket opened - " + myIP + "/" + myPort);
//                //Toast.makeText(context, "Server: Sockt opened", Toast.LENGTH_SHORT).show();
//
//                while(isPlaying){
//                    //연결 요청을 기다린다
//                    Socket clientSocket = serverSocket.accept();
//                    Log.e(GlobalClass.TAG, "Server: Client address : " + clientSocket.getInetAddress().toString().substring(1));
//                    //clientsocket : 통신 작업 수행!!
//                    serverIP = clientSocket.getInetAddress().toString().substring(1);
//
//                    //System.out.println( addr.getHostAddress() + "님이 접속 !!");
//                    //Toast.makeText(context, addr.getHostAddress() , Toast.LENGTH_SHORT).show();
//
//                    InputStream is = clientSocket.getInputStream();  // 클라이언트와 연결된 입력 통로. 입력받은 것을 저장시킴.
//                    BufferedReader br = new BufferedReader( new InputStreamReader( is ));
//                    //readLine 받을 때까지 기다리나?
//                    String msg = br.readLine();
//                    Log.e(GlobalClass.TAG, "Server: message received - " + msg);
//
//                    br.close();
//                    is.close();
//                    clientSocket.close();
//                }
//                serverSocket.close();
//
//                return "server connection end!";
//            } catch (IOException e) {
//                Log.e(GlobalClass.TAG, e.getMessage());
//                return null;
//            }
//        }
//
//        /*
//         * (non-Javadoc)
//         * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
//         */
//        @Override
//        protected void onPostExecute(String result) {
//            //if (result != null) {
//                //statusText.setText("File copied - " + result);
////                Intent intent = new Intent();
////                intent.setAction(Intent.ACTION_VIEW);
////                intent.setDataAndType(Uri.parse("file://" + result), "image/*");
////                context.startActivity(intent);
//           // }
//
//        }
//
//        /*
//         * (non-Javadoc)
//         * @see android.os.AsyncTask#onPreExecute()
//         */
//        @Override
//        protected void onPreExecute() {
//            //statusText.setText("Opening a server socket");
//        }
//
//    }
//
//
//
//    /**
//     * A simple server socket that accepts connection and writes some data on
//     * the stream.
//     */
//    public static class ClientAsyncTask extends AsyncTask<Void, Void, String> {
//
//        private Context context;
//        //private TextView statusText;
//
//        /**
//         * @param context
//         * //@param statusText
//         */
//        public ClientAsyncTask(Context context) {
//            this.context = context;
//            //this.statusText = (TextView) statusText;
//        }
//
//        @Override
//        protected String doInBackground(Void... params) {
//            try {
//                //아직 serverIP 에 대한 초기화가 안되면 기다린다.
//                while(isServer && serverIP.equals(""));
//                Socket socket = new Socket(serverIP, serverPort);
//                Log.e(GlobalClass.TAG, "Client: Socket opened - " + serverIP + "/" + serverPort);
//                //clientsocket : 통신 작업 수행!!
//
//                OutputStream os = socket.getOutputStream();  // 클라이언트와 연결된 입력 통로. 입력받은 것을 저장시킴.
//                PrintWriter pw = new PrintWriter( new OutputStreamWriter( os ));
//                //버퍼에 값이 생길 때만 메시지를 전송한다
//                //먼저 시작 메시지를 보낸다
//                pw.write(Integer.toString(counter++));
//                Log.e(GlobalClass.TAG, "Client: write - " + Integer.toString(counter-1));
//                //readStatus 가  false 가 되면 reading 을 중지한다
//                pw.close();
//                os.close();
//                socket.close();
//
//                return "server connection end!";
//            } catch (IOException e) {
//                Log.e(GlobalClass.TAG, e.getMessage());
//                return null;
//            }
//        }
//
//        /*
//         * (non-Javadoc)
//         * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
//         */
//        @Override
//        protected void onPostExecute(String result) {
//            //if (result != null) {
//            //statusText.setText("File copied - " + result);
////                Intent intent = new Intent();
////                intent.setAction(Intent.ACTION_VIEW);
////                intent.setDataAndType(Uri.parse("file://" + result), "image/*");
////                context.startActivity(intent);
//            // }
//
//        }
//
//        /*
//         * (non-Javadoc)
//         * @see android.os.AsyncTask#onPreExecute()
//         */
//        @Override
//        protected void onPreExecute() {
//            //statusText.setText("Opening a server socket");
//        }
//
//    }


}
