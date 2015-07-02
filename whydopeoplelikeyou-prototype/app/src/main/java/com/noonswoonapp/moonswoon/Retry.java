package com.noonswoonapp.moonswoon;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;


public class Retry extends AppCompatActivity {

    Button mRetry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_retry);

        mRetry = (Button) findViewById(R.id.button_retry);
        mRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Retry.this, SplashScreen.class);
                startActivity(intent);
                finish();
            }
        });
    }

}
