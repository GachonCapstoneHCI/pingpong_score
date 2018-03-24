package com.github.pocmo.pingpongkim;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class SplashActivity extends Activity {


    Handler mHandler;
    public static final int SPLASH_PERIOD = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        //3초 후에 화면 종료
        mHandler = new Handler();
        mHandler.postDelayed(new SplashHandler(), SPLASH_PERIOD);

    }

    private class SplashHandler implements Runnable{
        public void run() {
            startActivity(new Intent(getApplication(), TutorialActivity.class));
            SplashActivity.this.finish();
        }
    }
}
