package com.noonswoonapp.singlequiz;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import com.purplebrain.adbuddiz.sdk.AdBuddiz;


public class SplashScreen extends Activity {

    private static final String QUESTION_PREFS = "questionnaire_db";
    private static final String RESULT_PREFS = "result_db";
    private Handler handler;
    private Runnable runnable;
    private long mDelayTime;
    private long mTime = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AdBuddiz.setPublisherKey(getString(R.string.adbuddiz_publisherkey));
        AdBuddiz.cacheAds(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        createQuestionnaireDB();
        createResultDB();
//        PackageInfo info;
//        try {
//            info = getPackageManager().getPackageInfo("com.noonswoonapp.singlelevel", PackageManager.GET_SIGNATURES);
//            for (Signature signature : info.signatures) {
//                MessageDigest md;
//                md = MessageDigest.getInstance("SHA");
//                md.update(signature.toByteArray());
//                String something = new String(Base64.encode(md.digest(), 0));
//                //String something = new String(Base64.encodeBytes(md.digest()));
//                Log.e("hash key", something);
//            }
//        } catch (PackageManager.NameNotFoundException e1) {
//            Log.e("name not found", e1.toString());
//        } catch (NoSuchAlgorithmException e) {
//            Log.e("no such an algorithm", e.toString());
//        } catch (Exception e) {
//            Log.e("exception", e.toString());
//        }

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
            Utilities.saveArray(new String[]{"ครั้งสุดท้ายตอนรับปริญญา", "อาทิตย์ละครั้ง", "2-3 ครั้ง ต่ออาทิตย์", "ทุกวัน จนสนิทกับแม่เขาแล้ว"}, "QC1", QUESTION_PREFS, this);
            Utilities.saveArray(new String[]{"4", "3", "2", "1"}, "QP1", QUESTION_PREFS, this);

            Utilities.saveArray(new String[]{"สำหรับเพื่อนในกลุ่มของคุณ พวกเขามีแฟนแล้วกี่คน", "question_2"}, "Q2", QUESTION_PREFS, this);
            Utilities.saveArray(new String[]{"ไม่มีเลยซักคน โสดหมด", "มีแฟนแค่คนเดียว ให้เพื่อนอิจฉา", "ยังพอมีหลอมแหลม ชวนไปกินข้าวพอได้", "มีแฟนกันหมดทุกคนยกเว้นคุณ"}, "QC2", QUESTION_PREFS, this);
            Utilities.saveArray(new String[]{"3", "1", "2", "4"}, "QP2", QUESTION_PREFS, this);

            Utilities.saveArray(new String[]{"ถ้าคุณต้องออกไปกินข้าวนอกบ้าน คุณอยากพาใครไป", "question_3"}, "Q3", QUESTION_PREFS, this);
            Utilities.saveArray(new String[]{"ไม่พาไปซักคน กินคนเดียวได้ ประหยัดดี", "พาแฟนไป จะได้สวีทกัน", "พาครอบครัวไป กินกับที่บ้านแหละดีที่สุด", "ชวนเพื่อนจากเฟซบุ๊ก ลองเปิดโอกาสคุยกัน"}, "QC3", QUESTION_PREFS, this);
            Utilities.saveArray(new String[]{"4", "1", "3", "2"}, "QP3", QUESTION_PREFS, this);

            Utilities.saveArray(new String[]{"ถ้าคุณกำลังจะจากที่ทำงาน คุณอยากให้ใครมารับคุณมากที่สุด", "question_4"}, "Q4", QUESTION_PREFS, this);
            Utilities.saveArray(new String[]{"พ่อหรือแม่ สบายใจดี กลับสบายๆ", "เพื่อนสนิท เผื่อได้ไปกินไปเที่ยวกันต่อ สนุกๆ", "แฟน เพราะจะได้เจอหน้ากันบ่อยขึ้น", "ไม่ต้องมารับหรอก กลับเองได้ โตแล้ว"}, "QC4", QUESTION_PREFS, this);
            Utilities.saveArray(new String[]{"3", "2", "1", "4"}, "QP4", QUESTION_PREFS, this);

            Utilities.saveArray(new String[]{"ถ้าคุณอยุ่ที่โรงหนัง หนังแบบใดที่คุณอยากจะดู", "question_5"}, "Q5", QUESTION_PREFS, this);
            Utilities.saveArray(new String[]{"แอกชั่นบู๊ล้างผลาญ ไปดูหนังแนวอื่นกลัวกรนกวนคนอื่น", "โรแมนติกสิ ถ้ามีแฟนหรือคนที่ชอบไปดูด้วยนะ อย่างฟิน", "คอมเมดี้ ขบขัน ทำงานก็เครียดมากแล้ว ผ่อนคลายซะหน่อย", "สยองขวัญสิ ยิ่งไปกับคนที่ชอบนะ จะได้พอมีโอกาส ;-)"}, "QC5", QUESTION_PREFS, this);
            Utilities.saveArray(new String[]{"4", "1", "2", "3"}, "QP5", QUESTION_PREFS, this);

            Utilities.saveArray(new String[]{"ถ้าคุณไปสวนสนุก คุณอยากเล่นเครื่องเล่นอะไร", "question_6"}, "Q6", QUESTION_PREFS, this);
            Utilities.saveArray(new String[]{"ม้าหมุน ยิ่งถ้าไปกับแฟนนะ คงสวีทน่าดู", "บ้านผีสิง จะได้กระตุ้นต่อมเร้าใจในตัวคุณ่", "รถไฟเหาะ ยิ่งเล่นคนเดียว ยิ่งเสียว", "ไปทำไมสวนสนุก อยู่บ้านยังสนุกกว่า"}, "QC6", QUESTION_PREFS, this);
            Utilities.saveArray(new String[]{"1", "2", "3", "4"}, "QP6", QUESTION_PREFS, this);

            Utilities.saveArray(new String[]{"ถ้าคุณไปถีบเรือเป็ดเล่นกับเพื่อน 3 คน คุณจะ...", "question_7"}, "Q7", QUESTION_PREFS, this);
            Utilities.saveArray(new String[]{"ปล่อยเพื่อนอีกคนให้ถีบคนเดียว", "ให้เพื่อนถีบกัน2คน แล้วถีบคนเดียว", "เรือใครเรือมันแยกกันถีบเลย", "ไม่เล่นเลย หาอย่างอื่นทำ"}, "QC7", QUESTION_PREFS, this);
            Utilities.saveArray(new String[]{"1", "4", "3", "2"}, "QP7", QUESTION_PREFS, this);

            Utilities.saveArray(new String[]{"ความคิดของคุณ วันวาเลนไทน์คือ...", "question_8"}, "Q8", QUESTION_PREFS, this);
            Utilities.saveArray(new String[]{"วันที่จะได้บอกชอบคนที่เราชอบ แต่เค้าจะชอบเราหรือไม่อีกเรื่องนึง", "วันที่จะได้กินช็อคโกแลตเยอะๆ", "วันที่จะได้สวีทกับแฟน กระหนุงกระหนิง", "วันธรรมดาวันนึง อยู่คนเดียวสไตล์คนโสด"}, "QC8", QUESTION_PREFS, this);
            Utilities.saveArray(new String[]{"2", "3", "1", "4"}, "QP8", QUESTION_PREFS, this);

            Utilities.saveArray(new String[]{"ถ้าคุณอยากร้องคาราโอเกะ คุณจะไปที่ไหนกับใคร", "question_9"}, "Q9", QUESTION_PREFS, this);
            Utilities.saveArray(new String[]{"ห้องคาราโอเกะตามห้างกับเพื่อนเยอะๆ", "ตู้คาราโอเกะหยอดเหรียญ ร้องคนเดียวพอ", "ต่อไมค์ร้องเองที่บ้านกับหลาน และแมว", "ร้องกับ Youtube ก็พอแล้ว ไมค์ไม่ต้อง แมวไม่ต้อง"}, "QC9", QUESTION_PREFS, this);
            Utilities.saveArray(new String[]{"1", "2", "3", "4"}, "QP9", QUESTION_PREFS, this);

            Utilities.saveArray(new String[]{"กิจกรรมที่คุณจะทำถ้าคุณอยู่ในห้างคนเดียวคือ...", "question_10"}, "Q10", QUESTION_PREFS, this);
            Utilities.saveArray(new String[]{"เดินดูเลือกเสื้อผ้า ตั้งหน้าตั้งตารอวันที่เงินเดือนออก", "เข้าร้านบุฟเฟ่ต์ปิ้งย่างแบบไม่แคร์", "ลองเครื่องสำอาง รอวันลดราคา ค่อยมาซื้อ", "อยู่ร้านหนังสือ ยืนอ่านฟรีสักหน่อย"}, "QC10", QUESTION_PREFS, this);
            Utilities.saveArray(new String[]{"3", "4", "1", "2"}, "QP10", QUESTION_PREFS, this);

            editor.putBoolean("install", false);
            editor.apply();
        }
    }

    private void createResultDB() {
        SharedPreferences shared = getSharedPreferences(RESULT_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = shared.edit();
        if (shared.getBoolean("install", true)) {
            Utilities.saveArray(new String[]{"r_desc_1", "result_1m", "result_1f", "share_1m", "share_1f", "LVL 1 : คุณไม่โสดนี่นา"}, "A1", RESULT_PREFS, this);
            Utilities.saveArray(new String[]{"r_desc_2", "result_2m", "result_2f", "share_2m", "share_2f", "LVL 13 : โสดชิว กลับบ้านคนเดียว"}, "A2", RESULT_PREFS, this);
            Utilities.saveArray(new String[]{"r_desc_3", "result_3m", "result_3f", "share_3m", "share_3f", "LVL 26 : โสดติ๊ส เดินห้างคนเดียว"}, "A3", RESULT_PREFS, this);
            Utilities.saveArray(new String[]{"r_desc_4", "result_4m", "result_4f", "share_4m", "share_4f", "LVL 32 : โสดสู้ฟัด หม่ำข้าวนอกบ้านคนเดียว"}, "A4", RESULT_PREFS, this);
            Utilities.saveArray(new String[]{"r_desc_5", "result_5m", "result_5f", "share_5m", "share_5f", "LVL 44 : โสดจิต ดูหนังเก้าอี้สวีทคนเดียว"}, "A5", RESULT_PREFS, this);
            Utilities.saveArray(new String[]{"r_desc_6", "result_6m", "result_6f", "share_6m", "share_6f", "LVL 56 : โสดบันเทิง ร้องคาราโอเกะคนเดียว"}, "A6", RESULT_PREFS, this);
            Utilities.saveArray(new String[]{"r_desc_7", "result_7m", "result_7f", "share_7m", "share_7f", "LVL 63 : โสดมโน สามารถพูดคนเดียว"}, "A7", RESULT_PREFS, this);
            Utilities.saveArray(new String[]{"r_desc_8", "result_8m", "result_8f", "share_8m", "share_8f", "LVL 75 : โสดหลอน เที่ยวสวนสนุกคนเดียว"}, "A8", RESULT_PREFS, this);
            Utilities.saveArray(new String[]{"r_desc_9", "result_9m", "result_9f", "share_9m", "share_9f", "LVL 89 : โสดสาหัส พายเรือคนเดียว"}, "A9", RESULT_PREFS, this);
            Utilities.saveArray(new String[]{"r_desc_10", "result_10m", "result_10f", "share_10m", "share_10f", "LVL 99 : โสดโ*ตรพ่อ ฉลองวาเลนไทน์คนเดียว"}, "A10", RESULT_PREFS, this);
            editor.putBoolean("install", false);
            editor.apply();
        }
    }
}
