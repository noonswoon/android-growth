package com.noonswoonapp.whyppllikeyou;

import android.annotation.SuppressLint;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareButton;
import com.facebook.share.widget.ShareDialog;
import com.google.android.gms.analytics.HitBuilders;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;
import com.purplebrain.adbuddiz.sdk.AdBuddiz;
import com.purplebrain.adbuddiz.sdk.AdBuddizDelegate;
import com.purplebrain.adbuddiz.sdk.AdBuddizError;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String LANG_ENG = "0";
    private static final String LANG_THAI = "1";
    private static final String IMAGE = "2";
    private static final String ALIAS = "3";
    private static final String PREFS = "result_db";
    private static final int ASCII_A = 65;
    private static final int ASCII_Q = 81;
    private static final int ASCII_Z = 88;
    private static final int IMAGE_QUALITY = 100;
    private ProgressDialog mProgressDialog;
    private MyApplication mMyApplication;
    private CallbackManager callbackManager;
    private TextView mResultTextViewTH;
    private ImageView mProfileImage;
    private ImageView mResultImage;
    private TextView mNameTextView;
    private ShareButton mShareButton;
    private String mImageUrl;
    private int point;
    private Button mRetry;
    private LinearLayout mResultLayout;
    private ShareDialog mShareDialog;
    private ShareLinkContent mShareLinkContent;
    private boolean mUploadSuccess;
    private TextView mWaterMark1;
    private TextView mWaterMark2;
    private boolean mIsRetry;
    private boolean mIsShare = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        Intent intent = getIntent();
        point = intent.getIntExtra("point", 0);

        setContentView(R.layout.activity_main);
        changeLayoutSize();
        initUIElements();
    }

    private void initUIElements() {
        mMyApplication = (MyApplication) getApplication();
        mShareButton = (ShareButton) findViewById(R.id.button_share);
        mProfileImage = (ImageView) findViewById(R.id.image_profile);
        mResultImage = (ImageView) findViewById(R.id.image_result);
        mRetry = (Button) findViewById(R.id.button_retry);
        mShareDialog = new ShareDialog(MainActivity.this);
        mWaterMark1 = (TextView) findViewById(R.id.view_text_watermark1);
        mWaterMark2 = (TextView) findViewById(R.id.view_text_watermark2);

        changeFontSuperMarket(mNameTextView = (TextView) findViewById(R.id.view_text_name));
        changeFontSuperMarket((TextView) findViewById(R.id.view_text_result_header));
        changeFontSuperMarket(mResultTextViewTH = (TextView) findViewById(R.id.view_text_result_th));
        changeFontSuperMarket((TextView) findViewById(R.id.view_text_header_th));
        changeFontSuperMarket((Button) findViewById(R.id.button_retry));
        changeFontSuperMarket((Button) findViewById(R.id.button_share));

        AdBuddiz.setDelegate(new AdBuddizDelegate() {
            @Override
            public void didCacheAd() {
            }

            @Override
            public void didShowAd() {
            }

            @Override
            public void didFailToShowAd(AdBuddizError error) {
            }

            @Override
            public void didClick() {
                MyApplication.tracker().send(new HitBuilders.EventBuilder().setCategory("Ads")
                        .setAction("Click")
                        .setLabel("didClick")
                        .build());
                ParseQuery<ParseObject> query = ParseQuery.getQuery(ParseConstant.CLASS_USER_PROFILE);
                query.getInBackground(mMyApplication.getParseId(), new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject parseObject, ParseException e) {
                        parseObject.put(ParseConstant.KEY_CLICKED_ADS, true);
                    }
                });
            }

            @Override
            public void didHideAd() {
            }
        });

        mRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyApplication.tracker().send(new HitBuilders.EventBuilder().setCategory("Button")
                        .setAction("Click")
                        .setLabel("Retry")
                        .build());
                if (AdBuddiz.isReadyToShowAd(MainActivity.this) && mIsShare) {
                    mIsRetry = true;
                    AdBuddiz.showAd(MainActivity.this);
                } else {
                    returnToLogin();
                }

            }
        });

        mShareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIsShare = false;
                ParseQuery<ParseObject> query = ParseQuery.getQuery(ParseConstant.CLASS_USER_PROFILE);
                query.getInBackground(mMyApplication.getParseId(), new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject parseObject, ParseException e) {
                        parseObject.put(ParseConstant.KEY_CLICKED_SHARE, true);
                    }
                });
                MyApplication.tracker().send(new HitBuilders.EventBuilder().setCategory("Button")
                        .setAction("Click")
                        .setLabel("Share")
                        .build());
                mProgressDialog = Utilities.createProgressDialog("Sharing Image...", MainActivity.this);
                shareLinkContent();
            }
        });
    }


    private void uploadImage() {
        Bitmap bitmap1 = Utilities.takeLayoutScreenshot((RelativeLayout) findViewById(R.id.layout_result_profile));
        Bitmap bitmap2 = Utilities.takeLayoutScreenshot((RelativeLayout) findViewById(R.id.layout_result_image));
        Bitmap combine = combineImages(bitmap1, bitmap2);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        combine.compress(Bitmap.CompressFormat.JPEG, IMAGE_QUALITY, stream);
        byte[] image = stream.toByteArray();
        final ParseFile mFile = new ParseFile("UserGeneratedResult.png", image);
        mUploadSuccess = false;
        do {
            mFile.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        Log.v(TAG, "Upload result image: Success");
                        mImageUrl = mFile.getUrl();
                        ParseQuery<ParseObject> query = ParseQuery.getQuery(ParseConstant.CLASS_USER_PROFILE);
                        query.getInBackground(mMyApplication.getParseId(), new GetCallback<ParseObject>() {
                            @Override
                            public void done(ParseObject parseObject, ParseException e) {
                                if (e == null) {
                                    Log.v(TAG, "Get ParseObject : Success");

                                    parseObject.put(ParseConstant.KEY_IMAGE_FILE, mFile);
                                    parseObject.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            if (e == null) {
                                                Log.v(TAG, "Update : Success");
                                                mShareLinkContent = new ShareLinkContent.Builder()
                                                        .setContentDescription("คลิกที่นี่ เพื่อลองค้นหาเหตุผลที่ทำไมคนถึงชอบคุณดูสิ")
                                                        .setContentTitle("เหตุผลที่ทำไมคนถึงชอบคุณ")
                                                        .setImageUrl(Uri.parse(mImageUrl))
                                                        .setContentUrl(Uri.parse("http://bit.ly/whyppllike"))
                                                        .build();
                                                mShareButton.setEnabled(true);
                                                mUploadSuccess = true;
                                            } else {
                                                Log.e(TAG, "Update : Failed");
                                            }
                                        }
                                    });
                                } else {
                                    Log.e(TAG, "Get ParseObject : Failed");
                                }
                            }
                        });
                    } else {
                        Log.e(TAG, "Upload result image : Failed");
                        Log.e(TAG, "ERROR:" + String.valueOf(e));
                        Toast.makeText(MainActivity.this, "Connection error. Please wait...", Toast.LENGTH_LONG).show();
                    }
                }
            });
        } while (mUploadSuccess);
    }

    private void changeLayoutSize() {
        mResultLayout = (LinearLayout) findViewById(R.id.layout_result);
        mResultLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @SuppressLint("NewApi")
            @SuppressWarnings("deprecation")
            @Override
            public void onGlobalLayout() {

                setUserProfile();
                getResult();
                final int defWidth = mResultLayout.getLayoutParams().width;
                final int defHeight = mResultLayout.getLayoutParams().height;
                double height = mResultLayout.getHeight() / 2;
                mResultLayout.getLayoutParams().width = (int) (height * (1.911 / 2));
                mResultLayout.requestLayout();
                double imgHeight = mResultImage.getHeight();
                mResultTextViewTH.getLayoutParams().height = (int) (imgHeight / 4);
                mResultTextViewTH.requestLayout();
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN)
                    mResultLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                else
                    mResultLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);

                mResultLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @SuppressLint("NewApi")
                    @SuppressWarnings("deprecation")
                    @Override
                    public void onGlobalLayout() {
                        uploadImage();
                        mResultLayout.getLayoutParams().width = defWidth;
                        mResultLayout.getLayoutParams().height = defHeight;
                        mResultLayout.requestLayout();
                        mWaterMark1.setHeight(0);
                        mWaterMark2.setHeight(0);
                        mWaterMark1.setVisibility(View.INVISIBLE);
                        mWaterMark2.setVisibility(View.INVISIBLE);
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN)
                            mResultLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        else
                            mResultLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    }
                });
            }
        });
    }

    public Bitmap combineImages(Bitmap c, Bitmap s) {
        Bitmap cs;

        int width, height;

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

    private void changeFontSuperMarket(TextView textView) {
        Typeface font = Typeface.createFromAsset(getAssets(), "font/supermarket.ttf");
        textView.setTypeface(font);
    }

    private void shareLinkContent() {
        mShareDialog.show(mShareLinkContent);
        mProgressDialog.dismiss();
        mShareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {
            @Override
            public void onSuccess(Sharer.Result result) {
                Log.v(TAG, "Share Result : Share Success");
                AdBuddiz.showAd(MainActivity.this);
            }

            @Override
            public void onCancel() {
                Log.v(TAG, "Share Result : Share Cancel");
                AdBuddiz.showAd(MainActivity.this);
            }

            @Override
            public void onError(FacebookException e) {
                Log.e(TAG, "Share Result: Share Error");
                Log.e(TAG, "Share Result:" + e.toString());
                AdBuddiz.showAd(MainActivity.this);
            }
        });
    }

    private void getResult() {
        ArrayList<String> categories = new ArrayList<>();
        String category = "X";
        int n = 0;
        for (String s : mMyApplication.getCategory()) {
            categories.add(s.substring(0, 1));
        }
        for (int i = ASCII_A; i <= ASCII_Z; i++) {
            int z = Collections.frequency(categories, Character.toString((char) i));
            if (z > n) {
                n = z;
                int total = i + point;
                if (total < ASCII_A) {
                    total = ASCII_Z - (64 - total);
                } else if (total > ASCII_Z) {
                    total = (total - 89) + ASCII_A;
                } else if (total == ASCII_Q) {
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

        String drawableName = shared.getString(category + "_" + IMAGE, "null_" + IMAGE);
        int resID = getResources().getIdentifier(drawableName, "drawable", getPackageName());

        Log.i("Result :", shared.getString(category + "_" + LANG_THAI, "null_" + LANG_THAI));

        mResultTextViewTH.setText(shared.getString(category + "_" + LANG_THAI, "null_" + LANG_THAI));
        mResultImage.setImageResource(resID);
        mNameTextView.setText(mNameTextView.getText() + "\nสมญานาม : " + shared.getString(category + "_" + ALIAS, "null_" + ALIAS));

        mShareButton.setVisibility(View.VISIBLE);
        mRetry.setVisibility(View.VISIBLE);
    }

    private void setUserProfile() {
        mNameTextView.setText(mMyApplication.getUserName());
        if (!mMyApplication.getIsDefaultImage()) {
            File f = new File(mMyApplication.getProfileImage());
            Picasso.with(MainActivity.this).load(f).resize(230, 230).transform(new RoundedTransformation(115, 0)).into(mProfileImage);
        } else {
            Picasso.with(MainActivity.this).load(mMyApplication.getProfileImage()).resize(230, 230).transform(new RoundedTransformation(115, 0)).into(mProfileImage);
        }
    }

    public class RoundedTransformation implements com.squareup.picasso.Transformation {
        private final int radius;
        private final int margin;

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mIsRetry) {
            returnToLogin();
        }
    }

    private void returnToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginScreen.class);
        startActivity(intent);
        finish();
    }

}
