package com.noonswoonapp.moonswoon;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.github.jlmd.animatedcircleloadingview.AnimatedCircleLoadingView;


public class Questionnaire extends AppCompatActivity implements View.OnClickListener {

    private AnimatedCircleLoadingView animatedCircleLoadingView;
    private static final String PREFS = "question_db";
    private static final int TOTAL_QUESTION = 5;
    private TextView mQuestion;
    private ImageView mQuestionImage;
    private RadioButton mChoice1;
    private RadioButton mChoice2;
    private RadioButton mChoice3;
    private FrameLayout mLoading;
    private int question = 1;
    private int point = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questionaire);

        mQuestionImage = (ImageView) findViewById(R.id.view_image_question);
        changeFontSuperMarket(mQuestion = (TextView) findViewById(R.id.view_text_question));
        changeFontSuperMarket(mChoice1 = (RadioButton) findViewById(R.id.button_choice1));
        changeFontSuperMarket(mChoice2 = (RadioButton) findViewById(R.id.button_choice2));
        changeFontSuperMarket(mChoice3 = (RadioButton) findViewById(R.id.button_choice3));
        animatedCircleLoadingView = (AnimatedCircleLoadingView) findViewById(R.id.circle_loading_view);
        mLoading = (FrameLayout) findViewById(R.id.layout_circle_loading);
        loadQuestion();
        startLoading();

        mChoice1.setOnClickListener(this);
        mChoice2.setOnClickListener(this);
        mChoice3.setOnClickListener(this);
    }

    private void loadQuestion() {
        SharedPreferences shared = getSharedPreferences(PREFS, Context.MODE_PRIVATE);

        String drawableName = shared.getString("Q" + String.valueOf(question) + "_1", null);
        int resID = getResources().getIdentifier(drawableName, "drawable", getPackageName());
        mQuestionImage.setImageResource(resID);
        mQuestion.setText(shared.getString("Q" + String.valueOf(question) + "_0", null));
        mChoice1.setText(shared.getString("QC" + String.valueOf(question) + "_0", null));
        mChoice2.setText(shared.getString("QC" + String.valueOf(question) + "_1", null));
        mChoice3.setText(shared.getString("QC" + String.valueOf(question) + "_2", null));
    }

    private void addPoint(String choice) {
        SharedPreferences shared = getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        point = point + Integer.parseInt(shared.getString("QP" + String.valueOf(question) + "_" + choice, null));
    }

    private void changeFontSuperMarket(TextView textView) {
        Typeface font = Typeface.createFromAsset(getAssets(), "font/supermarket.ttf");
        textView.setTypeface(font);
    }


    @Override
    public void onClick(View v) {
        if (question <= TOTAL_QUESTION) {
            switch (v.getId()) {
                case R.id.button_choice1:
                    mChoice1.setChecked(false);
                    addPoint("0");
                    loadActivity();
                    break;
                case R.id.button_choice2:
                    mChoice2.setChecked(false);
                    addPoint("1");
                    loadActivity();
                    break;
                case R.id.button_choice3:
                    mChoice3.setChecked(false);
                    addPoint("2");
                    loadActivity();
                    break;
                default:
                    Log.e("Error:", "Somethings gone wrong with buttons");
                    break;
            }
        }
    }

    private void loadActivity() {
        if (question == TOTAL_QUESTION) {
            mLoading.setVisibility(View.VISIBLE);
            animatedCircleLoadingView.setVisibility(View.VISIBLE);
            startPercentMockThread();
        }
        if (question < TOTAL_QUESTION) {
            question++;
            loadQuestion();
        }
    }

    private void startLoading() {
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
