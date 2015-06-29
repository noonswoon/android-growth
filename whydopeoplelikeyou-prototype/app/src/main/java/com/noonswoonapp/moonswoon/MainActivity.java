package com.noonswoonapp.moonswoon;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Shader;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
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

    private static final String MY_PREFS = "result_db";
    private static final String LANG_ENG = "0";
    private static final String LANG_THAI = "1";
    private UserProfile userProfile = new UserProfile();
    private CallbackManager callbackManager;
    private TextView mResultTextView;
    private ImageView mProfileImage;
    private TextView mNameTextView;
    private ShareButton mShareButton;
    private String mImageUrl;
    private String mParseId;
    private ShareDialog mShareDialog;
    private Boolean mLoggedIn = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        createDB();

        setContentView(R.layout.activity_main);

        mResultTextView = (TextView) findViewById(R.id.view_text_result);
        mProfileImage = (ImageView) findViewById(R.id.image_profile);
        mNameTextView = (TextView) findViewById(R.id.view_text_name);
        mShareButton = (ShareButton) findViewById(R.id.button_share);

        mShareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap bitmap = takeScreenshot();
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] image = stream.toByteArray();
                final ParseFile mFile = new ParseFile("ShareImage.png", image);
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
                                                    shareLinkContent();
                                                } else {
                                                    Log.e("Update:", "Failed");
                                                }
                                            }
                                        });
                                    } else {
                                        Log.e("Get ParseObject", "Failed");
                                    }
                                }
                            });
                        } else {
                            Log.e("Upload result image:", "Failed");
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
                    mResultTextView.setText(null);
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

    private void shareLinkContent() {
        mShareDialog = new ShareDialog(MainActivity.this);
        ShareLinkContent content = new ShareLinkContent.Builder()
                .setContentDescription("Test : setContentDescription")
                .setContentTitle("Test : setContentTitle")
                .setImageUrl(Uri.parse(mImageUrl))
                .setContentUrl(Uri.parse("https://developers.facebook.com"))
                .build();
        mShareDialog.show(content);
        mShareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {
            @Override
            public void onSuccess(Sharer.Result result) {
                showAds();
                Log.e("Share Result:", "Share Success");
            }

            @Override
            public void onCancel() {
                Log.e("Share Result:", "Share Cancel");
            }

            @Override
            public void onError(FacebookException e) {
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

    public Bitmap takeScreenshot() {
        View rootView = findViewById(android.R.id.content).getRootView();
        rootView.setDrawingCacheEnabled(true);
        return rootView.getDrawingCache();
    }

    private void getResult(TextView result) {
        mShareButton = (ShareButton) findViewById(R.id.button_share);
        mShareButton.setVisibility(View.VISIBLE);
        ArrayList<String> categories = new ArrayList<>();
        String category = "null";
        int n = 0;
        for (String s : userProfile.getCategory()) {
            categories.add(s.substring(0, 1));
        }
        for (int i = 65; i <= 90; i++) {
            int z = Collections.frequency(categories, Character.toString((char) i));
            if (z > n) {
                n = z;
                category = String.valueOf((char) i);
            }
        }
        if (category.equals("C")) {
            n = 0;
            int c = 0;
            Set<String> hashsetList = new HashSet<String>(userProfile.getCategory());
            for (String s : hashsetList) {
                if (s.substring(0,1).equals("C")) {
                    int i = Collections.frequency(userProfile.getCategory(), s);
                    Log.e("Category:", String.format("%s Count: %d", s, i));
                    if (i > n){
                        n = i;
                    }
                    if (s.contains("Community")) {
                        c = c + i;
                        if(c > n) {
                            category = String.format("%s%d", "C", (i + 4) / 5 * 5);
                            Log.e("Test:", String.format("%s", category));
                        }
                        Log.e("Community Count:", String.format("%d",c));
                    }
                }
            }
        }
        SharedPreferences shared = getSharedPreferences(MY_PREFS,
                Context.MODE_PRIVATE);
        result.setText(shared.getString(category + "_" + LANG_ENG, shared.getString("null", "null")));
    }

    private void getUserProfile() {
        mProfileImage = (ImageView) findViewById(R.id.image_profile);
        mNameTextView = (TextView) findViewById(R.id.view_text_name);
        mResultTextView = (TextView) findViewById(R.id.view_text_result);
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
                            mNameTextView.setText(String.format("People Like\n%s %s Because", object.getString("first_name"), object.getString("last_name")));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        try {
                            JSONObject picture = object.getJSONObject("picture");
                            JSONObject data = picture.getJSONObject("data");
                            Log.e("JSON: ", data.getString("url"));
                            Log.e("JSON: ", "https://graph.facebook.com/" + object.getString("id") + "/picture?type=large");
                            Picasso.with(MainActivity.this).load(data.getString("url")).transform(new RoundedTransformation(160, 0)).into(mProfileImage);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        userProfile.setUserProfile(object);
                        userProfile.setCategory(category);
                        getResult(mResultTextView);
                        sendUserProfile();
                        // Application code
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id, first_name,last_name, picture.width(320).height(320), likes.limit(100), birthday, email");
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


    private void createDB() {
        SharedPreferences shared = getSharedPreferences(MY_PREFS,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = shared.edit();
        if (shared.getBoolean("runfirsttime", true)) {
            saveArray(new String[]{"You've got beautiful eyes, cute smile and silky hair.",
                            "คุณมีดวงตาที่สวยงาม มีรอยยิ้มที่น่ารัก และมีผมที่นุ่มสลวย"},
                    "A", MainActivity.this);
            saveArray(new String[]{"You've got a nice skin, very good looking and adorable.",
                            "คุณมีผิวพรรณที่ดี เป็นคนที่ดูดีและน่ารัก"},
                    "B", MainActivity.this);
            saveArray(new String[]{"You're so sexy that you make the others blush when they look at you.",
                            "คุณมีความเซ็กซี่มากจนกระทั่ง ทำให้คนอื่นหน้าแดง เมื่อพวกเขามองมายังคุณ"},
                    "C", MainActivity.this);
            saveArray(new String[]{"You have glamorous lips and perfect body",
                            "คุณมีริมฝีปากที่สวย และรูปร่างอันเพอร์เฟ็ค"},
                    "D", MainActivity.this);
            saveArray(new String[]{"You've slim body and skinny legs. Also your mind is beautiful.",
                            "คุณมีรูปร่างบางและขาเรียวสวย นอกจากนี้จิตใจคุณยังงดงามอีกด้วย"},
                    "E", MainActivity.this);
            saveArray(new String[]{"You're homely looking but your possitive attitude make people like you.",
                            "หน้าตาของคุณก็งั้นๆแหละ แต่คุณเป็นคนคิดบวกเลยทำให้ผู้คนชอบคุณ"},
                    "F", MainActivity.this);
            saveArray(new String[]{"You're friendly, good-humored that make everyone wants to be with you.",
                            "คุณเป็นคนที่เป็นกันเอง มีความขบขัน ทำให้ทุกๆคนอยากจะเข้ามาอยู่ใกล้ๆคุณ"},
                    "G", MainActivity.this);
            saveArray(new String[]{"You've got nice legs, curvy body and you're easy-going with everyone.",
                            "คุณมีขาที่สวย รูปร่างดี และเข้ากับคนอื่นได้ทุกๆคน"},
                    "H", MainActivity.this);
            saveArray(new String[]{"You're so stunning. You make others jaw drop when they look at you.",
                            "คุณสวยมาก! สวยจนกระทั่งทำให้คนอื่นอ้าปากค้างเมื่อพวกเขามองคุณ"},
                    "I", MainActivity.this);
            saveArray(new String[]{"You're funny person. You have sense of humor. Everyone wants to be close with you.",
                            "คุณเป็นคนตลก มีความขำขัน จนทุกๆคนอยากจะอยู่ใกล้ๆกับคุณ"},
                    "J", MainActivity.this);
            saveArray(new String[]{"You've got adorable dimples. Your smile melt the other hearts.",
                            "คุณมีลักยิ้มที่น่ารัก รอยยิ้มของคุณละลายใจของทุกๆคน"},
                    "K", MainActivity.this);
            saveArray(new String[]{"You're a little bit cheeky. your humor always make people laugh.",
                            "คุณมีความทะเล้น มุกตลกของคุณทำให้คนอื่นขำได้เสมอ"},
                    "L", MainActivity.this);
            saveArray(new String[]{"You're rich and generous. That's why people want to be with you.",
                            "คุณรวยและเป็นคนใจกว้าง นี่แหละคือสาเหตุที่ผู้คนอยากอยู่ใกล้คุณ"},
                    "M", MainActivity.this);
            saveArray(new String[]{"You're never look aged. You skin is very nice and you have no wrinkles.",
                            "คุณดูไม่แก่ลงไปเลย ผิวพรรณของคุณนั้นดีเยี่ยม ไม่มีแม้กระทั่งรอยตีนกา"},
                    "N", MainActivity.this);
            saveArray(new String[]{"You look very elegant yet adorable. Everyone wants to be with you.",
                            "คุณเป็นคนที่มีความสง่างาม แต่ยังมีความน่ารัก ทำให้ทุกๆคนอยากอยู่กับคุณ"},
                    "O", MainActivity.this);
            saveArray(new String[]{"You've got blushing cheeks. Everyone can fall in love with you easily when they see you.",
                            "คุณมีแก้มที่แดงสวย ทุกๆคนสามารถตกหลุมรักคุณได้อย่างง่ายดายเมื่อเขาเห็นคุณ"},
                    "P", MainActivity.this);
            saveArray(new String[]{"You're very attractive person and your curvy body make people like you.",
                            "คุณเป็นคนที่มีเสน่ห์ดึงดูดมาก รูปร่างอันงดงามของคุณ ทำให้ผู้คนชอบคุณ"},
                    "R", MainActivity.this);
            saveArray(new String[]{"You're polite, quiet and modest. When people are with you, they're happy.",
                            "คุณเป็นคนสุภาพ เงียบ และเจียมเนื้อเจียมตัว เมื่อคนอื่นอยู่กับคุณ พวกเขารู้สึกมีความสุข"},
                    "S", MainActivity.this);
            saveArray(new String[]{"You like adventurous activities and love to visit exciting places.",
                            "คุณเป็นคนที่ชอบกิจกรรมผจญภัย และชอบที่จะไปเยี่ยมชมสถานที่น่าตื่นตาตื่นใจ"},
                    "T", MainActivity.this);
            saveArray(new String[]{"You're extroverted person. Easy-going with everyone and you're kind. That's why people like you.",
                            "คุณเป็นคนเข้าสังคมเก่ง เข้ากับคนอื่นได้ทุกคน และคุณยังใจดีอีกต่างหาก นั่นคือเหตุผลที่ทำไมผู้คนถึงชอบคุณ"},
                    "U", MainActivity.this);
            saveArray(new String[]{"You've nice eyebrows, pretty lips and elegant hair. Moreover you're funny.",
                            "คุณมีคิ้วที่ดูดี มีริมฝีปากที่รูปร่างสวย และมีผมที่นุ่มสลวย นอกจากนี้คุณยังเป็นคนที่มีความตลกอีกด้วย"},
                    "V", MainActivity.this);
            saveArray(new String[]{"You've good manner, friendly yet sometimes you're a little bit naughty and cute.",
                            "คุณเป็นคนที่มีมารยาทดี มีความเป็นกันเอง บางครั้งคุณก็ซุกซนนิดหน่อย และคุณก็น่ารักด้วย"},
                    "W", MainActivity.this);
            saveArray(new String[]{"You're young and have beautiful mind. You're kind to everyone.",
                            "คุณดูเด็ก และมีจิตใจที่ดี คุณใจดีกับทุกๆคน"},
                    "null", MainActivity.this);
            saveArray(new String[]{"Your dressing style is so fashionable. You also have nice make up.",
                            "คุณเป็นคนที่มีสไตล์การแต่งตัวทันสมัย และการแต่งหน้าของคุณก็ดูดีอีกด้วย"},
                    "C5", MainActivity.this);
            saveArray(new String[]{"You’re a bit chubby, your belly is so soft. That’s why people like you.",
                            "คุณออกจะจ้ำม่ำเล็กน้อย มีพุงนุ่มนิ่ม เป็นเหตุให้ผู้คนชอบคุณ"},
                    "C10", MainActivity.this);
            saveArray(new String[]{"You’re so fabulous and good to everyone. You’re so nice that many people like you.",
                            "คุณเป็นคนที่ดีเหลือเกิน ดีกับทุกๆคน จนทำให้คนหลายๆคนชอบในตัวคุณ"},
                    "C15", MainActivity.this);
            saveArray(new String[]{"Your smile can melt people’ heart and your body has very good shape.",
                            "รอยยิ้มของคุณละลายใจของผู้คน และคุณยังมีรูปร่างที่ดีอีกด้วย"},
                    "C20", MainActivity.this);
            saveArray(new String[]{"You’re not that good-looking person, not so beautiful. But your sincerity is the reason why people like you.",
                            "คุณไม่ได้ดูดีอะไรขนาดนั้น ไม่ได้สวยมาก แต่ความจริงใจของคุณเป็นสาเหตุให้ผู้คนชอบคุณ"},
                    "C25", MainActivity.this);
            editor.putBoolean("runfirsttime", false);
            editor.apply();
        }
    }

    public boolean saveArray(String[] array, String arrayName, Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences(MY_PREFS, 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(arrayName + "_size", array.length);
        for (int i = 0; i < array.length; i++)
            editor.putString(arrayName + "_" + i, array[i]);
        return editor.commit();
    }

    public String[] loadArray(String arrayName, Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences(MY_PREFS, 0);
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
