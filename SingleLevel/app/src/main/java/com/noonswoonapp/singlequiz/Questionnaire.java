package com.noonswoonapp.singlequiz;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.jlmd.animatedcircleloadingview.AnimatedCircleLoadingView;


public class Questionnaire extends Activity implements View.OnClickListener {

    private static final String QUESTION_PREFS = "questionnaire_db";
    private static final int QUESTION_MAX = 10;
    private AnimatedCircleLoadingView animatedCircleLoadingView;
    private Button mChoice1;
    private Button mChoice2;
    private Button mChoice3;
    private Button mChoice4;
    private ImageView mQuestionImage;
    private TextView mQuestionText;
    private FrameLayout mLoadingLayout;
    private int question = 0;
    private int point = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questionnaire);

        mChoice1 = (Button) findViewById(R.id.button_choice1);
        Utilities.changeFontSuperMarket(mChoice1, this);
        mChoice2 = (Button) findViewById(R.id.button_choice2);
        Utilities.changeFontSuperMarket(mChoice2, this);
        mChoice3 = (Button) findViewById(R.id.button_choice3);
        Utilities.changeFontSuperMarket(mChoice3, this);
        mChoice4 = (Button) findViewById(R.id.button_choice4);
        Utilities.changeFontSuperMarket(mChoice4, this);
        mQuestionImage = (ImageView) findViewById(R.id.imageview_questionnaire);
        mQuestionText = (TextView) findViewById(R.id.textview_question);
        Utilities.changeFontSuperMarket(mQuestionText, this);
        mLoadingLayout = (FrameLayout) findViewById(R.id.framelayout_loading);
        animatedCircleLoadingView = (AnimatedCircleLoadingView) findViewById(R.id.circle_loading_view);
        loadQuestion();
        initLoadingScreen();

        mChoice1.setOnClickListener(this);
        mChoice2.setOnClickListener(this);
        mChoice3.setOnClickListener(this);
        mChoice4.setOnClickListener(this);
    }

    private void loadQuestion() {
        SharedPreferences shared = getSharedPreferences(QUESTION_PREFS, Context.MODE_PRIVATE);

        if (question < QUESTION_MAX) {
            question++;
            String drawableName = shared.getString("Q" + String.valueOf(question) + "_1", null);
            int resID = getResources().getIdentifier(drawableName, "drawable", getPackageName());
            mQuestionImage.setImageResource(resID);

            mQuestionText.setText(shared.getString("Q" + String.valueOf(question) + "_0", null));
            mChoice1.setText(shared.getString("QC" + String.valueOf(question) + "_0", null));
            mChoice2.setText(shared.getString("QC" + String.valueOf(question) + "_1", null));
            mChoice3.setText(shared.getString("QC" + String.valueOf(question) + "_2", null));
            mChoice4.setText(shared.getString("QC" + String.valueOf(question) + "_3", null));
        } else if (question == QUESTION_MAX){
            mLoadingLayout.setVisibility(View.VISIBLE);
            animatedCircleLoadingView.setVisibility(View.VISIBLE);
            mChoice1.setClickable(false);
            mChoice2.setClickable(false);
            mChoice3.setClickable(false);
            mChoice4.setClickable(false);
            startPercentMockThread();
        }
    }

    private void addPoint(String choice) {
        SharedPreferences shared = getSharedPreferences(QUESTION_PREFS, Context.MODE_PRIVATE);
        point = point + Integer.parseInt(shared.getString("QP" + String.valueOf(question) + "_" + choice, null));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_choice1:
                addPoint("0");
                loadQuestion();
                break;
            case R.id.button_choice2:
                addPoint("1");
                loadQuestion();
                break;
            case R.id.button_choice3:
                addPoint("2");
                loadQuestion();
                break;
            case R.id.button_choice4:
                addPoint("3");
                loadQuestion();
                break;
        }
    }

    private void initLoadingScreen() {
        animatedCircleLoadingView.startDeterminate();
    }

    private void startPercentMockThread() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1500);
                    for (int i = 0; i <= 100; i++) {
                        Thread.sleep(20);
                        changePercent(i);
                    }
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(Questionnaire.this, MainActivity.class);
                intent.putExtra("point", point);
                startActivity(intent);
                finish();
            }
        };
        new Thread(runnable).start();
    }

    private void changePercent(final int percent) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                animatedCircleLoadingView.setPercent(percent);
            }
        });
    }

}
