package com.noonswoonapp.singlelevel;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;


public class MainActivity extends Activity {

    private static final String RESULT_PREFS = "result_db";
    private static final String RESULT_LVL = "_0";
    private static final String RESULT_DESC1 = "_1";
    private static final String RESULT_DESC2 = "_2";
    private static final String RESULT_IMAGE_M = "_3";
    private static final String RESULT_IMAGE_F = "_4";
    private int point;
    private ImageView mResultImage;
    private TextView mDesc1Text;
    private TextView mDesc2Text;
    private TextView mLvlText;
    private MyApplication mMyApplication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMyApplication = (MyApplication) getApplication();
        mResultImage = (ImageView) findViewById(R.id.imageview_result);
        mDesc1Text = (TextView) findViewById(R.id.textview_desc1);
        mDesc2Text = (TextView) findViewById(R.id.textview_desc2);
        mLvlText = (TextView) findViewById(R.id.textview_lvl);


        Intent intent = getIntent();
        point = intent.getIntExtra("point", 0);
        if (point <= 10 && point >= 12) {
            getResult(1);
        } else if (point >= 13 && point <= 15) {
            getResult(2);
        } else if (point >= 16 && point <= 18) {
            getResult(3);
        } else if (point >= 19 && point <= 21) {
            getResult(4);
        } else if (point >= 22 && point <= 24) {
            getResult(5);
        } else if (point >= 25 && point <= 27) {
            getResult(6);
        } else if (point >= 28 && point <= 30) {
            getResult(7);
        } else if (point >= 31 && point <= 33) {
            getResult(8);
        } else if (point >= 34 && point <= 36) {
            getResult(9);
        } else if (point >= 37 && point <= 40) {
            getResult(10);
        }
    }

    private void getResult(int result) {
        SharedPreferences shared = getSharedPreferences(RESULT_PREFS, Context.MODE_PRIVATE);
        String drawableName = shared.getString("A" + String.valueOf(result) + checkGender(), null);
        int resID = getResources().getIdentifier(drawableName, "drawable", getPackageName());
        mResultImage.setImageResource(resID);
        mLvlText.setText("LVL : " + shared.getString("A" + String.valueOf(result) + RESULT_LVL, null));
        mDesc1Text.setText(shared.getString("A" + String.valueOf(result) + RESULT_DESC1, null));
        mDesc2Text.setText(shared.getString("A" + String.valueOf(result) + RESULT_DESC2, null));

    }

    private String checkGender() {
        try {
            String gender = mMyApplication.getUserProfile().getString("gender");
            switch (gender) {
                case "male":
                    return RESULT_IMAGE_M;
                case "female":
                    return RESULT_IMAGE_F;
                default:
                    return RESULT_IMAGE_M;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return RESULT_IMAGE_M;
    }
}
