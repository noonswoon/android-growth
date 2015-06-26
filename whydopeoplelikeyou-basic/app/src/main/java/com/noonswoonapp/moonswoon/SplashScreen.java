package com.noonswoonapp.moonswoon;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;


public class SplashScreen extends AppCompatActivity {

    private Handler handler;
    private Runnable runnable;
    private long mDelayTime;
    private long mTime = 1000;

    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        handler = new Handler();

        runnable = new Runnable() {
            public void run() {
                Intent intent = new Intent(SplashScreen.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        };

    }

    public void onResume() {
        super.onResume();
        mDelayTime = mTime;
        handler.postDelayed(runnable, mDelayTime);
        mTime = System.currentTimeMillis();
    }

    public void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable);
        mTime = mDelayTime - (System.currentTimeMillis() - mTime);
    }

}
