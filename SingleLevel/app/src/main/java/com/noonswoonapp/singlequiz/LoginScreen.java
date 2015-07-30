package com.noonswoonapp.singlequiz;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.analytics.HitBuilders;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;


public class LoginScreen extends Activity {

    private static final String TAG = LoginScreen.class.getSimpleName();
    private LoginButton mLoginButton;
    private CallbackManager mCallbackManager;
    private MyApplication mMyApplication;
    private Boolean mLoggedIn = false;
    private Button mStartButton;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        mCallbackManager = CallbackManager.Factory.create();
        setContentView(R.layout.activity_login_screen);

        mMyApplication = (MyApplication) getApplication();
        mLoginButton = (LoginButton) findViewById(R.id.button_login);
        mStartButton = (Button) findViewById(R.id.button_start);
        Utilities.changeFontSuperMarket(mStartButton, this);

        final AccessTokenTracker accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken,
                                                       AccessToken currentAccessToken) {
                if (currentAccessToken == null) {
                    mLoggedIn = false;
                    mStartButton.setVisibility(View.INVISIBLE);
                    mLoginButton.setVisibility(View.VISIBLE);
                }
            }
        };

        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyApplication.tracker().send(new HitBuilders.EventBuilder().setCategory("Button")
                        .setAction("Click")
                        .setLabel("Start")
                        .build());
                Intent intent = new Intent(LoginScreen.this, Questionnaire.class);
                startActivity(intent);
                finish();
            }
        });

        mLoginButton.setReadPermissions("public_profile, email, user_photos, user_birthday, email");
        mLoginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                MyApplication.tracker().send(new HitBuilders.EventBuilder().setCategory("Button")
                        .setAction("Click")
                        .setLabel("Login")
                        .build());
                Log.i(TAG, "Login Success");
                mLoggedIn = true;
                accessTokenTracker.startTracking();
                getUserProfile();
            }

            @Override
            public void onCancel() {
                Log.i(TAG, "Login Cancel");
            }

            @Override
            public void onError(FacebookException exception) {
                Log.e(TAG, "Login Error");
            }
        });
    }

    private void changeFontSuperMarket(TextView textView) {
        Typeface font = Typeface.createFromAsset(getAssets(), "font/supermarket.ttf");
        textView.setTypeface(font);
    }

    private void getUserProfile() {
        mLoginButton.setVisibility(View.INVISIBLE);
        mProgressDialog = Utilities.createProgressDialog("Retrieving user profile...", LoginScreen.this);
        mProgressDialog.show();
        GraphRequest request = GraphRequest.newMeRequest(
                AccessToken.getCurrentAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(
                            JSONObject object,
                            GraphResponse response) {
                        if (object != null) {
                            try {
                                JSONObject picture = object.getJSONObject("picture");
                                JSONObject data = picture.getJSONObject("data");
                                mMyApplication.setProfileImage(data.getString("url"));
                                Picasso.with(LoginScreen.this).load(data.getString("url")).resize(230, 230).transform(new RoundedTransformation(115, 0)).fetch();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            mMyApplication.setUserProfile(object);
                            mProgressDialog.dismiss();
                            mStartButton.setVisibility(View.VISIBLE);
                            sendUserProfile();
                        } else {
                            mProgressDialog.dismiss();
                            LoginManager.getInstance().logOut();
                            Toast.makeText(LoginScreen.this, "No internet connection. Please try again.", Toast.LENGTH_LONG).show();
                            mLoginButton.setVisibility(View.VISIBLE);
                        }
                    }
                }

        );
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id, first_name, last_name, picture.width(300).height(300), birthday, email, gender");
        request.setParameters(parameters);
        request.executeAsync();
    }

    private void sendUserProfile() {
        final ParseObject mUser = new ParseObject(ParseConstant.CLASS_USER_PROFILE);
        JSONObject profile = mMyApplication.getUserProfile();
        try {
            mUser.put(ParseConstant.KEY_FIRST_NAME, profile.getString("first_name"));
            mUser.put(ParseConstant.KEY_LAST_NAME, profile.getString("last_name"));
            mUser.put(ParseConstant.KEY_EMAIL, profile.getString("email"));
            mUser.put(ParseConstant.KEY_BIRTH_DATE, profile.getString("birthday"));
            mUser.put(ParseConstant.KEY_ID, profile.getString("id"));
            mUser.put(ParseConstant.KEY_GENDER, profile.getString("gender"));
            if (ParseInstallation.getCurrentInstallation().getList("channels")
                    == null) {
                parseSubscribe(profile.getString("gender"));
                parseSubscribe("nssinglelv" + profile.getString("id"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mUser.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.v(TAG, "Send user profile Success");
                    mMyApplication.setParseId(mUser.getObjectId());
                } else {
                    Log.e(TAG, "Send user profile Failed");
                }
            }
        });
    }

    private void parseSubscribe(final String channel) {
        ParsePush.subscribeInBackground(channel, new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.v(TAG, "Parse : successfully subscribed to the " + channel + " channel.");
                } else {
                    Log.e(TAG, "Parse : failed to subscribe for push", e);
                }
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
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



}
