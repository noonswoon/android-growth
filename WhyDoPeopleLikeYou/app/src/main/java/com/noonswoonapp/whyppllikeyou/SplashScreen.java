package com.noonswoonapp.whyppllikeyou;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;

import com.crashlytics.android.Crashlytics;
import com.purplebrain.adbuddiz.sdk.AdBuddiz;

import io.fabric.sdk.android.Fabric;


public class SplashScreen extends AppCompatActivity {

    private Handler handler;
    private Runnable runnable;
    private long mDelayTime;
    private long mTime = 1000;
    private static final String RESULT_PREFS = "result_db";
    private static final String QUESTION_PREFS = "question_db";

    public void onCreate(Bundle savedInstanceState) {
        AdBuddiz.setPublisherKey(getString(R.string.adbuddiz_publisherkey));
        AdBuddiz.cacheAds(this);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_splash_screen);
        createResultDB();
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

    private void createResultDB() {
        SharedPreferences shared = getSharedPreferences(RESULT_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = shared.edit();
        if (shared.getBoolean("install", true)) {
            Utilities.saveArray(new String[]{"Beautiful eyes, Cute smile, Silky hair",
                            "ตาตี่ จิตใจเหี้ยม",
                            "result_1",
                            "ตี๋อำมหิต"},
                    "A", RESULT_PREFS, this);
            Utilities.saveArray(new String[]{"Nice skin, Good-looking, Adorable",
                            "บั้นท้ายใหญ่ ยิ้มง่าย",
                            "result_2",
                            "บั้นท้ายพิฆาต"},
                    "B", RESULT_PREFS, this);
            Utilities.saveArray(new String[]{"Sexy, Charming, Attractive",
                            "ขาใหญ่ ขำขัน",
                            "result_3",
                            "ขาหมูสะท้านฟ้า"},
                    "C", RESULT_PREFS, this);
            Utilities.saveArray(new String[]{"Glamorous lips, Perfect body, Nice attitude",
                            "รูจมูกบาน เซ็กซี่",
                            "result_4",
                            "จมูกเครื่องดูดฝุ่น"},
                    "D", RESULT_PREFS, this);
            Utilities.saveArray(new String[]{"Slim body, Skinny Legs, Beautiful mind",
                            "คิ้วดก พูดตรง",
                            "result_5", "" +
                            "คิ้วสาหร่าย"},
                    "E", RESULT_PREFS, this);
            Utilities.saveArray(new String[]{"Homely looking, Fine hairstyle, Positive attitude",
                            "รักแร้เหม็น ทะเล้น",
                            "result_6",
                            "กลิ่นตัวสะท้านโลกา"},
                    "F", RESULT_PREFS, this);
            Utilities.saveArray(new String[]{"Friendly, Good-humored, Nice smile",
                            "จ้ำม่ำ กินเก่ง",
                            "result_7",
                            "จอมเขมือบแห่งศตวรรษ"},
                    "G", RESULT_PREFS, this);
            Utilities.saveArray(new String[]{"Nice legs, Curvy body, Easy-going",
                            "ขนจมูกยาว รวย",
                            "result_8",
                            "เส้นขนทะยานฟ้า"},
                    "H", RESULT_PREFS, this);
            Utilities.saveArray(new String[]{"Chubby, Good-humoured, Friendly",
                            "รักแร้ดำ จิตใจดี",
                            "result_9",
                            "หมักหมมบ่มเชื้อรา"},
                    "I", RESULT_PREFS, this);
            Utilities.saveArray(new String[]{"Skinny, Funny, Have Sense of Humor",
                            "ริมฝีปากอวบ ถ่อมตน",
                            "result_10",
                            "จูบเย้ยจันทร์"},
                    "J", RESULT_PREFS, this);
            Utilities.saveArray(new String[]{"Adorable Dimples, Round Face, Fine Skin",
                            "ไร้รอยตีนกา ขี้เหนียว",
                            "result_11",
                            "หน้าเด็กตลอดกาล"},
                    "K", RESULT_PREFS, this);
            Utilities.saveArray(new String[]{"Cheeky, Funny, Attractive",
                            "หูกาง ชอบรับฟัง",
                            "result_12",
                            "หูกระด้ง"},
                    "L", RESULT_PREFS, this);
            Utilities.saveArray(new String[]{"Rich, Generous, Beautiful face",
                            "ขนหน้าแข้งดก เป็นกันเอง",
                            "result_13",
                            "ขาหัวไชเท้า"},
                    "M", RESULT_PREFS, this);
            Utilities.saveArray(new String[]{"Not Aged, Smooth Skin, No Wrinkles",
                            "ขายาว ขี้งอน",
                            "result_14",
                            "ขาเรียวเกี่ยวสวาท"},
                    "N", RESULT_PREFS, this);
            Utilities.saveArray(new String[]{"Elegant, Adorable, Pleasing",
                            "พุงนุ่มนิ่ม อ่อนโยน",
                            "result_15",
                            "มาร์ชเมลโล่"},
                    "O", RESULT_PREFS, this);
            Utilities.saveArray(new String[]{"Blushing Cheeks, Nice Makeup, Pretty Face",
                            "รักแร้ขาว กินจุ",
                            "result_16",
                            "วงแขนดวงจันทรา"},
                    "P", RESULT_PREFS, this);
            Utilities.saveArray(new String[]{"Long Legs, Cool Hairstyle, Fashionable style",
                            "คิ้วบาง ตลก",
                            "result_17",
                            "คิ้ว 0 มิติ"},
                    "R", RESULT_PREFS, this);
            Utilities.saveArray(new String[]{"Polite, Quiet, Modest",
                            "จมูกโด่ง ซน",
                            "result_18",
                            "พิน็อคคิโอ"},
                    "S", RESULT_PREFS, this);
            Utilities.saveArray(new String[]{"Cool Hobbies, Cool Friend, Fashionable style",
                            "ขนแขนดก ประหยัด",
                            "result_19",
                            "ขนแขนอเมซอน"},
                    "T", RESULT_PREFS, this);
            Utilities.saveArray(new String[]{"Extroverted, Easy-going, Kind",
                            "เอวบาง เข้าสังคมเก่ง",
                            "result_20",
                            "เอวเพรียวเกี่ยวใจ"},
                    "U", RESULT_PREFS, this);
            Utilities.saveArray(new String[]{"Nice Eyebrows, Elegant Hair, Funny",
                            "เท้าเหม็น ใจดี",
                            "result_21",
                            "กลิ่นบาทาปราบมาร"},
                    "V", RESULT_PREFS, this);
            Utilities.saveArray(new String[]{"Good Manner, Friendly, Cute",
                            "ฟันเหยิน จริงใจ",
                            "result_22",
                            "ฟันขูดมะพร้าว"},
                    "W", RESULT_PREFS, this);
            Utilities.saveArray(new String[]{"Young, Beautiful Mind, Kind",
                            "หัวหยิก รวย",
                            "result_23",
                            "ฝอยขัดหม้อ"},
                    "X", RESULT_PREFS, this);
            editor.putBoolean("install", false);
            editor.apply();
        }
    }

    private void createQuestionnaireDB() {

        SharedPreferences shared = getSharedPreferences(QUESTION_PREFS,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = shared.edit();
        if (shared.getBoolean("install", true)) {
            Utilities.saveArray(new String[]{"คุณคิดว่าตัวเองน่ารักรึเปล่า", "question_1"}, "Q1", QUESTION_PREFS, this);
            Utilities.saveArray(new String[]{"แน่นอน ก็ฉันน่ารักอะ", "ก็มีบ้างบางครั้งนะ", "ไม่เลย ฉันไม่คิดว่าฉันน่ารัก"}, "QC1", QUESTION_PREFS, this);
            Utilities.saveArray(new String[]{"0", "-1", "1"}, "QP1", QUESTION_PREFS, this);

            Utilities.saveArray(new String[]{"คุณแต่งหน้าบ่อยแค่ไหน", "question_2"}, "Q2", QUESTION_PREFS, this);
            Utilities.saveArray(new String[]{"แต่งทุกวันเลย", "บางครั้งก็แต่ง", "ไม่เคยเลย"}, "QC2", QUESTION_PREFS, this);
            Utilities.saveArray(new String[]{"-1", "1", "0"}, "QP2", QUESTION_PREFS, this);

            Utilities.saveArray(new String[]{"คุณออกกำลังกายบ่อยแค่ไหน", "question_3"}, "Q3", QUESTION_PREFS, this);
            Utilities.saveArray(new String[]{"ออกทุกวันนะ", "เกือบทุกวัน บางวันก็ไม่ได้ออก", "ไม่เคยเลย"}, "QC3", QUESTION_PREFS, this);
            Utilities.saveArray(new String[]{"0", "1", "-1"}, "QP3", QUESTION_PREFS, this);

            Utilities.saveArray(new String[]{"คุณโกนหนวด/เครา/ขน ของคุณบ่อยแค่ไหน", "question_4"}, "Q4", QUESTION_PREFS, this);
            Utilities.saveArray(new String[]{"ทุกวัน", "ทุกสัปดาห์", "ไม่เคย"}, "QC4", QUESTION_PREFS, this);
            Utilities.saveArray(new String[]{"-1", "0", "1"}, "QP4", QUESTION_PREFS, this);

            Utilities.saveArray(new String[]{"ลองประเมิณความน่าดึงดูดของคุณ", "question_5"}, "Q5", QUESTION_PREFS, this);
            Utilities.saveArray(new String[]{"7 - 10", "4 - 6", "0 - 3"}, "QC5", QUESTION_PREFS, this);
            Utilities.saveArray(new String[]{"1", "0", "-1"}, "QP5", QUESTION_PREFS, this);

            editor.putBoolean("install", false);
            editor.apply();
        }
    }
}
