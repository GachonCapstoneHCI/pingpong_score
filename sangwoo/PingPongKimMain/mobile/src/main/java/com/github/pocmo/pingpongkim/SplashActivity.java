package com.github.pocmo.pingpongkim;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

/**
 * 초기 탁구왕 김탁구 화면
 */
public class SplashActivity extends Activity {
    public static final int SPLASH_PERIOD = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        //1초동안 글자 애니메이션 재생 : 깜빡임
        YoYo.with(Techniques.Flash).duration(1000).delay(1000).playOn(findViewById(R.id.textSplash));
        //3초 후에 화면 종료
        new Handler().postDelayed(new SplashHandler(), SPLASH_PERIOD);
    }

    private class SplashHandler implements Runnable{
        public void run() {
            //튜토리얼 화면 실행
            startActivity(new Intent(getApplication(), TutorialActivity.class));
            SplashActivity.this.finish();
        }
    }
}
