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
import android.os.Handler;
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
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
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

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    private ProgressDialog mProgressDialog;
    private static final String LANG_ENG = "0";
    private static final String LANG_THAI = "1";
    private static final String IMAGE = "2";
    private static final String ALIAS = "3";
    private static final String PREFS = "result_db";
    private UserProfile mUserProfile;
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        Intent intent = getIntent();
        point = intent.getIntExtra("point", 0);

        setContentView(R.layout.activity_main);

        changeLayoutSize();

        mUserProfile = (UserProfile) getApplication();
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

        mProgressDialog = Utilities.createProgressDialog("Generating Result...", MainActivity.this);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                mProgressDialog.dismiss();
            }
        }, 3000);

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
        combine.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] image = stream.toByteArray();
        final ParseFile mFile = new ParseFile("ShareImage.png", image);
        mUploadSuccess = false;
        do {
            mFile.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        Log.e("Upload result image:", "Success");
                        mImageUrl = mFile.getUrl();
                        ParseQuery<ParseObject> query = ParseQuery.getQuery(ParseConstant.CLASS_USER_PROFILE);
                        query.getInBackground(mUserProfile.getParseId(), new GetCallback<ParseObject>() {
                            @Override
                            public void done(ParseObject parseObject, ParseException e) {
                                if (e == null) {
                                    Log.e("Get ParseObject", "Success");

                                    parseObject.put(ParseConstant.KEY_IMAGE_FILE, mFile);
                                    parseObject.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            if (e == null) {
                                                Log.e("Update:", "Success");
                                                mShareLinkContent = new ShareLinkContent.Builder()
                                                        .setContentDescription("คลิกที่นี่ เพื่อลองค้นหาเหตุผลที่ทำไมคนถึงชอบคุณดูสิ")
                                                        .setContentTitle("เหตุผลที่ทำไมคนถึงชอบคุณ")
                                                        .setImageUrl(Uri.parse(mImageUrl))
                                                        .setContentUrl(Uri.parse("https://goo.gl/pszrQA"))
                                                        .build();
                                                mShareButton.setEnabled(true);
                                                mUploadSuccess = true;
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
                        Log.e("ERROR:", String.valueOf(e));
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
                mResultTextViewTH.getLayoutParams().height = (int) (imgHeight / 5);
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


    private void changeFontSuperMarket(TextView textView) {
        Typeface font = Typeface.createFromAsset(getAssets(), "font/supermarket.ttf");
        textView.setTypeface(font);
    }

    private void shareLinkContent() {


        mShareDialog.show(mShareLinkContent);
        mShareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {
            @Override
            public void onSuccess(Sharer.Result result) {
                mProgressDialog.dismiss();
                showAds();
                Log.e("Share Result:", "Share Success");
            }

            @Override
            public void onCancel() {
                mProgressDialog.dismiss();
                showAds();
                Log.e("Share Result:", "Share Cancel");
            }

            @Override
            public void onError(FacebookException e) {
                mProgressDialog.dismiss();
                showAds();
                Log.e("Share Result:", "Share Error");
                Log.e("Share Result:", e.toString());
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


    private void getResult() {
        ArrayList<String> categories = new ArrayList<>();
        String category = "X";
        int n = 0;
        for (String s : mUserProfile.getCategory()) {
            categories.add(s.substring(0, 1));
        }
        for (int i = 65; i <= 88; i++) {
            int z = Collections.frequency(categories, Character.toString((char) i));
            if (z > n) {
                n = z;
                int total = i + point;
                if (total < 65) {
                    total = 88 - (64 - total);
                } else if (total > 87) {
                    total = (total - 89) + 65;
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

        String drawableName = shared.getString(category + "_" + IMAGE, "null_" + IMAGE);
        int resID = getResources().getIdentifier(drawableName, "drawable", getPackageName());

        Log.e("Result :", shared.getString(category + "_" + LANG_THAI, "null_" + LANG_THAI));

        mResultTextViewTH.setText(shared.getString(category + "_" + LANG_THAI, "null_" + LANG_THAI));
        mResultImage.setImageResource(resID);
        mNameTextView.setText(mNameTextView.getText() + "\nสมญานาม : " + shared.getString(category + "_" + ALIAS, "null_" + ALIAS));

        mShareButton.setVisibility(View.VISIBLE);
        mRetry.setVisibility(View.VISIBLE);
    }

    private void setUserProfile() {
        mNameTextView.setText(mUserProfile.getUserName());
        Picasso.with(MainActivity.this).load(mUserProfile.getProfileImage()).resize(230, 230).transform(new RoundedTransformation(115, 0)).into(mProfileImage);
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

}
