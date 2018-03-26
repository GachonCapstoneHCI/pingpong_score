package com.github.pocmo.pingpongkim;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

/**
 * 경기중 스코어나 경기 상태를 보여주는 페이지
 */
public class PlayActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
    }
}
