package com.noonswoonapp.moonswoon;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;


public class Questionnaire extends AppCompatActivity implements View.OnClickListener {

    private static final String PREFS = "question_db";
    private static final boolean UPDATE_DB = false;
    private static final int TOTAL_QUESTION = 5;
    private TextView mQuestion;
    private RadioButton mChoice1;
    private RadioButton mChoice2;
    private RadioButton mChoice3;
    private int question = 1;
    private int point = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questionaire);

        changeFontSuperMarket(mQuestion = (TextView) findViewById(R.id.view_text_question));
        changeFontSuperMarket(mChoice1 = (RadioButton) findViewById(R.id.button_choice1));
        changeFontSuperMarket(mChoice2 = (RadioButton) findViewById(R.id.button_choice2));
        changeFontSuperMarket(mChoice3 = (RadioButton) findViewById(R.id.button_choice3));

        loadDB();
        loadQuestion();

        mChoice1.setOnClickListener(this);
        mChoice2.setOnClickListener(this);
        mChoice3.setOnClickListener(this);
    }

    private void loadQuestion() {
        SharedPreferences shared = getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        if (question > TOTAL_QUESTION) {
            loadActivity();
        }
        mQuestion.setText(shared.getString("Q" + String.valueOf(question), null));
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

    private void loadDB() {

        SharedPreferences shared = getSharedPreferences(PREFS,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = shared.edit();
        if (shared.getBoolean("install", true) || true) {
            editor.putString("Q1", "Do you think that you are cute?");
            Utilities.saveArray(new String[]{"Definitely, I am cute!", "Sometimes", "Never, I don’t think I am cute"}, "QC1", PREFS, Questionnaire.this);
            Utilities.saveArray(new String[]{"0", "-1", "1"}, "QP1", PREFS, Questionnaire.this);

            editor.putString("Q2", "How often do you wear a makeup?");
            Utilities.saveArray(new String[]{"Every day", "Sometimes", "Never"}, "QC2", PREFS, Questionnaire.this);
            Utilities.saveArray(new String[]{"-1", "1", "0"}, "QP2", PREFS, Questionnaire.this);

            editor.putString("Q3", "How often do you workout ?");
            Utilities.saveArray(new String[]{"Every day", "Almost every day", "Never"}, "QC3", PREFS, Questionnaire.this);
            Utilities.saveArray(new String[]{"0", "1", "-1"}, "QP3", PREFS, Questionnaire.this);

            editor.putString("Q4", "What kind of food do you like?");
            Utilities.saveArray(new String[]{"Thai Food", "Chinese/Japanese/Korean Food", "Western Food"}, "QC4", PREFS, Questionnaire.this);
            Utilities.saveArray(new String[]{"-1", "0", "1"}, "QP4", PREFS, Questionnaire.this);

            editor.putString("Q5", "Please rate how attractive you are?");
            Utilities.saveArray(new String[]{"7 - 10", "4 - 6", "0 - 3"}, "QC5", PREFS, Questionnaire.this);
            Utilities.saveArray(new String[]{"1", "0", "-1"}, "QP5", PREFS, Questionnaire.this);

            editor.putBoolean("install", false);
            editor.apply();
        }
    }

    @Override
    public void onClick(View v) {
        if (question <= TOTAL_QUESTION) {
            switch (v.getId()) {
                case R.id.button_choice1:
                    mChoice1.setChecked(false);
                    addPoint("0");
                    question++;
                    loadQuestion();
                    break;
                case R.id.button_choice2:
                    mChoice2.setChecked(false);
                    addPoint("1");
                    question++;
                    loadQuestion();
                    break;
                case R.id.button_choice3:
                    mChoice3.setChecked(false);
                    addPoint("2");
                    question++;
                    loadQuestion();
                    break;
                default:
                    Log.e("Error:", "Somethings gone wrong with buttons");
                    break;
            }
        }
    }

    private void loadActivity() {
        Intent intent = new Intent(Questionnaire.this, MainActivity.class);
        intent.putExtra("point", point);
        startActivity(intent);
        finish();
    }
}
