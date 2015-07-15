package com.noonswoonapp.whyppllikeyou;

import android.app.ProgressDialog;
import android.content.Intent;
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
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.noonswoonapp.moonswoon.R;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;


public class LoginScreen extends AppCompatActivity {

    private int PICK_IMAGE_REQUEST = 1;
    private Button mStartButton;
    private LoginButton mLoginButton;
    private EditText mName;
    private CallbackManager callbackManager;
    private UserProfile mUserProfile;
    private Boolean mLoggedIn = false;
    private ImageButton mProfileImage;
    private ProgressDialog mProgressDialog;
    private TextView mImageDescr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        setContentView(R.layout.activity_login_screen);

        mImageDescr = (TextView) findViewById(R.id.view_text_image_description);
        mUserProfile = (UserProfile) getApplication();
        mStartButton = (Button) findViewById(R.id.button_start);
        mLoginButton = (LoginButton) findViewById(R.id.button_login);
        mProfileImage = (ImageButton) findViewById(R.id.image_button_profile);
        mName = (EditText) findViewById(R.id.text_edit_name);
        mName.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        mName.requestFocus();

        Typeface font = Typeface.createFromAsset(getAssets(), "font/supermarket.ttf");
        mStartButton.setTypeface(font);
        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginScreen.this, Questionnaire.class);
                startActivity(intent);
                mUserProfile.setUserName(mName.getText().toString());
                finish();
            }
        });

        mProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
            }
        });

        final AccessTokenTracker accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken,
                                                       AccessToken currentAccessToken) {
                if (currentAccessToken == null) {
                    mLoggedIn = false;
                    mStartButton.setVisibility(View.INVISIBLE);
                    mProfileImage.setVisibility(View.INVISIBLE);
                    mName.setVisibility(View.INVISIBLE);
                }
            }
        };

        mLoginButton.setReadPermissions("public_profile, email, user_likes, user_photos, user_birthday, email");
        mLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                mLoggedIn = true;
                mLoginButton.setVisibility(View.INVISIBLE);
                accessTokenTracker.startTracking();
                getUserProfile();
                Log.e("Login Result:", "Success");
            }

            @Override
            public void onCancel() {
                Log.e("Login Result:", "Cancel");
            }

            @Override
            public void onError(FacebookException exception) {
                Log.e("Login Result:", "Error");
                Toast.makeText(LoginScreen.this, "Login Error. Please try again.", Toast.LENGTH_LONG).show();
            }
        });
    }



    private void getUserProfile() {
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
                                JSONObject picture = object.getJSONObject("picture");
                                JSONObject data = picture.getJSONObject("data");
                                Log.e("JSON: ", data.getString("url"));
                                Log.e("JSON: ", "https://graph.facebook.com/" + object.getString("id") + "/picture?type=large");
                                mUserProfile.setProfileImage(data.getString("url"));

                                Picasso.with(LoginScreen.this).load(data.getString("url")).resize(230, 230).transform(new RoundedTransformation(115, 0)).into(mProfileImage);

                                mName.setText(object.getString("first_name") + " " + object.getString("last_name"));
                                mName.setSelection(mName.getText().length());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            mUserProfile.setUserProfile(object);
                            mUserProfile.setCategory(category);
                            mStartButton.setVisibility(View.VISIBLE);
                            mProfileImage.setVisibility(View.VISIBLE);
                            mName.setVisibility(View.VISIBLE);
                            mImageDescr.setVisibility(View.VISIBLE);
                            mProgressDialog.dismiss();
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
        parameters.putString("fields", "id, first_name,last_name, picture.width(230).height(230), likes.limit(100), birthday, email");
        request.setParameters(parameters);
        request.executeAsync();
    }

    private void sendUserProfile() {
        final ParseObject mUser = new ParseObject(ParseConstant.CLASS_USER_PROFILE);
        JSONObject profile = mUserProfile.getUserProfile();
        try {
            mUser.put(ParseConstant.KEY_FIRST_NAME, profile.getString("first_name"));
            mUser.put(ParseConstant.KEY_LAST_NAME, profile.getString("last_name"));
            mUser.put(ParseConstant.KEY_EMAIL, profile.getString("email"));
            mUser.put(ParseConstant.KEY_BIRTH_DATE, profile.getString("birthday"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mUser.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.e("Send user profile:", "Success");
                    mUserProfile.setParseId(mUser.getObjectId());
                } else {
                    Log.e("Send user profile:", "Failed");
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
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri uri = data.getData();
            String filePath = Utilities.getPath(LoginScreen.this, uri);
            File f = new File(filePath);
            mUserProfile.setProfileImage(filePath);
            mUserProfile.setIsDefaultImage(false);
            Picasso.with(LoginScreen.this).load(f).resize(230, 230).transform(new RoundedTransformation(115, 0)).into(mProfileImage);
        }
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

    @Override
    protected void onStart() {
        super.onStart();
    }


    @Override
    protected void onStop() {
        super.onStop();
    }
}
