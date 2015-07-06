package com.noonswoonapp.moonswoon;

import android.annotation.SuppressLint;
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
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

public class MainActivity extends AppCompatActivity {

    private ProgressDialog mProgressDialog;
    private static final String PREFS = "result_db_new";
    private static final String LANG_ENG = "0";
    private static final String LANG_THAI = "1";
    private static final String IMAGE = "2";
    private static final boolean UPDATE_DB = true;
    private UserProfile userProfile = new UserProfile();
    private CallbackManager callbackManager;
    private TextView mResultTextViewTH;
    private ImageView mProfileImage;
    private ImageView mResultImage;
    private TextView mNameTextView;
    private ShareButton mShareButton;
    private String mImageUrl;
    private String mParseId;
    private Boolean mLoggedIn = false;
    private int point;
    private Button mRetry;
    private LinearLayout mResultLayout;
    private int height;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        loadDB();
        Intent intent = getIntent();
        point = intent.getIntExtra("point", 0);

        setContentView(R.layout.activity_main);

        mResultLayout = (LinearLayout) findViewById(R.id.layout_result);
        mResultLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @SuppressLint("NewApi")
            @SuppressWarnings("deprecation")
            @Override
            public void onGlobalLayout() {
                height = mResultLayout.getHeight() / 2;
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mResultLayout.getLayoutParams();
                params.width = (int) (height * 1.911);
                mResultLayout.setLayoutParams(params);

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN)
                    mResultLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                else
                    mResultLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });




        mShareButton = (ShareButton) findViewById(R.id.button_share);
        mProfileImage = (ImageView) findViewById(R.id.image_profile);
        changeFontSuperMarket(mNameTextView = (TextView) findViewById(R.id.view_text_name));
        mResultImage = (ImageView) findViewById(R.id.image_result);
        mRetry = (Button) findViewById(R.id.button_retry);
        changeFontSuperMarket(mResultTextViewTH = (TextView) findViewById(R.id.view_text_result_th));
        changeFontSuperMarket((TextView) findViewById(R.id.view_text_header_th));
        changeFontSuperMarket((Button) findViewById(R.id.button_retry));
        changeFontSuperMarket((Button) findViewById(R.id.button_share));

        createProgressDialog("Generating Result...");

        mRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SplashScreen.class);
                startActivity(intent);
                finish();
            }
        });

        mShareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap bitmap1 = takeLayoutScreenshot((RelativeLayout) findViewById(R.id.layout_result_profile));
                Bitmap bitmap2 = takeLayoutScreenshot((RelativeLayout) findViewById(R.id.layout_result_image));
                Bitmap combine = combineImages(bitmap1, bitmap2);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                combine.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] image = stream.toByteArray();
                final ParseFile mFile = new ParseFile("ShareImage.png", image);

                createProgressDialog("Sharing Image...");
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

    public Bitmap combineImages(Bitmap c, Bitmap s) {
        Bitmap cs = null;

        int width, height = 0;

        if (c.getWidth() > s.getWidth()) {
            width = c.getWidth() + s.getWidth();
            height = c.getHeight();
        } else {
            width = s.getWidth() + s.getWidth();
            height = c.getHeight();
        }

        cs = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas comboImage = new Canvas(cs);

        comboImage.drawBitmap(c, 0f, 0f, null);
        comboImage.drawBitmap(s, c.getWidth(), 0f, null);

        return cs;
    }

    private void createProgressDialog(String Message) {
        mProgressDialog = new ProgressDialog(MainActivity.this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setMessage(Message);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();
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
                    mProgressDialog.dismiss();
                } else {
                    Log.e("Send user profile:", "Failed");
                    mProgressDialog.dismiss();
                }
            }
        });
    }

    public Bitmap takeLayoutScreenshot(RelativeLayout view) {
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
                } else if (total == 81) {
                    if (point >= 0) {
                        total++;
                    } else {
                        total--;
                    }
                }
                category = String.valueOf((char) total);
            }
        }

        SharedPreferences shared = getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        mResultTextViewTH.setText(shared.getString(category + "_" + LANG_THAI, shared.getString("null_" + LANG_THAI, "null_" + LANG_THAI)));
        Log.e("Thai:", shared.getString(category + "_" + LANG_THAI, shared.getString("null_" + LANG_THAI, "null_" + LANG_THAI)));
        String mDrawableName = shared.getString(category + "_" + IMAGE, shared.getString("null_" + IMAGE, "null_" + IMAGE));
        int resID = getResources().getIdentifier(mDrawableName, "drawable", getPackageName());
        mResultImage.setImageResource(resID);
        mShareButton.setVisibility(View.VISIBLE);
        mRetry.setVisibility(View.VISIBLE);
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
                            Picasso.with(MainActivity.this).load(data.getString("url")).transform(new RoundedTransformation(150, 0)).into(mProfileImage);
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
        parameters.putString("fields", "id, first_name,last_name, picture.width(300).height(300), likes.limit(100), birthday, email");
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
            paint1.setColor(Color.parseColor("#FFee34"));
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
                            "ตาตี่ จิตใจเหี้ยม",
                            "result_1"},
                    "A", PREFS, MainActivity.this);
            Utilities.saveArray(new String[]{"Nice skin, Good-looking, Adorable",
                            "บั้นท้ายใหญ่ ยิ้มง่าย",
                            "result_2"},
                    "B", PREFS, MainActivity.this);
            Utilities.saveArray(new String[]{"Sexy, Charming, Attractive",
                            "ขาใหญ่ ขำขัน",
                            "result_3"},
                    "C", PREFS, MainActivity.this);
            Utilities.saveArray(new String[]{"Glamorous lips, Perfect body, Nice attitude",
                            "ตาเหล่ เซ็กซี่",
                            "result_4"},
                    "D", PREFS, MainActivity.this);
            Utilities.saveArray(new String[]{"Slim body, Skinny Legs, Beautiful mind",
                            "คิ้วดก พูดตรง",
                            "result_5"},
                    "E", PREFS, MainActivity.this);
            Utilities.saveArray(new String[]{"Homely looking, Fine hairstyle, Positive attitude",
                            "รักแร้เหม็น ทะเล้น",
                            "result_6"},
                    "F", PREFS, MainActivity.this);
            Utilities.saveArray(new String[]{"Friendly, Good-humored, Nice smile",
                            "จ้ำม่ำ กินเก่ง",
                            "result_7"},
                    "G", PREFS, MainActivity.this);
            Utilities.saveArray(new String[]{"Nice legs, Curvy body, Easy-going",
                            "ขนจมูกยาว รวย",
                            "result_8"},
                    "H", PREFS, MainActivity.this);
            Utilities.saveArray(new String[]{"Chubby, Good-humoured, Friendly",
                            "รักแร้ดำ จิตใจดี",
                            "result_9"},
                    "I", PREFS, MainActivity.this);
            Utilities.saveArray(new String[]{"Skinny, Funny, Have Sense of Humor",
                            "ริมฝีปากอวบ ถ่อมตน",
                            "result_10"},
                    "J", PREFS, MainActivity.this);
            Utilities.saveArray(new String[]{"Adorable Dimples, Round Face, Fine Skin",
                            "ไม่มีรอยตีนกา ขี้เหนียว",
                            "result_11"},
                    "K", PREFS, MainActivity.this);
            Utilities.saveArray(new String[]{"Cheeky, Funny, Attractive",
                            "หน้าตาบ้านๆ ใจดี",
                            "result_12"},
                    "L", PREFS, MainActivity.this);
            Utilities.saveArray(new String[]{"Rich, Generous, Beautiful face",
                            "ขนหน้าแข้งดก เป็นกันเอง",
                            "result_13"},
                    "M", PREFS, MainActivity.this);
            Utilities.saveArray(new String[]{"Not Aged, Smooth Skin, No Wrinkles",
                            "ขายาว ขี้งอน",
                            "result_14"},
                    "N", PREFS, MainActivity.this);
            Utilities.saveArray(new String[]{"Elegant, Adorable, Pleasing",
                            "พุงนุ่มนิ่ม อ่อนโยน",
                            "result_15"},
                    "O", PREFS, MainActivity.this);
            Utilities.saveArray(new String[]{"Blushing Cheeks, Nice Makeup, Pretty Face",
                            "รักแร้ขาว กินจุ",
                            "result_16"},
                    "P", PREFS, MainActivity.this);
            Utilities.saveArray(new String[]{"Long Legs, Cool Hairstyle, Fashionable style",
                            "คิ้วบาง ตลก",
                            "result_17"},
                    "R", PREFS, MainActivity.this);
            Utilities.saveArray(new String[]{"Polite, Quiet, Modest",
                            "จมูกโด่ง ซน",
                            "result_18"},
                    "S", PREFS, MainActivity.this);
            Utilities.saveArray(new String[]{"Cool Hobbies, Cool Friend, Fashionable style",
                            "ขนแขนดก ประหยัด",
                            "result_19"},
                    "T", PREFS, MainActivity.this);
            Utilities.saveArray(new String[]{"Extroverted, Easy-going, Kind",
                            "เอวบาง เข้าสังคมเก่ง",
                            "result_20"},
                    "U", PREFS, MainActivity.this);
            Utilities.saveArray(new String[]{"Nice Eyebrows, Elegant Hair, Funny",
                            "เท้าเหม็น ใจดี",
                            "result_21"},
                    "V", PREFS, MainActivity.this);
            Utilities.saveArray(new String[]{"Good Manner, Friendly, Cute",
                            "ฟันเหยิน จริงใจ",
                            "result_22"},
                    "W", PREFS, MainActivity.this);
            Utilities.saveArray(new String[]{"Young, Beautiful Mind, Kind",
                            "ผมหยิก รวย",
                            "result_23"},
                    "null", PREFS, MainActivity.this);
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
