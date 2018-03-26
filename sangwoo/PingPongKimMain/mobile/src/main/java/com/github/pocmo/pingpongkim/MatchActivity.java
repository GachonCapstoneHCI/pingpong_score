package com.github.pocmo.pingpongkim;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MatchActivity extends AppCompatActivity {

    Button buttonFindPlayer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match);

        buttonFindPlayer = (Button)findViewById(R.id.buttonFindPlayer);
        buttonFindPlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //상대 매칭 서비스 시작 (wifi direct)
            }
        });


    }
}
