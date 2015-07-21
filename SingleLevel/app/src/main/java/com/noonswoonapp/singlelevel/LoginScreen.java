package com.noonswoonapp.singlelevel;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.SaveCallback;

import org.json.JSONException;
import org.json.JSONObject;


public class LoginScreen extends Activity {

    private static final String TAG = LoginScreen.class.getSimpleName();
    private LoginButton mLoginButton;
    private CallbackManager mCallbackManager;
    private MyApplication mMyApplication;
    private Boolean mLoggedIn = false;
    private Button mStartButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        mCallbackManager = CallbackManager.Factory.create();
        setContentView(R.layout.activity_login_screen);

        mMyApplication = (MyApplication) getApplication();
        mLoginButton = (LoginButton) findViewById(R.id.button_login);
        mStartButton = (Button) findViewById(R.id.button_start);

        final AccessTokenTracker accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken,
                                                       AccessToken currentAccessToken) {
                if (currentAccessToken == null) {
                    mLoggedIn = false;
                }
            }
        };

        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginScreen.this, Questionnaire.class);
                startActivity(intent);
                finish();
            }
        });

        mLoginButton.setReadPermissions("public_profile, email, user_photos, user_birthday, email");
        mLoginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.i(TAG, "Login Success");
                mLoggedIn = true;
                mLoginButton.setVisibility(View.INVISIBLE);
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

    private void getUserProfile() {
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
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            mMyApplication.setUserProfile(object);
                            sendUserProfile();
                        } else {
                            LoginManager.getInstance().logOut();
                            Toast.makeText(LoginScreen.this, "No internet connection. Please try again.", Toast.LENGTH_LONG).show();
                            mLoginButton.setVisibility(View.VISIBLE);
                        }
                    }
                }

        );
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id, first_name, last_name, picture.width(230).height(230), birthday, email, gender");
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
            if (ParseInstallation.getCurrentInstallation().getList("channels")
                    == null) {
                parseSubscribe(profile.getString("gender"));
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
            mLoginButton.setVisibility(View.INVISIBLE);
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
