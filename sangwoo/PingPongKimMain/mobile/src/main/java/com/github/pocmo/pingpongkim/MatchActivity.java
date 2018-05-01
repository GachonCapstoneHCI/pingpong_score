package com.github.pocmo.pingpongkim;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.pocmo.pingpongkim.match.DeviceDetailFragment;
import com.github.pocmo.pingpongkim.match.DeviceListFragment;
import com.github.pocmo.pingpongkim.match.WiFiDirectBroadcastReceiver;

/**
 * match activity
 * device list frag : 연결 가능한 상대 검색
 * wifi direct 로 다른 상대 검색
 * 검색한 상대와 연결
 * play activity  로 이동
 * device detail frag : 연결 후 보이는 화면
 *
 */
public class MatchActivity extends AppCompatActivity implements WifiP2pManager.ChannelListener, DeviceListFragment.DeviceActionListener {

    public String partnerDeviceName = "NONE";
    //뷰
    private Button buttonFindPlayer;
    private View fragmentList, fragmentDetails;
    boolean isMatched = false;  //상대와 연결 성사 여부

    //wifi direct
    private WifiP2pManager manager;
    private boolean isWifiP2pEnabled = false;
    private boolean retryChannel = false;
    private WifiP2pManager.Channel channel;

    //브로드캐스트 리시버
    private BroadcastReceiver receiver = null;
    private final IntentFilter intentFilter = new IntentFilter();

    private MyReceiver myReceiver;

    //경기 전후
    private TextView textStatus;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match);

        //뷰 객체화
        fragmentDetails = findViewById(R.id.frag_detail);
        fragmentList = findViewById(R.id.frag_list);
        buttonFindPlayer = (Button)findViewById(R.id.buttonFindPlayer);

        //연결을 초기화하는 과정
        buttonFindPlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!isMatched){
                    //상대 매칭 서비스 시작 (wifi direct)
                    if (!isWifiP2pEnabled) {
                        Toast.makeText(MatchActivity.this, "wifi p2p 가 사용불가합니다",
                                Toast.LENGTH_SHORT).show();
                    }

                    //프래그먼트를 가져온다
                    final DeviceListFragment fragment = (DeviceListFragment) getFragmentManager()
                            .findFragmentById(R.id.frag_list);
                    fragment.onInitiateDiscovery();

                    //검색 시작
                    manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {

                        @Override
                        public void onSuccess() {
                            Toast.makeText(MatchActivity.this, "상대를 찾는 중...",
                                    Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(int reasonCode) {
                            Toast.makeText(MatchActivity.this, "상대를 찾는데 실패했습니다!", Toast.LENGTH_SHORT);
                        }
                    });
                }
                else{
                    //연결 끊기
                    disconnect();
                }
            }

        });

        //make intent filter
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        //wifi direct 초기화
        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);

        //리시버 등록
        myReceiver = new MyReceiver();
        registerReceiver(myReceiver, makeIntentFilter());

        //
        textStatus = findViewById(R.id.gameStatus);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(myReceiver); //리시버 해제
    }

    @Override
    protected void onResume() {
        super.onResume();
        //리시버 등록
        receiver = new WiFiDirectBroadcastReceiver(manager, channel, this);
        registerReceiver(receiver, intentFilter);

        //승패 표시
        if(GlobalClass.isGameOver){
            if(GlobalClass.isServer) {
                textStatus.setText("승리");
                textStatus.setTextColor(Color.parseColor("#4472C4"));
            }
            else  {
                textStatus.setText("패배");
                textStatus.setTextColor(Color.parseColor("#FF0000"));
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //리시버 해제
        unregisterReceiver(receiver);
    }

    /**
     * @param isWifiP2pEnabled the isWifiP2pEnabled to set
     */
    public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
        this.isWifiP2pEnabled = isWifiP2pEnabled;
    }

    /**
     * 프래그먼트 데이터들을 초기화
     */
    public void resetData() {
        DeviceListFragment fragmentList = (DeviceListFragment) getFragmentManager()
                .findFragmentById(R.id.frag_list);
        DeviceDetailFragment fragmentDetails = (DeviceDetailFragment) getFragmentManager()
                .findFragmentById(R.id.frag_detail);
        //리스트 프래그먼트 초기화
        if (fragmentList != null) {
            fragmentList.clearPeers();
        }
        //디테일 프래그먼트 초기화
        if (fragmentDetails != null) {
            fragmentDetails.resetViews();
        }

        //매칭 전 상태로 복귀
        isMatched = false;
        buttonFindPlayer.setText("상대 찾기");
        getFragmentManager().findFragmentById(R.id.frag_detail).getView().setVisibility(View.GONE);
        getFragmentManager().findFragmentById(R.id.frag_list).getView().setVisibility(View.VISIBLE);
    }


    /**
     * 매칭 목록이 떴을 때
     */
    public void setData(){
        isMatched = true;
        buttonFindPlayer.setText("연결 해제");
        getFragmentManager().findFragmentById(R.id.frag_detail).getView().setVisibility(View.VISIBLE);
        getFragmentManager().findFragmentById(R.id.frag_list).getView().setVisibility(View.GONE);
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.action_items, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.atn_direct_enable:
//                if (manager != null && channel != null) {
//
//                    // Since this is the system wireless settings activity, it's
//                    // not going to send us a result. We will be notified by
//                    // WiFiDeviceBroadcastReceiver instead.
//
//                    startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
//                } else {
//                    Log.e(GlobalClass.TAG, "channel or manager is null");
//                }
//                return true;
//
//            case R.id.atn_direct_discover:
//                if (!isWifiP2pEnabled) {
//                    Toast.makeText(MatchActivity.this, "p2p warning",
//                            Toast.LENGTH_SHORT).show();
//                    return true;
//                }
//                final DeviceListFragment fragment = (DeviceListFragment) getFragmentManager()
//                        .findFragmentById(R.id.frag_list);
//                fragment.onInitiateDiscovery();
//                manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
//
//                    @Override
//                    public void onSuccess() {
//                        Toast.makeText(MatchActivity.this, "Discovery Initiated",
//                                Toast.LENGTH_SHORT).show();
//                    }
//
//                    @Override
//                    public void onFailure(int reasonCode) {
//                        Toast.makeText(MatchActivity.this, "Discovery Failed : " + reasonCode,
//                                Toast.LENGTH_SHORT).show();
//                    }
//                });
//                return true;
//            default:
//                return super.onOptionsItemSelected(item);
//        }
//    }
    /**
     * SensorService에서 전송한다
     * @return
     */
    private static IntentFilter makeIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SensorReceiverService.ACTION_START_PLAY);
        return intentFilter;
    }


    /**
     * 서비스에서 device 정보를 받아오면 프래그먼트에 뿌려주는 역할을 함
     * @param device
     */
    @Override
    public void showDetails(WifiP2pDevice device) {
        DeviceDetailFragment fragmentDetails = (DeviceDetailFragment) getFragmentManager()
                .findFragmentById(R.id.frag_detail);
        fragmentDetails.showDetails(device);
    }


    /**
     * 매칭
     * @param config
     */
    @Override
    public void connect(WifiP2pConfig config) {
        manager.connect(channel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                //fragment details 설정
                setData();
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(MatchActivity.this, "매칭에 실패했습니다. 다시 시도하세요",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void disconnect() {
        final DeviceDetailFragment fragment = (DeviceDetailFragment) getFragmentManager()
                .findFragmentById(R.id.frag_detail);
        fragment.resetViews();
        manager.removeGroup(channel, new WifiP2pManager.ActionListener() {

            @Override
            public void onFailure(int reasonCode) {
                Log.d(GlobalClass.TAG, "Disconnect failed. Reason :" + reasonCode);

            }

            @Override
            public void onSuccess() {
                resetData();
            }

        });
    }

    /**
     * 연결 해제 시 실행되는 콜백함수
     */
    @Override
    public void onChannelDisconnected() {
        if (manager != null && !retryChannel) {
            Toast.makeText(this, "채널을 잃었습니다. 다시 시도하세요", Toast.LENGTH_LONG).show();
            //프래그먼트 초기화
            resetData();
            retryChannel = true;
            //연결 재시도
            manager.initialize(this, getMainLooper(), this);
        } else {
            Toast.makeText(this,
                    "채널이 영구적으로 연결을 잃었습니다. 연결을 다시 시도해주세요",
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void cancelDisconnect() {

        if (manager != null) {
            final DeviceListFragment fragmentList = (DeviceListFragment) getFragmentManager()
                    .findFragmentById(R.id.frag_list);
            if (fragmentList.getDevice() == null
                    || fragmentList.getDevice().status == WifiP2pDevice.CONNECTED) {
                disconnect();
            } else if (fragmentList.getDevice().status == WifiP2pDevice.AVAILABLE
                    || fragmentList.getDevice().status == WifiP2pDevice.INVITED) {

                manager.cancelConnect(channel, new WifiP2pManager.ActionListener() {

                    @Override
                    public void onSuccess() {
                        Toast.makeText(MatchActivity.this, "연결 취소",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int reasonCode) {
                        Toast.makeText(MatchActivity.this,
                                "연결 취소에 실패했습니다. 코드: " + reasonCode,
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    public void setPartnerDeviceName(String deviceName){
        partnerDeviceName = deviceName;
    }

    /*
     PLAY 액션이 들어오면 PLAY ACTIVITY 를 띄움
     TODO : PLAY 액션 : 경기 시작 버튼 누르면?
     */
    class MyReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action != null && action.equals(SensorReceiverService.ACTION_START_PLAY)){
                //경기중 액티비티를 띄운다
                Intent playActivity = new Intent(MatchActivity.this, PlayActivity.class);
                startActivity(playActivity);
            }
        }
    }

}
