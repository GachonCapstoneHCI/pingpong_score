package com.github.pocmo.pingpongkim;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.github.pocmo.pingpongkim.data.Sensor;
import com.github.pocmo.pingpongkim.events.BusProvider;
import com.github.pocmo.pingpongkim.events.NewSensorEvent;
import com.github.pocmo.pingpongkim.ui.ExportActivity;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.squareup.otto.Subscribe;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private RemoteSensorManager remoteSensorManager;
    public static String TAG = "PingPongBoy";
    Toolbar mToolbar;

    private ViewPager pager;
    private View emptyState;
    private NavigationView mNavigationView;
    private Menu mNavigationViewMenu;
    private List<Node> mNodes;


    //pingpongboy : 뷰에 태그 출력하기
    TextView sensorDataText;
    TextView swingResultText;
    StringBuilder sb;


    //클라이언트 설정
    String serverIp;
    String playerName;

    //클라세팅
    private Socket clientSocket;
    private DataInputStream clientIn;
    private DataOutputStream clientOut;
    private String clientMsg;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //클라이언트 설정
        Intent intent = getIntent();
        serverIp = intent.getStringExtra("server_ip");
        playerName = intent.getStringExtra("player_name");



        //view 설정
        mToolbar = (Toolbar) findViewById(R.id.my_awesome_toolbar);
        emptyState = findViewById(R.id.empty_state);

        mNavigationView = (NavigationView) findViewById(R.id.navView);
        mNavigationView.setNavigationItemSelectedListener(this);
        mNavigationViewMenu = mNavigationView.getMenu();


        //pingpongboy : 뷰에 태그 출력하기
        //sensorDataText = (TextView)findViewById(R.id.sensor_text);
        swingResultText = (TextView)findViewById(R.id.swing_result_text);
        sb = new StringBuilder("");

        //pingponboy : 리시버 등록
        registerReceiver(mSensorDataReceiver, makeIntentFilter());

        initToolbar();
        initViewPager();

        remoteSensorManager = RemoteSensorManager.getInstance(this);

        //final EditText tagname = (EditText) findViewById(R.id.tagname);

//        findViewById(R.id.tag_button).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String tagnameText = "EMPTY";
//                if (!tagname.getText().toString().isEmpty()) {
//                    tagnameText = tagname.getText().toString();
//                }
//
//                RemoteSensorManager.getInstance(MainActivity.this).addTag(tagnameText);
//            }
//        });
//
//        tagname.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//
//            @Override
//            public boolean onEditorAction(TextView v, int actionId,
//                                          KeyEvent event) {
//                if (event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
//                    InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//
//                    in.hideSoftInputFromWindow(tagname
//                                    .getApplicationWindowToken(),
//                            InputMethodManager.HIDE_NOT_ALWAYS);
//
//
//                    return true;
//
//                }
//                return false;
//            }
//        });

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);



        //클라이언트 설정
        //joinServer();

    }

    private void initToolbar() {
        setSupportActionBar(mToolbar);

        final ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(false);
            ab.setTitle(R.string.app_name);
            mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.action_about:
                            startActivity(new Intent(MainActivity.this, AboutActivity.class));
                            return true;
                        case R.id.action_export:
                            startActivity(new Intent(MainActivity.this, ExportActivity.class));
                            return true;
                    }

                    return true;
                }
            });
        }
    }

    private void initViewPager() {
        pager = (ViewPager) findViewById(R.id.pager);

        pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {

            }

            @Override
            public void onPageSelected(int id) {
                ScreenSlidePagerAdapter adapter = (ScreenSlidePagerAdapter) pager.getAdapter();
                if (adapter != null) {
                    Sensor sensor = adapter.getItemObject(id);
                    if (sensor != null) {
                        remoteSensorManager.filterBySensorId((int) sensor.getId());
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        BusProvider.getInstance().register(this);
        List<Sensor> sensors = RemoteSensorManager.getInstance(this).getSensors();
        pager.setAdapter(new ScreenSlidePagerAdapter(getSupportFragmentManager(), sensors));

        //Log.e(TAG,  "sensor size : " + Integer.toString(sensors.size()));

        if (sensors.size() > 0) {
            emptyState.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.VISIBLE);
        }

        remoteSensorManager.startMeasurement();

        mNavigationViewMenu.clear();
        remoteSensorManager.getNodes(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(final NodeApi.GetConnectedNodesResult pGetConnectedNodesResult) {
                mNodes = pGetConnectedNodesResult.getNodes();
                for (Node node : mNodes) {
                    SubMenu menu = mNavigationViewMenu.addSubMenu(node.getDisplayName());

                    MenuItem item = menu.add("15 sensors");
                    if (node.getDisplayName().startsWith("G")) {
                        item.setChecked(true);
                        item.setCheckable(true);
                    } else {
                        item.setChecked(false);
                        item.setCheckable(false);
                    }
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        BusProvider.getInstance().unregister(this);

        remoteSensorManager.stopMeasurement();
    }

    @Override
    public boolean onNavigationItemSelected(final MenuItem pMenuItem) {
        Toast.makeText(this, "Device: " + pMenuItem.getTitle(), Toast.LENGTH_SHORT).show();
        return false;
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        private List<Sensor> sensors;

        public ScreenSlidePagerAdapter(FragmentManager fm, List<Sensor> symbols) {
            super(fm);
            this.sensors = symbols;
        }


        public void addNewSensor(Sensor sensor) {
            this.sensors.add(sensor);
        }


        private Sensor getItemObject(int position) {
            return sensors.get(position);
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            return SensorFragment.newInstance(sensors.get(position).getId());
        }

        @Override
        public int getCount() {
            return sensors.size();
        }

    }


    private void notifyUSerForNewSensor(Sensor sensor) {
        Toast.makeText(this, "New Sensor!\n" + sensor.getName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //리시버 해제 : memory leak 방지
        unregisterReceiver(mSensorDataReceiver);

        //텍스트 제거하기
        swingResultText.setText("");
        sb.setLength(0);
    }

    @Subscribe
    public void onNewSensorEvent(final NewSensorEvent event) {
        Log.e(TAG, "new sensor detected");
        ((ScreenSlidePagerAdapter) pager.getAdapter()).addNewSensor(event.getSensor());
        pager.getAdapter().notifyDataSetChanged();
        emptyState.setVisibility(View.GONE);
        notifyUSerForNewSensor(event.getSensor());
    }

    //receiver 등록
    private final BroadcastReceiver mSensorDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(TAG, "message received " + intent.getAction());
            String action = intent.getAction();
            if(action != null && action.equals(SensorReceiverService.ACTION_DETECT_SWING)){
                String msg = intent.getStringExtra("swingdetect");
                double val = intent.getDoubleExtra("sensordata", 0);
                long timestamp = intent.getLongExtra("timestamp", 0);
                //Log.e(TAG, Long.toString(timestamp));

                DecimalFormat df = new DecimalFormat("###.##");


                if(val > 0) {
                    String str = msg + " / " + df.format(val) + " / " +getDate(timestamp) + "\n";
                    sb.append(str);
                    swingResultText.setText(sb);
                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();

//                    try {
//                        clientOut.writeUTF(playerName + " : " + str);
//                    } catch (IOException e) {
//                        Log.e(TAG, "ERROR : client message send error");
//                        e.printStackTrace();
//                    }
                }
            }
        }
    };

    /**
     * 브로드캐스트에 등록을 할 때 intentFilter 를 보내는데 여기에 3가지 action 을 추가함
     * @return
     */
    private static IntentFilter makeIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SensorReceiverService.ACTION_RECEIVE_SENSOR_DATA);
        intentFilter.addAction(SensorReceiverService.ACTION_DETECT_SWING);
        return intentFilter;
    }

    private String getDate(long timeStamp){

        try{
            SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss.SSS", Locale.KOREA);
            Date netDate = (new Date(timeStamp));
            return sdf.format(netDate);
        }
        catch(Exception ex){
            return "xx";
        }
    }


    //클라이언트 관련
    public void joinServer() {
//        if(playerName==null){
//            playerName=;
//        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //서버 아이피로 접속한다
                    clientSocket = new Socket(serverIp, 7777);
                    Log.v("", "클라이언트 : 서버 연결됨.");

                    clientOut = new DataOutputStream(clientSocket.getOutputStream());
                    clientIn = new DataInputStream(clientSocket.getInputStream());

                    //접속하자마자 닉네임 전송하면. 서버가 이걸 닉네임으로 인식을 하고서 맵에 집어넣겠지요?
                    clientOut.writeUTF(playerName);
                    Log.v("", "클라이언트 : 메시지 전송완료");

                    while (clientIn != null) {
                        try {
                            clientMsg = clientIn.readUTF();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        //handler.sendEmptyMessage(CLIENT_TEXT_UPDATE);
                    }
                } catch (UnknownHostException e1) {
                    e1.printStackTrace();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }).start();
    }



}
