package com.noonswoonapp.moonswoon;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.TextView;


public class Questionnaire extends AppCompatActivity {

    private static final String PREFS = "question_db";
    private static final boolean UPDATE_DB = false;
    private TextView mQuestion;
    private Button mChoice1;
    private Button mChoice2;
    private Button mChoice3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questionaire);
        loadDB();

        TextView mQuestion = (TextView) findViewById(R.id.view_text_question);
        Button mChoice1 = (Button) findViewById(R.id.button_choice1);
        Button mChoice2 = (Button) findViewById(R.id.button_choice2);
        Button mChoice3 = (Button) findViewById(R.id.button_choice3);

    }

    private void loadDB() {
        SharedPreferences shared = getSharedPreferences(PREFS,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = shared.edit();
        if (shared.getBoolean("install", true) || shared.getBoolean("update", true)) {
            editor.putString("Q1", "Question 1");
            Utilities.saveArray(new String[]{"Choice1", "Choice2", "Choice3"}, "Q1", PREFS, Questionnaire.this);

            editor.putString("Q2", "Question 2");
            Utilities.saveArray(new String[]{"Choice1", "Choice2", "Choice3"}, "Q2",PREFS, Questionnaire.this);

            editor.putBoolean("install", false);
            editor.putBoolean("update", UPDATE_DB);
            editor.apply();
        }
    }

}
