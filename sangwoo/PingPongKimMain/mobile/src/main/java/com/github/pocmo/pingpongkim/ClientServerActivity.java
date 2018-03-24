package com.github.pocmo.pingpongkim;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ClientServerActivity extends AppCompatActivity {

    public static String TAG = "PinpPongBoy/CS";

    Button buttonStartServer;
    Button buttonStartClient;
    EditText editTextServerIp;
    EditText editTextPlayerName;


    //서버용


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_server);
        buttonStartServer = (Button)findViewById(R.id.buttonStartServer);
        buttonStartClient = (Button)findViewById(R.id.buttonStartClient);
        editTextServerIp = (EditText)findViewById(R.id.editTextServerIp);
        editTextPlayerName = (EditText)findViewById(R.id.editTextPlayerName);


        buttonStartServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //서버로 시작하고 서버화면으로 넘어간다
                startActivity(new Intent(ClientServerActivity.this, ServerActivity.class));
            }
        });

        buttonStartClient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "플레이어로 시작합니다", Toast.LENGTH_SHORT).show();
                //클라이언트으로 시작하고 화면으로 넘어간다
                Intent clientIntent = new Intent(ClientServerActivity.this, MainActivity.class);
                clientIntent.putExtra("server_ip", editTextServerIp.getText().toString());
                clientIntent.putExtra("player_name", editTextPlayerName.getText().toString());
                startActivity(clientIntent);
            }
        });
    }



}
