package com.noonswoonapp.singlelevel;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;


public class SplashScreen extends Activity {

    private static final String QUESTION_PREFS = "questionnaire_db";
    private Handler handler;
    private Runnable runnable;
    private long mDelayTime;
    private long mTime = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        createQuestionnaireDB();
        handler = new Handler();
        runnable = new Runnable() {
            public void run() {
                Intent intent = new Intent(SplashScreen.this, LoginScreen.class);
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

    private void createQuestionnaireDB() {
        SharedPreferences shared = getSharedPreferences(QUESTION_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = shared.edit();
        if (shared.getBoolean("install", true)) {
            Utilities.saveArray(new String[]{"คุณออกไปเจอเพื่อนคุณบ่อยแค่ไหน", "question_1"}, "Q1", QUESTION_PREFS, this);
            Utilities.saveArray(new String[]{"ครั้งสุดท้ายตอนรับปริญญา", "อาทิตย์ละครั้ง", "2-3 ครั้ง ต่ออาทิตย์" , "ทุกวัน จนสนิทกับแม่เขาแล้ว"}, "QC1", QUESTION_PREFS, this);
            Utilities.saveArray(new String[]{"0", "-1", "1"}, "QP1", QUESTION_PREFS, this);

            Utilities.saveArray(new String[]{"สำหรับเพื่อนในกลุ่มของคุณ พวกเขามีแฟนแล้วกี่คน", "question_2"}, "Q2", QUESTION_PREFS, this);
            Utilities.saveArray(new String[]{"ไม่มีเลยซักคน โสดหมด", "มีแฟนแค่คนเดียว ให้เพื่อนอิจฉา", "ยังพอมีหลอมแหลม ชวนไปกินข้าวพอได้", "มีแฟนกันหมดทุกคนยกเว้นคุณ"}, "QC2", QUESTION_PREFS, this);
            Utilities.saveArray(new String[]{"-1", "1", "0"}, "QP2", QUESTION_PREFS, this);

            Utilities.saveArray(new String[]{"ถ้าคุณต้องออกไปกินข้าวนอกบ้าน คุณอยากพาใครไป", "question_3"}, "Q3", QUESTION_PREFS, this);
            Utilities.saveArray(new String[]{"ไม่พาไปซักคน กินคนเดียวได้ ประหยัดดี", "พาเพื่อนสนิทๆไปสิ จะได้เมาท์กันสนุกๆ", "พาแฟนไป จะได้สวีทกัน", "พาครอบครัวไป กินกับที่บ้านแหละดีที่สุด"}, "QC3", QUESTION_PREFS, this);
            Utilities.saveArray(new String[]{"0", "1", "-1"}, "QP3", QUESTION_PREFS, this);

            Utilities.saveArray(new String[]{"ถ้าคุณกำลังจะจากที่ทำงาน คุณอยากให้ใครมารับคุณมากที่สุด", "question_4"}, "Q4", QUESTION_PREFS, this);
            Utilities.saveArray(new String[]{"พ่อหรือแม่ สบายใจดี กลับสบายๆ", "เพื่อนสนิท เผื่อได้ไปกินไปเที่ยวกันต่อ สนุกๆ", "แฟน เพราะจะได้เจอหน้ากันบ่อยขึ้น", "ไม่ต้องมารับหรอก กลับเองได้ โตแล้ว"}, "QC4", QUESTION_PREFS, this);
            Utilities.saveArray(new String[]{"-1", "0", "1"}, "QP4", QUESTION_PREFS, this);

            Utilities.saveArray(new String[]{"คุณอยากไปดูหนังแบบไหนที่โรงหนัง", "question_5"}, "Q5", QUESTION_PREFS, this);
            Utilities.saveArray(new String[]{"แอกชั่นบู๊ล้างผลาญ ไปดูหนังแนวอื่นกลัวกรนกวนคนอื่น", "โรแมนติกสิ ถ้ามีแฟนหรือคนที่ชอบไปดูด้วยนะ อย่างฟิน", "คอมเมดี้ ขบขัน ทำงานก็เครียดมากแล้ว ผ่อนคลายซะหน่อย", "สยองขวัญสิ ยิ่งไปกับคนที่ชอบนะ จะได้พอมีโอกาส ;-)"}, "QC5", QUESTION_PREFS, this);
            Utilities.saveArray(new String[]{"1", "0", "-1"}, "QP5", QUESTION_PREFS, this);

            Utilities.saveArray(new String[]{"ถ้าคุณไปสวนสนุก คุณอยากเล่นเครื่องเล่นอะไร", "question_6"}, "Q6", QUESTION_PREFS, this);
            Utilities.saveArray(new String[]{"ม้าหมุน ยิ่งถ้าไปกับแฟนนะ คงสวีทน่าดู", "บ้านผีสิง ไม่ได้กลัวแต่ไปเดินขำชิวๆ แกล้งเป็นผีหลอกคนอื่น", "รถไฟเหาะ ยิ่งเล่นคนเดียว ยิ่งเสียว" , "ไปทำไมสวนสนุก อยู่บ้านยังสนุกกว่า"}, "QC6", QUESTION_PREFS, this);
            Utilities.saveArray(new String[]{"0", "-1", "1"}, "QP6", QUESTION_PREFS, this);

            Utilities.saveArray(new String[]{"ถ้าคุณไปถีบเรือเป็ดเล่นกับเพื่อน 3 คน คุณจะ...", "question_7"}, "Q7", QUESTION_PREFS, this);
            Utilities.saveArray(new String[]{"ปล่อยเพื่อนอีกคนให้ถีบคนเดียว", "ให้เพื่อนถีบกัน2คน แล้วถีบคนเดียว", "เรือใครเรือมันแยกกันถีบเลย", "ไม่เล่นเลย หาอย่างอื่นทำ"}, "QC7", QUESTION_PREFS, this);
            Utilities.saveArray(new String[]{"-1", "1", "0"}, "QP7", QUESTION_PREFS, this);

            Utilities.saveArray(new String[]{"ความคิดของคุณ วันวาเลนไทน์คือ...", "question_8"}, "Q8", QUESTION_PREFS, this);
            Utilities.saveArray(new String[]{"วันที่จะได้บอกชอบคนที่เราชอบ แต่เค้าจะชอบเราหรือไม่อีกเรื่องนึง", "วันที่จะได้กินช็อคโกแลตเยอะๆ", "วันที่จะได้สวีทกับแฟน กระหนุงกระหนิง", "วันธรรมดาวันนึง ไม่ได้พิเศษอะไร"}, "QC8", QUESTION_PREFS, this);
            Utilities.saveArray(new String[]{"0", "1", "-1"}, "QP8", QUESTION_PREFS, this);

            Utilities.saveArray(new String[]{"ถ้าคุณอยากร้องคาราโอเกะ คุณจะไปที่ไหนกับใคร", "question_9"}, "Q9", QUESTION_PREFS, this);
            Utilities.saveArray(new String[]{"ห้องคาราโอเกะตามห้างกับเพื่อนเยอะๆ", "ตู้คาราโอเกะหยอดเหรียญ ร้องคนเดียวพอ", "ต่อไมค์ร้องเองที่บ้านกับหลาน และแมว", "ร้องกับ Youtube ก็พอแล้ว ไมค์ไม่ต้อง แมวไม่ต้อง"}, "QC9", QUESTION_PREFS, this);
            Utilities.saveArray(new String[]{"-1", "0", "1"}, "QP9", QUESTION_PREFS, this);

            Utilities.saveArray(new String[]{"กิจกรรมที่คุณจะทำถ้าคุณอยู่ในห้างคนเดียวคือ...", "question_10"}, "Q10", QUESTION_PREFS, this);
            Utilities.saveArray(new String[]{"เดินดูเลือกเสื้อผ้า และจินตนาการกับตัวเอง", "ลงไปแผนก supermarket หาของชิมฟรี…(หรือหา buffet กิน)", "ลองเครื่องสำอาง รอวันลดราคา ค่อยมาซื้อ", "อยู่ร้านหนังสือ ยืนอ่านฟรีสักหน่อย"}, "QC10", QUESTION_PREFS, this);
            Utilities.saveArray(new String[]{"1", "0", "-1"}, "QP10", QUESTION_PREFS, this);

            editor.putBoolean("install", false);
            editor.apply();
        }
    }
}
