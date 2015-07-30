package com.noonswoonapp.singlequiz;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
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

import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Random;


public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String RESULT_PREFS = "result_db";
    private static final String RESULT_DESC_IMAGE = "_0";
    private static final String RESULT_IMAGE_M = "_1";
    private static final String RESULT_IMAGE_F = "_2";
    private static final String RESULT_SHARE_M = "_3";
    private static final String RESULT_SHARE_F = "_4";
    private static final String RESULT_SHARE_DESC = "_5";
    private static final int IMAGE_QUALITY = 100;
    private static final int SHARE_IMAGE_HEADER_MARGIN_LEFT = 90;
    private static final String ADS_LINK = "https://play.google.com/store/apps/details?id=com.noonswoon&hl=th";
    private int point;
    private TransferUtility transferUtility;
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
    private boolean mUploadSuccess;
    private RelativeLayout mProfileLayout;
    private boolean mIsRetry = false;
    private boolean mIsShare = false;
    private int result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        transferUtility = Utilities.getTransferUtility(MainActivity.this);
        Intent intent = getIntent();
        point = intent.getIntExtra("point", 0);

        setContentView(R.layout.activity_main);

        mMyApplication = (MyApplication) getApplication();
        mShareDialog = new ShareDialog(MainActivity.this);
        mResultImage = (ImageView) findViewById(R.id.imageview_result_image);
        mResultDesc = (ImageView) findViewById(R.id.imageview_result_desc);
        mRetryButton = (Button) findViewById(R.id.button_retry);
        Utilities.changeFontSuperMarket(mRetryButton, this);
        mProfileImage = (ImageView) findViewById(R.id.imageview_profile);
        mProfileName = (TextView) findViewById(R.id.textview_name);
        Utilities.changeFontSuperMarket(mProfileName, this);
        mShareButton = (ShareButton) findViewById(R.id.button_share);

        setUserProfile();
        getResult();

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
                ParseQuery<ParseObject> query = ParseQuery.getQuery(ParseConstant.CLASS_USER_PROFILE);
                query.getInBackground(mMyApplication.getParseId(), new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject parseObject, ParseException e) {
                        parseObject.put(ParseConstant.KEY_CLICKED_ADS, true);
                        returnToLogin();
                    }
                });
            }

            @Override
            public void didHideAd() {
            }
        });

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
                mProfileLayout.getLayoutParams().width = bitmap1.getWidth() - SHARE_IMAGE_HEADER_MARGIN_LEFT;
                mProfileLayout.requestLayout();
                final float default_size = mProfileName.getTextSize() / 2;
                mProfileName.setTextSize(25);
                mProfileName.requestLayout();

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN)
                    mProfileLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                else
                    mProfileLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);

                mProfileName.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @SuppressLint("NewApi")
                    @SuppressWarnings("deprecation")
                    @Override
                    public void onGlobalLayout() {
                        uploadImage(bitmap1);

                        mProfileLayout.getLayoutParams().width = RelativeLayout.LayoutParams.MATCH_PARENT;
                        mProfileLayout.getLayoutParams().height = RelativeLayout.LayoutParams.MATCH_PARENT;
                        mProfileLayout.requestLayout();

                        mProfileName.setTextSize(default_size);
                        mProfileName.setGravity(Gravity.CENTER);
                        mProfileName.requestLayout();
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN)
                            mProfileName.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        else
                            mProfileName.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    }
                });
            }
        });

        mRetryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyApplication.tracker().send(new HitBuilders.EventBuilder().setCategory("Button")
                        .setAction("Click")
                        .setLabel("Retry")
                        .build());
                if (AdBuddiz.isReadyToShowAd(MainActivity.this) && !mIsShare) {
                    mIsRetry = true;
                    showAds();
                } else {
                    returnToLogin();
                }
            }
        });

        mShareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyApplication.tracker().send(new HitBuilders.EventBuilder().setCategory("Button")
                        .setAction("Click")
                        .setLabel("Share")
                        .build());
                mIsShare = true;
                ParseQuery<ParseObject> query = ParseQuery.getQuery(ParseConstant.CLASS_USER_PROFILE);
                query.getInBackground(mMyApplication.getParseId(), new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject parseObject, ParseException e) {
                        parseObject.put(ParseConstant.KEY_CLICKED_SHARE, true);
                        parseObject.saveInBackground();
                    }
                });
                mProgressDialog = Utilities.createProgressDialog("Sharing Image...", MainActivity.this);
                shareLinkContent();
            }
        });

    }

    private void showAds() {
        Random r = new Random();
        int n = r.nextInt(9);
        if (n <= 4) {
            noonswoonAds();
        } else if (n >= 5) {
            AdBuddiz.showAd(MainActivity.this);
        }
    }

    private void noonswoonAds() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.noonswoon_ad);
        dialog.setCancelable(false);
        Button closeButton = (Button) dialog.findViewById(R.id.button_close);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
                returnToLogin();
            }
        });
        ImageButton ads = (ImageButton) dialog.findViewById(R.id.imagebutton_ads);
        ads.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIsRetry = true;
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(ADS_LINK));
                startActivity(intent);
            }
        });
        dialog.show();
    }

    private void getResult() {
        if (point <= 10 && point >= 12) {
            setQuestionnaireResult(1);
        } else if (point >= 13 && point <= 15) {
            setQuestionnaireResult(2);
        } else if (point >= 16 && point <= 18) {
            setQuestionnaireResult(3);
        } else if (point >= 19 && point <= 21) {
            setQuestionnaireResult(4);
        } else if (point >= 22 && point <= 24) {
            setQuestionnaireResult(5);
        } else if (point >= 25 && point <= 27) {
            setQuestionnaireResult(6);
        } else if (point >= 28 && point <= 30) {
            setQuestionnaireResult(7);
        } else if (point >= 31 && point <= 33) {
            setQuestionnaireResult(8);
        } else if (point >= 34 && point <= 36) {
            setQuestionnaireResult(9);
        } else if (point >= 37 && point <= 40) {
            setQuestionnaireResult(10);
        }
    }

    private void setUserProfile() {
        try {
            mProfileName.setText("ระดับความโสดของ " + mMyApplication.getUserProfile().getString("first_name") + " " + mMyApplication.getUserProfile().getString("last_name"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Picasso.with(MainActivity.this)
                .load(mMyApplication.getProfileImage())
                .resize(230, 230)
                .transform(new RoundedTransformation(115, 0))
                .into(mProfileImage);
    }

    private Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        return resizedBitmap;
    }

    private Bitmap combineImages(Bitmap c, Bitmap s, Bitmap n) {
        Bitmap csn;
        Bitmap s_resize = getResizedBitmap(s, 110, 110);

        int width, height;

        width = c.getWidth();
        height = c.getHeight();

        csn = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas comboImage = new Canvas(csn);

        comboImage.drawBitmap(c, 0f, 0f, null);
        comboImage.drawBitmap(s_resize, SHARE_IMAGE_HEADER_MARGIN_LEFT, 10f, null);
        comboImage.drawBitmap(n, s_resize.getWidth() + SHARE_IMAGE_HEADER_MARGIN_LEFT + 15f, 10f, null);

        return csn;
    }

    private String getRealPathFromURI(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        String path = cursor.getString(idx);
        cursor.close();
        return path;
    }

    private Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.PNG, IMAGE_QUALITY, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    private void deleteFile(File file){
        if (file.exists()) {
            if (file.delete()) {
                System.out.println("file Deleted");
            } else {
                System.out.println("file not Deleted");
            }
        }
    }

    private void uploadImage(Bitmap bitmap1) {

        mProfileImage.setDrawingCacheEnabled(true);
        mProfileImage.buildDrawingCache();
        Bitmap bitmap2 = mProfileImage.getDrawingCache();

        mProfileName.setDrawingCacheEnabled(true);
        mProfileName.buildDrawingCache();
        Bitmap bitmap3 = mProfileName.getDrawingCache();

        Bitmap combine = combineImages(bitmap1, bitmap2, bitmap3);
        mProfileImage.setDrawingCacheEnabled(false);
        mProfileName.setDrawingCacheEnabled(false);

        final Uri tempUri = getImageUri(getApplicationContext(), combine);
        final File finalFile = new File(getRealPathFromURI(tempUri));
        finalFile.deleteOnExit();

        TransferObserver observer = transferUtility.upload(
                getString(R.string.aws_s3_bucket_name),
                mMyApplication.getParseId() + ".png",
                finalFile
        );
        observer.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                if (state.toString().equals("COMPLETED")) {
                    deleteFile(finalFile);
                    SharedPreferences shared = getSharedPreferences(RESULT_PREFS, Context.MODE_PRIVATE);
                    Log.v(TAG, "S3 Upload result image: Success");
                    mShareLinkContent = new ShareLinkContent.Builder()
                            .setContentDescription("คุณเป็นคนโสดรึเปล่า? จริงๆแล้วคุณนั้นโสดแค่ไหน แอพของเราจะบอกระดับความโสดของคุณ ดาวน์โหลด 'โสดแค่ไหน' เพื่อค้นหาระดับความโสดของคุณ และพบกับคำตอบสุดฮาของคุณ อย่าลืมแชร์บอกต่อระดับความโสดของคุณด้วยนะ!")
                            .setContentTitle("โสดแค่ไหน - (" + shared.getString("A" + String.valueOf(result) + RESULT_SHARE_DESC, null) + ")")
                            .setImageUrl(Uri.parse("https://s3-ap-southeast-1.amazonaws.com/" + getString(R.string.aws_s3_bucket_name) + "/" + mMyApplication.getParseId() + ".png"))
                            .setContentUrl(Uri.parse("http://bit.ly/singlequiz"))
                            .build();
                    mShareButton.setEnabled(true);
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {

            }

            @Override
            public void onError(int id, Exception ex) {
                deleteFile(finalFile);
                Log.e(TAG, "S3 Upload result image : Failed");
                ex.printStackTrace();
                Toast.makeText(MainActivity.this, "Error occured. Please try again.", Toast.LENGTH_LONG).show();
            }
        });

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        combine.compress(Bitmap.CompressFormat.JPEG, 0, stream);
        byte[] image = stream.toByteArray();
        final ParseFile mFile = new ParseFile("UserGeneratedResult.jpg", image);
        mUploadSuccess = false;
        do {
            mFile.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        Log.v(TAG, "Parse Upload: Success");
                        mImageUrl = mFile.getUrl();
                        ParseQuery<ParseObject> query = ParseQuery.getQuery(ParseConstant.CLASS_USER_PROFILE);
                        query.getInBackground(mMyApplication.getParseId(), new GetCallback<ParseObject>() {
                            @Override
                            public void done(ParseObject parseObject, ParseException e) {
                                if (e == null) {
                                    Log.v(TAG, "Get parseobject: Success");

                                    parseObject.put(ParseConstant.KEY_IMAGE_FILE, mFile);
                                    parseObject.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            if (e == null) {
                                                Log.v(TAG, "Parse Update : Success");
                                                mUploadSuccess = true;
                                            } else {
                                                Log.e(TAG, "Parse Update : Failed");
                                            }
                                        }
                                    });
                                } else {
                                    Log.e(TAG, "Get parseobject : Failed");
                                }
                            }
                        });
                    } else {
                        Log.e(TAG, "Parse Upload: Failed");
                        Log.e(TAG, "ERROR:" + String.valueOf(e));
                    }
                }
            });
        } while (mUploadSuccess);
/*
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
*/
    }

    private void shareLinkContent() {
        mShareDialog.show(mShareLinkContent);
        mProgressDialog.dismiss();
        mShareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {
            @Override
            public void onSuccess(Sharer.Result result) {
                Log.v(TAG, "Share Result : Share Success");
                showAds();
            }

            @Override
            public void onCancel() {
                Log.v(TAG, "Share Result : Share Cancel");
                showAds();
            }

            @Override
            public void onError(FacebookException e) {
                Log.e(TAG, "Share Result: Share Error");
                Log.e(TAG, "Share Result: " + e.toString());
                showAds();
            }
        });
    }

    private class RoundedTransformation implements com.squareup.picasso.Transformation {
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

    private void setQuestionnaireResult(int result) {
        this.result = result;
        setResultImage(checkResultGender(), mResultImage, result);
        setResultImage(RESULT_DESC_IMAGE, mResultDesc, result);
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
        return RESULT_SHARE_M;
    }

    private void returnToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginScreen.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mIsRetry) {
            returnToLogin();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
