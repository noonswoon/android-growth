package com.noonswoonapp.moonswoon;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareButton;
import com.facebook.share.widget.ShareDialog;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final String OLD_PREFS = "result_db";
    private static final String PREFS = "result_db_new";
    private static final String LANG_ENG = "0";
    private static final String LANG_THAI = "1";
    private static final String IMAGE = "2";
    private static final boolean UPDATE_DB = true;
    private UserProfile userProfile = new UserProfile();
    private CallbackManager callbackManager;
    private TextView mResultTextViewEN;
    private TextView mResultTextViewTH;
    private ImageView mProfileImage;
    private ImageView mResultImage;
    private TextView mNameTextView;
    private TextView mHeaderEn;
    private TextView mHeaderTh;
    private TextView mBecauseEn;
    private TextView mBecauseTh;
    private TextView mIsThatYouEn;
    private TextView mIsThatYouTh;
    private ShareButton mShareButton;
    private String mImageUrl;
    private String mParseId;
    private Boolean mLoggedIn = false;
    private int point;
    private Button button;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
//        loadOldDB();
        loadDB();
        Intent intent = getIntent();
        point = intent.getIntExtra("point", 0);

        setContentView(R.layout.activity_main);

        changeFontSuperMarket(mResultTextViewEN = (TextView) findViewById(R.id.view_text_result_en));
        changeFontSuperMarket(mResultTextViewTH = (TextView) findViewById(R.id.view_text_result_th));
        mShareButton = (ShareButton) findViewById(R.id.button_share);
        mProfileImage = (ImageView) findViewById(R.id.image_profile);
        changeFontSuperMarket(mNameTextView = (TextView) findViewById(R.id.view_text_name));
        mResultImage = (ImageView) findViewById(R.id.image_result);
        changeFontSuperMarket(mResultTextViewEN = (TextView) findViewById(R.id.view_text_result_en));
        changeFontSuperMarket(mResultTextViewTH = (TextView) findViewById(R.id.view_text_result_th));
        changeFontSuperMarket(mHeaderEn = (TextView) findViewById(R.id.view_text_header_en));
        changeFontSuperMarket(mHeaderTh = (TextView) findViewById(R.id.view_text_header_th));
        changeFontSuperMarket(mBecauseEn = (TextView) findViewById(R.id.view_text_because_en));
        changeFontSuperMarket(mBecauseTh = (TextView) findViewById(R.id.view_text_because_th));
        changeFontSuperMarket(mIsThatYouEn = (TextView) findViewById(R.id.view_text_isyou_en));
        changeFontSuperMarket(mIsThatYouTh = (TextView) findViewById(R.id.view_text_isyou_th));

        mShareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap bitmap = takeLayoutScreenshot();
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] image = stream.toByteArray();
                final ParseFile mFile = new ParseFile("ShareImage.png", image);

                final ProgressDialog mProgressDialog = new ProgressDialog(MainActivity.this);
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                mProgressDialog.setMessage("Sharing Image...");
                mProgressDialog.setIndeterminate(true);
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();
                mFile.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            Log.e("Upload result image:", "Success");
                            mImageUrl = mFile.getUrl();
                            ParseQuery<ParseObject> query = ParseQuery.getQuery("UserProfile");
                            query.getInBackground(mParseId, new GetCallback<ParseObject>() {
                                @Override
                                public void done(ParseObject parseObject, ParseException e) {
                                    if (e == null) {
                                        Log.e("Get ParseObject", "Success");

                                        parseObject.put("ImageFile", mFile);
                                        parseObject.saveInBackground(new SaveCallback() {
                                            @Override
                                            public void done(ParseException e) {
                                                if (e == null) {
                                                    Log.e("Update:", "Success");

                                                    shareLinkContent(mProgressDialog);
                                                } else {
                                                    Log.e("Update:", "Failed");
                                                    mProgressDialog.dismiss();
                                                }
                                            }
                                        });
                                    } else {
                                        Log.e("Get ParseObject", "Failed");
                                        mProgressDialog.dismiss();
                                    }
                                }
                            });
                        } else {
                            Log.e("Upload result image:", "Failed");
                            Log.e("ERROR:", String.valueOf(e));
                            mProgressDialog.dismiss();
                        }
                    }
                });
            }
        });

        final AccessTokenTracker accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken,
                                                       AccessToken currentAccessToken) {
                if (currentAccessToken == null) {
                    mResultTextViewEN.setText(null);
                    mNameTextView.setText(null);
                    mProfileImage.setImageBitmap(null);
                    mShareButton.setVisibility(View.INVISIBLE);
                    mLoggedIn = false;
                }
            }
        };
        if (AccessToken.getCurrentAccessToken() == null) {
            LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile, email, user_likes, user_photos, user_birthday, email"));
            LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    getUserProfile();
                    mLoggedIn = true;
                    accessTokenTracker.startTracking();
                    Log.e("Login Result:", "Login Success");
                }

                @Override
                public void onCancel() {
                    Log.e("Login Result:", "Login Cancel");
                }

                @Override
                public void onError(FacebookException e) {
                    Log.e("Login Result:", "Login Error");
                }
            });
        }
    }

    private void changeFontSuperMarket(TextView textView) {
        Typeface font = Typeface.createFromAsset(getAssets(), "font/supermarket.ttf");
        textView.setTypeface(font);
    }

    private void shareLinkContent(final ProgressDialog progressDialog) {
        ShareDialog shareDialog = new ShareDialog(MainActivity.this);
        ShareLinkContent content = new ShareLinkContent.Builder()
                .setContentDescription("Find out and share your fabulousness with your friends!")
                .setContentTitle("Why do people like you?")
                .setImageUrl(Uri.parse(mImageUrl))
                .setContentUrl(Uri.parse("https://noonswoonapp.com/"))
                .build();
        shareDialog.show(content);
        shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {
            @Override
            public void onSuccess(Sharer.Result result) {
                progressDialog.dismiss();
                showAds();
                Log.e("Share Result:", "Share Success");
            }

            @Override
            public void onCancel() {
                progressDialog.dismiss();
                showAds();
                Log.e("Share Result:", "Share Cancel");
            }

            @Override
            public void onError(FacebookException e) {
                progressDialog.dismiss();
                showAds();
                Log.e("Share Result:", "Share Error");
            }
        });
    }


    private void showAds() {
        final Dialog dialog = new Dialog(MainActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_noonswoon);

        Button buttonDismiss = (Button) dialog.findViewById(R.id.button_dismiss);
        ImageButton imageAds = (ImageButton) dialog.findViewById(R.id.image_ads);

        buttonDismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Intent intent = new Intent(MainActivity.this, Retry.class);
                startActivity(intent);
            }
        });

        imageAds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("market://details?id=com.android.chrome"));
                startActivity(intent);
                dialog.dismiss();
            }
        });
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void sendUserProfile() {
        final ParseObject mUser = new ParseObject("UserProfile");
        JSONObject profile = userProfile.getUserProfile();
        try {
            mUser.put("FirstName", profile.getString("first_name"));
            mUser.put("LastName", profile.getString("last_name"));
            mUser.put("Email", profile.getString("email"));
            mUser.put("BirthDate", profile.getString("birthday"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mUser.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.e("Send user profile:", "Success");
                    mParseId = mUser.getObjectId();
                } else {
                    Log.e("Send user profile:", "Failed");
                }
            }
        });
    }

    public Bitmap takeLayoutScreenshot() {
        RelativeLayout view = (RelativeLayout) findViewById(R.id.layout_result);
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        return view.getDrawingCache();
    }

    public Bitmap takeScreenshot() {
        View rootView = findViewById(android.R.id.content).getRootView();
        rootView.setDrawingCacheEnabled(true);
        return rootView.getDrawingCache();
    }

    private void getResult() {
        ArrayList<String> categories = new ArrayList<>();
        String category = "null";
        int n = 0;
        for (String s : userProfile.getCategory()) {
            categories.add(s.substring(0, 1));
        }
        for (int i = 65; i <= 87; i++) {
            int z = Collections.frequency(categories, Character.toString((char) i));
            if (z > n) {
                n = z;
                int total = i + point;
                if (total < 65) {
                    total = 87 - (64 - total);
                } else if (total > 87) {
                    total = (total - 88) + 65;
                } else if (total == 81){
                    if (point >= 0) {
                        total++;
                    } else {
                        total--;
                    }
                }
                category = String.valueOf((char)total);
            }
        }
        if (category.equals("C")) {
            n = 0;
            int c = 0;
            Set<String> hashsetList = new HashSet<>(userProfile.getCategory());
            for (String s : hashsetList) {
                if (s.substring(0, 1).equals("C")) {
                    int i = Collections.frequency(userProfile.getCategory(), s);
                    Log.e("Category:", String.format("%s Count: %d", s, i));
                    if (i > n) {
                        n = i;
                    }
                    if (s.contains("Community")) {
                        c = c + i;
                        if (c > n) {
                            category = String.format("%s%d", "C", (i + 4) / 5 * 5);
                            Log.e("Test:", String.format("%s", category));
                        }
                        Log.e("Community Count:", String.format("%d", c));
                    }
                }
            }
        }

        SharedPreferences shared = getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        mResultTextViewEN.setText(shared.getString(category + "_" + LANG_ENG, shared.getString("null_" + LANG_ENG, "null_" + LANG_ENG)));
        mResultTextViewTH.setText(shared.getString(category + "_" + LANG_THAI, shared.getString("null_" + LANG_THAI, "null_" + LANG_THAI)));
        Log.e("Thai:", shared.getString(category + "_" + LANG_THAI, shared.getString("null_" + LANG_THAI, "null_" + LANG_THAI)));
        String mDrawableName = shared.getString(category + "_" + IMAGE, shared.getString("null_" + IMAGE, "null_" + IMAGE));
        int resID = getResources().getIdentifier(mDrawableName, "drawable", getPackageName());
        mResultImage.setImageResource(resID);
        mShareButton.setVisibility(View.VISIBLE);
    }

    private void getUserProfile() {
        GraphRequest request = GraphRequest.newMeRequest(
                AccessToken.getCurrentAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(
                            JSONObject object,
                            GraphResponse response) {
                        ArrayList<String> category = new ArrayList<>();
                        try {
                            JSONObject likes = object.getJSONObject("likes");
                            JSONArray data = likes.getJSONArray("data");
                            for (int i = 0; !data.getJSONObject(i).getString("category").isEmpty(); i++) {
                                category.add(data.getJSONObject(i).getString("category"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        try {
                            mNameTextView.setText(object.getString("first_name") + " " + object.getString("last_name"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        try {
                            JSONObject picture = object.getJSONObject("picture");
                            JSONObject data = picture.getJSONObject("data");
                            Log.e("JSON: ", data.getString("url"));
                            Log.e("JSON: ", "https://graph.facebook.com/" + object.getString("id") + "/picture?type=large");
                            Picasso.with(MainActivity.this).load(data.getString("url")).transform(new RoundedTransformation(100, 0)).into(mProfileImage);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        userProfile.setUserProfile(object);
                        userProfile.setCategory(category);
                        getResult();
                        sendUserProfile();
                        // Application code
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id, first_name,last_name, picture.width(200).height(200), likes.limit(100), birthday, email");
        request.setParameters(parameters);
        request.executeAsync();
    }


    public class RoundedTransformation implements com.squareup.picasso.Transformation {
        private final int radius;
        private final int margin;  // dp

        // radius is corner radii in dp
        // margin is the board in dp
        public RoundedTransformation(final int radius, final int margin) {
            this.radius = radius;
            this.margin = margin;
        }

        @Override
        public Bitmap transform(final Bitmap source) {

            final Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setShader(new BitmapShader(source, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));

            Bitmap output = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(output);
            canvas.drawCircle((source.getWidth() - margin) / 2, (source.getHeight() - margin) / 2, radius - 2, paint);

            if (source != output) {
                source.recycle();
            }

            Paint paint1 = new Paint();
            paint1.setColor(Color.BLACK);
            paint1.setStyle(Paint.Style.STROKE);
            paint1.setAntiAlias(true);
            paint1.setStrokeWidth(5);
            canvas.drawCircle((source.getWidth() - margin) / 2, (source.getHeight() - margin) / 2, radius - 2, paint1);


            return output;
        }

        @Override
        public String key() {
            return "rounded";
        }
    }

    private void loadDB() {
        SharedPreferences shared = getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = shared.edit();
        if (shared.getBoolean("install", true) || UPDATE_DB) {
            Utilities.saveArray(new String[]{"Beautiful eyes, Cute smile, Silky hair",
                            "ตาสวย ยิ้มสวย ผมนุ่มลื่น",
                            "result_1"},
                    "A", PREFS, MainActivity.this);
            Utilities.saveArray(new String[]{"Nice skin, Good-looking, Adorable",
                            "ดูดี ผิวสวย น่ารัก",
                            "result_2"},
                    "B", PREFS, MainActivity.this);
            Utilities.saveArray(new String[]{"Sexy, Charming, Attractive",
                            "เซ็กซี่ มีเสน่ห์ น่าดึงดูดٴ",
                            "result_3"},
                    "C", PREFS, MainActivity.this);
            Utilities.saveArray(new String[]{"Glamorous lips, Perfect body, Nice attitude",
                            "ริมฝีปากน่ามอง หุ่นดี ทัศนคติดี",
                            "result_4"},
                    "D", PREFS, MainActivity.this);
            Utilities.saveArray(new String[]{"Slim body, Skinny Legs, Beautiful mind",
                            "หุ่นบาง ขาเรียว จิตใจงาม",
                            "result_5"},
                    "E", PREFS, MainActivity.this);
            Utilities.saveArray(new String[]{"Homely looking, Fine hairstyle, Positive attitude",
                            "หน้าตาบ้านๆ ทรงผมดูไม่เลว คิดแง่บวก",
                            "result_6"},
                    "F", PREFS, MainActivity.this);
            Utilities.saveArray(new String[]{"Friendly, Good-humored, Nice smile",
                            "ซุกซน ทะเล้น มีความขำขัน",
                            "result_7"},
                    "G", PREFS, MainActivity.this);
            Utilities.saveArray(new String[]{"Nice legs, Curvy body, Easy-going",
                            "ขาสวย หุ่นโค้งได้สัดส่วน เข้ากับคนอื่นง่าย",
                            "result_8"},
                    "H", PREFS, MainActivity.this);
            Utilities.saveArray(new String[]{"Chubby, Good-humoured, Friendly",
                            "จ้ำม่ำ มีความขบขัน เป็นกันเองͧ",
                            "result_9"},
                    "I", PREFS, MainActivity.this);
            Utilities.saveArray(new String[]{"Skinny, Funny, Have Sense of Humor",
                            "ผอมเพรียว ตลก มีความขำขัน",
                            "result_10"},
                    "J", PREFS, MainActivity.this);
            Utilities.saveArray(new String[]{"Adorable Dimples, Round Face, Fine Skin",
                            "ลักยิ้มน่ารัก หน้ากลม ผิวดี",
                            "result_11"},
                    "K", PREFS, MainActivity.this);
            Utilities.saveArray(new String[]{"Cheeky, Funny, Attractive",
                            "ทะเล้น ตลก น่าดึงดูด",
                            "result_12"},
                    "L", PREFS, MainActivity.this);
            Utilities.saveArray(new String[]{"Rich, Generous, Beautiful face",
                            "รวย ใจกว้าง ใบหน้าสวย",
                            "result_13"},
                    "M", PREFS, MainActivity.this);
            Utilities.saveArray(new String[]{"Not Aged, Smooth Skin, No Wrinkles",
                            "ดูไม่แก่ ผิวเนียนนุ่ม ไม่มีรอยตีนกา",
                            "result_14"},
                    "N", PREFS, MainActivity.this);
            Utilities.saveArray(new String[]{"Elegant, Adorable, Pleasing",
                            "สง่า น่ารัก น่าดึงดูดٴ",
                            "result_15"},
                    "O", PREFS, MainActivity.this);
            Utilities.saveArray(new String[]{"Blushing Cheeks, Nice Makeup, Pretty Face",
                            "แก้มแดง แต่งหน้าดี ใบหน้าสวย",
                            "result_16"},
                    "P", PREFS, MainActivity.this);
            Utilities.saveArray(new String[]{"Long Legs, Cool Hairstyle, Fashionable style",
                            "ขายาว ทรงผมดูดี แต่งตัวทันสมัย",
                            "result_17"},
                    "R", PREFS, MainActivity.this);
            Utilities.saveArray(new String[]{"Polite, Quiet, Modest",
                            "สุภาพ เงียบ มีความถ่อมตน",
                            "result_18"},
                    "S", PREFS, MainActivity.this);
            Utilities.saveArray(new String[]{"Cool Hobbies, Cool Friend, Fashionable style",
                            "จ่ำม่ำ พุงนุ่มนิ่ม เสียงน่ารัก",
                            "result_19"},
                    "T", PREFS, MainActivity.this);
            Utilities.saveArray(new String[]{"Extroverted, Easy-going, Kind",
                            "เข้าสังคมเก่ง สบายๆ ใจดี",
                            "result_20"},
                    "U", PREFS, MainActivity.this);
            Utilities.saveArray(new String[]{"Nice Eyebrows, Elegant Hair, Funny",
                            "ขยัน ฉลาด มีความจริงใจ",
                            "result_21"},
                    "V", PREFS, MainActivity.this);
            Utilities.saveArray(new String[]{"Good Manner, Friendly, Cute",
                            "มารยาทดี เป็นกันเอง น่ารัก",
                            "result_22"},
                    "W", PREFS, MainActivity.this);
            Utilities.saveArray(new String[]{"Young, Beautiful Mind, Kind",
                            "ดูเด็ก จิตใจงาม ใจดี",
                            "result_23"},
                    "null", PREFS, MainActivity.this);
            Utilities.saveArray(new String[]{"Fashionable Style, Nice Makeup, Rich",
                            "แต่งตัวทันสมัย แต่งหน้าดี รวย",
                            "result_3"},
                    "C5", PREFS, MainActivity.this);
            Utilities.saveArray(new String[]{"Chubby, Soft Belly, Cute Voice",
                            "จ่ำม่ำ พุงนุ่มนิ่ม เสียงน่ารัก",
                            "result_3"},
                    "C10", PREFS, MainActivity.this);
            Utilities.saveArray(new String[]{"Fabulous, Hot body, Easy-going",
                            "เป็นคนดีมากๆ หุ่นดีมาก สบายๆ",
                            "result_3"},
                    "C15", PREFS, MainActivity.this);
            Utilities.saveArray(new String[]{"Naughty, Cheeky, Good-humoured",
                            "ซุกซน ทะเล้น มีความขำขัน",
                            "result_3"},
                    "C20", PREFS, MainActivity.this);
            Utilities.saveArray(new String[]{"Hard-working, Intelligent, Sincerity",
                            "ขยัน ฉลาด มีความจริงใจ",
                            "result_3"},
                    "C25", PREFS, MainActivity.this);
            editor.putBoolean("install", false);
            editor.apply();
        }
    }

    private void loadOldDB() {
        SharedPreferences shared = getSharedPreferences(OLD_PREFS,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = shared.edit();
        if (shared.getBoolean("install", true) || UPDATE_DB) {
            Utilities.saveArray(new String[]{"You've got beautiful eyes, cute smile and silky hair.",
                            "คุณมีดวงตาที่สวยงาม มีรอยยิ้มที่น่ารัก และมีผมที่นุ่มสลวย"},
                    "A", PREFS, MainActivity.this);
            Utilities.saveArray(new String[]{"You've got a nice skin, very good looking and adorable.",
                            "คุณมีผิวพรรณที่ดี เป็นคนที่ดูดีและน่ารัก"},
                    "B", PREFS, MainActivity.this);
            Utilities.saveArray(new String[]{"You're so sexy that you make the others blush when they look at you.",
                            "คุณมีความเซ็กซี่มากจนกระทั่ง ทำให้คนอื่นหน้าแดง เมื่อพวกเขามองมายังคุณ"},
                    "C", PREFS, MainActivity.this);
            Utilities.saveArray(new String[]{"You have glamorous lips and perfect body",
                            "คุณมีริมฝีปากที่สวย และรูปร่างอันเพอร์เฟ็ค"},
                    "D", PREFS, MainActivity.this);
            Utilities.saveArray(new String[]{"You've slim body and skinny legs. Also your mind is beautiful.",
                            "คุณมีรูปร่างบางและขาเรียวสวย นอกจากนี้จิตใจคุณยังงดงามอีกด้วย"},
                    "E", PREFS, MainActivity.this);
            Utilities.saveArray(new String[]{"You're homely looking but your possitive attitude make people like you.",
                            "หน้าตาของคุณก็งั้นๆแหละ แต่คุณเป็นคนคิดบวกเลยทำให้ผู้คนชอบคุณ"},
                    "F", PREFS, MainActivity.this);
            Utilities.saveArray(new String[]{"You're friendly, good-humored that make everyone wants to be with you.",
                            "คุณเป็นคนที่เป็นกันเอง มีความขบขัน ทำให้ทุกๆคนอยากจะเข้ามาอยู่ใกล้ๆคุณ"},
                    "G", PREFS, MainActivity.this);
            Utilities.saveArray(new String[]{"You've got nice legs, curvy body and you're easy-going with everyone.",
                            "คุณมีขาที่สวย รูปร่างดี และเข้ากับคนอื่นได้ทุกๆคน"},
                    "H", PREFS, MainActivity.this);
            Utilities.saveArray(new String[]{"You're so stunning. You make others jaw drop when they look at you.",
                            "คุณสวยมาก! สวยจนกระทั่งทำให้คนอื่นอ้าปากค้างเมื่อพวกเขามองคุณ"},
                    "I", PREFS, MainActivity.this);
            Utilities.saveArray(new String[]{"You're funny person. You have sense of humor. Everyone wants to be close with you.",
                            "คุณเป็นคนตลก มีความขำขัน จนทุกๆคนอยากจะอยู่ใกล้ๆกับคุณ"},
                    "J", PREFS, MainActivity.this);
            Utilities.saveArray(new String[]{"You've got adorable dimples. Your smile melt the other hearts.",
                            "คุณมีลักยิ้มที่น่ารัก รอยยิ้มของคุณละลายใจของทุกๆคน"},
                    "K", PREFS, MainActivity.this);
            Utilities.saveArray(new String[]{"You're a little bit cheeky. your humor always make people laugh.",
                            "คุณมีความทะเล้น มุกตลกของคุณทำให้คนอื่นขำได้เสมอ"},
                    "L", PREFS, MainActivity.this);
            Utilities.saveArray(new String[]{"You're rich and generous. That's why people want to be with you.",
                            "คุณรวยและเป็นคนใจกว้าง นี่แหละคือสาเหตุที่ผู้คนอยากอยู่ใกล้คุณ"},
                    "M", PREFS, MainActivity.this);
            Utilities.saveArray(new String[]{"You're never look aged. You skin is very nice and you have no wrinkles.",
                            "คุณดูไม่แก่ลงไปเลย ผิวพรรณของคุณนั้นดีเยี่ยม ไม่มีแม้กระทั่งรอยตีนกา"},
                    "N", PREFS, MainActivity.this);
            Utilities.saveArray(new String[]{"You look very elegant yet adorable. Everyone wants to be with you.",
                            "คุณเป็นคนที่มีความสง่างาม แต่ยังมีความน่ารัก ทำให้ทุกๆคนอยากอยู่กับคุณ"},
                    "O", PREFS, MainActivity.this);
            Utilities.saveArray(new String[]{"You've got blushing cheeks. Everyone can fall in love with you easily when they see you.",
                            "คุณมีแก้มที่แดงสวย ทุกๆคนสามารถตกหลุมรักคุณได้อย่างง่ายดายเมื่อเขาเห็นคุณ"},
                    "P", PREFS, MainActivity.this);
            Utilities.saveArray(new String[]{"You're very attractive person and your curvy body make people like you.",
                            "คุณเป็นคนที่มีเสน่ห์ดึงดูดมาก รูปร่างอันงดงามของคุณ ทำให้ผู้คนชอบคุณ"},
                    "R", PREFS, MainActivity.this);
            Utilities.saveArray(new String[]{"You're polite, quiet and modest. When people are with you, they're happy.",
                            "คุณเป็นคนสุภาพ เงียบ และเจียมเนื้อเจียมตัว เมื่อคนอื่นอยู่กับคุณ พวกเขารู้สึกมีความสุข"},
                    "S", PREFS, MainActivity.this);
            Utilities.saveArray(new String[]{"You like adventurous activities and love to visit exciting places.",
                            "คุณเป็นคนที่ชอบกิจกรรมผจญภัย และชอบที่จะไปเยี่ยมชมสถานที่น่าตื่นตาตื่นใจ"},
                    "T", PREFS, MainActivity.this);
            Utilities.saveArray(new String[]{"You're extroverted person. Easy-going with everyone and you're kind. That's why people like you.",
                            "คุณเป็นคนเข้าสังคมเก่ง เข้ากับคนอื่นได้ทุกคน และคุณยังใจดีอีกต่างหาก นั่นคือเหตุผลที่ทำไมผู้คนถึงชอบคุณ"},
                    "U", PREFS, MainActivity.this);
            Utilities.saveArray(new String[]{"You've nice eyebrows, pretty lips and elegant hair. Moreover you're funny.",
                            "คุณมีคิ้วที่ดูดี มีริมฝีปากที่รูปร่างสวย และมีผมที่นุ่มสลวย นอกจากนี้คุณยังเป็นคนที่มีความตลกอีกด้วย"},
                    "V", PREFS, MainActivity.this);
            Utilities.saveArray(new String[]{"You've good manner, friendly yet sometimes you're a little bit naughty and cute.",
                            "คุณเป็นคนที่มีมารยาทดี มีความเป็นกันเอง บางครั้งคุณก็ซุกซนนิดหน่อย และคุณก็น่ารักด้วย"},
                    "W", PREFS, MainActivity.this);
            Utilities.saveArray(new String[]{"You're young and have beautiful mind. You're kind to everyone.",
                            "คุณดูเด็ก และมีจิตใจที่ดี คุณใจดีกับทุกๆคน"},
                    "null", PREFS, MainActivity.this);
            Utilities.saveArray(new String[]{"Your dressing style is so fashionable. You also have nice make up.",
                            "คุณเป็นคนที่มีสไตล์การแต่งตัวทันสมัย และการแต่งหน้าของคุณก็ดูดีอีกด้วย"},
                    "C5", PREFS, MainActivity.this);
            Utilities.saveArray(new String[]{"You're a bit chubby, your belly is so soft. That�s why people like you.",
                            "คุณออกจะจ้ำม่ำเล็กน้อย มีพุงนุ่มนิ่ม เป็นเหตุให้ผู้คนชอบคุณ"},
                    "C10", PREFS, MainActivity.this);
            Utilities.saveArray(new String[]{"You're so fabulous and good to everyone. You're so nice that many people like you.",
                            "คุณเป็นคนที่ดีเหลือเกิน ดีกับทุกๆคน จนทำให้คนหลายๆคนชอบในตัวคุณ"},
                    "C15", PREFS, MainActivity.this);
            Utilities.saveArray(new String[]{"Your smile can melt people heart and your body has very good shape.",
                            "รอยยิ้มของคุณละลายใจของผู้คน และคุณยังมีรูปร่างที่ดีอีกด้วย"},
                    "C20", PREFS, MainActivity.this);
            Utilities.saveArray(new String[]{"You're not that good-looking person, not so beautiful. But your sincerity is the reason why people like you.",
                            "คุณไม่ได้ดูดีอะไรขนาดนั้น ไม่ได้สวยมาก แต่ความจริงใจของคุณเป็นสาเหตุให้ผู้คนชอบคุณ"},
                    "C25", PREFS, MainActivity.this);
            editor.putBoolean("install", false);
            editor.apply();
        }
    }

    public String[] loadArray(String arrayName, Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences(PREFS, 0);
        int size = prefs.getInt(arrayName + "_size", 0);
        String array[] = new String[size];
        for (int i = 0; i < size; i++)
            array[i] = prefs.getString(arrayName + "_" + i, null);
        return array;
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppEventsLogger.activateApp(this);
        if (AccessToken.getCurrentAccessToken() != null && !mLoggedIn) {
            mLoggedIn = true;
            getUserProfile();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        AppEventsLogger.deactivateApp(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);

    }

}
