package com.noonswoonapp.singlelevel;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
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
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;

import org.json.JSONException;

import java.io.ByteArrayOutputStream;


public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String RESULT_PREFS = "result_db";
    private static final String RESULT_DESC = "_0";
    private static final String RESULT_IMAGE_M = "_1";
    private static final String RESULT_IMAGE_F = "_2";
    private static final String RESULT_SHARE_M = "_3";
    private static final String RESULT_SHARE_F = "_4";
    private int point;
    private String mImageUrl;
    private ImageView mResultImage;
    private ImageView mResultDesc;
    private ImageView mProfileImage;
    private TextView mProfileName;
    private Button mRetryButton;
    private ShareButton mShareButton;
    private MyApplication mMyApplication;
    private ProgressDialog mProgressDialog;
    private ShareDialog mShareDialog;
    private CallbackManager callbackManager;
    private ShareLinkContent mShareLinkContent;
    private Boolean mUploadSuccess;
    private RelativeLayout mProfileLayout;
    private LinearLayout mMainLayout;
    private int result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        Intent intent = getIntent();
        point = intent.getIntExtra("point", 0);

        setContentView(R.layout.activity_main);

        mMyApplication = (MyApplication) getApplication();
        mShareDialog = new ShareDialog(MainActivity.this);
        mResultImage = (ImageView) findViewById(R.id.imageview_result_image);
        mResultDesc = (ImageView) findViewById(R.id.imageview_result_desc);
        mRetryButton = (Button) findViewById(R.id.button_retry);
        mProfileImage = (ImageView) findViewById(R.id.imageview_profile);
        mProfileName = (TextView) findViewById(R.id.textview_name);
        mShareButton = (ShareButton) findViewById(R.id.button_share);

        setUserProfile();
        getResult();

        mProfileLayout = (RelativeLayout) findViewById(R.id.relativelayout_profile);
        mProfileLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @SuppressLint("NewApi")
            @SuppressWarnings("deprecation")
            @Override
            public void onGlobalLayout() {
                SharedPreferences shared = getSharedPreferences(RESULT_PREFS, Context.MODE_PRIVATE);
                int resID = getResources().getIdentifier(shared.getString("A" + String.valueOf(result) + checkShareGender(), null), "drawable", getPackageName());
                Drawable d = ContextCompat.getDrawable(MainActivity.this, resID);
                final Bitmap bitmap1 = ((BitmapDrawable) d).getBitmap();
                mProfileLayout.getLayoutParams().width = bitmap1.getWidth();
                mProfileLayout.requestLayout();

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN)
                    mProfileLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                else
                    mProfileLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);

                mProfileLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @SuppressLint("NewApi")
                    @SuppressWarnings("deprecation")
                    @Override
                    public void onGlobalLayout() {
                        Bitmap bitmap2 = Utilities.takeLayoutScreenshot((RelativeLayout) findViewById(R.id.relativelayout_profile));

                        uploadImage(bitmap1, bitmap2);
                        mProfileLayout.getLayoutParams().width = RelativeLayout.LayoutParams.MATCH_PARENT;
                        mProfileLayout.requestLayout();
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN)
                            mProfileLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        else
                            mProfileLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    }
                });
            }
        });

        mRetryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LoginScreen.class);
                startActivity(intent);
                finish();
            }
        });

        mShareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseQuery<ParseObject> query = ParseQuery.getQuery(ParseConstant.CLASS_USER_PROFILE);
                query.getInBackground(mMyApplication.getParseId(), new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject parseObject, ParseException e) {
                        parseObject.put(ParseConstant.KEY_CLICKED_SHARE, true);
                    }
                });
                mProgressDialog = Utilities.createProgressDialog("Sharing Image...", MainActivity.this);
                shareLinkContent();
            }
        });

    }

    private void getResult() {
        if (point <= 10 && point >= 12) {
            getResult(1);
        } else if (point >= 13 && point <= 15) {
            getResult(2);
        } else if (point >= 16 && point <= 18) {
            getResult(3);
        } else if (point >= 19 && point <= 21) {
            getResult(4);
        } else if (point >= 22 && point <= 24) {
            getResult(5);
        } else if (point >= 25 && point <= 27) {
            getResult(6);
        } else if (point >= 28 && point <= 30) {
            getResult(7);
        } else if (point >= 31 && point <= 33) {
            getResult(8);
        } else if (point >= 34 && point <= 36) {
            getResult(9);
        } else if (point >= 37 && point <= 40) {
            getResult(10);
        }
    }

    private void setUserProfile() {
        try {
            mProfileName.setText("ระดับความโสดของ\n" + mMyApplication.getUserProfile().getString("first_name") + " " + mMyApplication.getUserProfile().getString("last_name"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Picasso.with(MainActivity.this)
                .load(mMyApplication.getProfileImage())
                .resize(230, 230)
                .transform(new RoundedTransformation(115, 0))
                .into(mProfileImage);
    }

    public Bitmap combineImages(Bitmap c, Bitmap s) {
        Bitmap cs;

        int width, height;

        width = c.getWidth();
        height = c.getHeight();

        cs = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas comboImage = new Canvas(cs);

        comboImage.drawBitmap(c, 0f, 0f, null);
        comboImage.drawBitmap(s, 0f, 0f, null);

        return cs;
    }

    private void uploadImage(Bitmap bitmap1, Bitmap bitmap2) {
        Bitmap combine = combineImages(bitmap1, bitmap2);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        combine.compress(Bitmap.CompressFormat.JPEG, 100, stream);
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
                                                        .setContentUrl(Uri.parse("https://goo.gl/pszrQA"))
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

    private void shareLinkContent() {
        mShareDialog.show(mShareLinkContent);
        mProgressDialog.dismiss();
        mShareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {
            @Override
            public void onSuccess(Sharer.Result result) {
                Log.v(TAG, "Share Result : Share Success");
            }

            @Override
            public void onCancel() {
                Log.v(TAG, "Share Result : Share Cancel");
            }

            @Override
            public void onError(FacebookException e) {
                Log.e(TAG, "Share Result: Share Error");
                Log.e(TAG, "Share Result: " + e.toString());
            }
        });
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

    private void getResult(int result) {
        this.result = result;
        setResultImage(checkResultGender(), mResultImage, result);
        setResultImage(RESULT_DESC, mResultDesc, result);
    }

    private void setResultImage(String reference, ImageView imageView, int result) {
        SharedPreferences shared = getSharedPreferences(RESULT_PREFS, Context.MODE_PRIVATE);
        String drawableName = shared.getString("A" + String.valueOf(result) + reference, null);
        int resID = getResources().getIdentifier(drawableName, "drawable", getPackageName());
        imageView.setImageResource(resID);
    }

    private String checkResultGender() {
        try {
            String gender = mMyApplication.getUserProfile().getString("gender");
            switch (gender) {
                case "male":
                    return RESULT_IMAGE_M;
                case "female":
                    return RESULT_IMAGE_F;
                default:
                    return RESULT_IMAGE_M;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return RESULT_IMAGE_M;
    }

    private String checkShareGender() {
        try {
            String gender = mMyApplication.getUserProfile().getString("gender");
            switch (gender) {
                case "male":
                    return RESULT_SHARE_M;
                case "female":
                    return RESULT_SHARE_F;
                default:
                    return RESULT_SHARE_M;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return RESULT_IMAGE_M;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
