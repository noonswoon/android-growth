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
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareButton;
import com.parse.ParseObject;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;


public class MainActivity extends AppCompatActivity {

    private static final String MY_PREFS = "result_db";
    private UserProfile userProfile = new UserProfile();
    private CallbackManager callbackManager;
    private TextView mResultTextView;
    private ImageView mProfileImage;
    private TextView mNameTextView;
    private ShareButton mShareButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        setContentView(R.layout.activity_main);

        mResultTextView = (TextView) findViewById(R.id.view_text_result);
        mProfileImage = (ImageView) findViewById(R.id.image_profile);
        mNameTextView = (TextView) findViewById(R.id.view_text_name);
        mShareButton = (ShareButton) findViewById(R.id.button_share);

        createDB();



        mShareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap image = takeScreenshot();
                SharePhoto photo = new SharePhoto.Builder()
                        .setBitmap(image)
                        .build();
                SharePhotoContent content = new SharePhotoContent.Builder()
                        .addPhoto(photo)
                        .build();
                mShareButton.setShareContent(content);
                mShareButton.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {
                    @Override
                    public void onSuccess(Sharer.Result result) {
                        showAds();
                        Log.e("Login Result:", "Share Success");
                    }

                    @Override
                    public void onCancel() {
                        Log.e("Login Result:", "Share Cancel");
                    }

                    @Override
                    public void onError(FacebookException e) {
                        Log.e("Login Result:", "Share Error");
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
        JSONObject profile = userProfile.getUserProfile();
        ParseObject user = new ParseObject("UserProfile");
        try {
            user.put("FirstName", profile.getString("first_name"));
            user.put("LastName", profile.getString("last_name"));
            user.put("Email", profile.getString("email"));
            user.put("BirthDate", profile.getString("birthday"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        user.saveEventually();
    }

    public Bitmap takeScreenshot() {
        View rootView = findViewById(android.R.id.content).getRootView();
        rootView.setDrawingCacheEnabled(true);
        return rootView.getDrawingCache();
    }

    private void getResult(TextView result) {
        mShareButton = (ShareButton) findViewById(R.id.button_share);
        mShareButton.setVisibility(View.VISIBLE);
        String category = "null";
        int n = 0;
        for (int i = 65; i <= 90; i++) {
            int z = Collections.frequency(userProfile.getCategory(), Character.toString((char) i));
            if (z > n) {
                n = z;
                category = String.valueOf((char) i);
            }
        }

        SharedPreferences shared = getSharedPreferences(MY_PREFS,
                Context.MODE_PRIVATE);
        result.setText(shared.getString(category, shared.getString("null", "null")));
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
                                category.add(data.getJSONObject(i).getString("category").substring(0, 1));
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
        // Get SharedPreferences
        SharedPreferences shared = getSharedPreferences(MY_PREFS,
                Context.MODE_PRIVATE);
        // Save SharedPreferences
        SharedPreferences.Editor editor = shared.edit();
        if (shared.getBoolean("runfirsttime", true)) {
            editor.putString("A", "You've got beautiful eyes, cute smile and silky hair.");
            editor.putString("B", "You've got a nice skin, very good looking and adorable.");
            editor.putString("C", "You're so sexy that you make the others blush when they look at you.");
            editor.putString("D", "You have glamorous lips and perfect body.");
            editor.putString("E", "You've slim body and skinny legs. Also your mind is beautiful.");
            editor.putString("F", "You're homely looking but your possitive attitude make people like you.");
            editor.putString("G", "You're friendly, good-humored that make everyone wants to be with you.");
            editor.putString("H", "You've got nice legs, curvy body and you're easy-going with everyone.");
            editor.putString("I", "You're so stunning. You make others jaw drop when they look at you.");
            editor.putString("J", "You're funny person. You have sense of humor. Everyone wants to be close with you.");
            editor.putString("K", "You've got adorable dimples. Your smile melt the other hearts.");
            editor.putString("L", "You're a little bit cheeky. your humor always make people laugh.");
            editor.putString("M", "You're rich and generous. That's why people want to be with you.");
            editor.putString("N", "You're never look aged. You skin is very nice and you have no wrinkles.");
            editor.putString("O", "You look very elegant yet adorable. Everyone wants to be with you.");
            editor.putString("P", "You've got blushing cheeks. Everyone can fall in love with you easily when they see you.");
            editor.putString("R", "You're very attractive person and your curvy body make people like you.");
            editor.putString("S", "You're polite, quiet and modest. When people are with you, they're happy.");
            editor.putString("T", "You like adventurous activities and love to visit exciting places.");
            editor.putString("U", "You're extroverted person. Easy-going with everyone and you're kind. That's why people like you.");
            editor.putString("V", "You've nice eyebrows, pretty lips and elegant hair. Moreover you're funny.");
            editor.putString("W", "You've good manner, friendly yet sometimes you're a little bit naughty and cute.");
            editor.putString("null", "You're young and have beautiful mind. You're kind to everyone.");
            editor.putBoolean("runfirsttime", false);
            editor.apply();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppEventsLogger.activateApp(this);
        if (AccessToken.getCurrentAccessToken() != null) {
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
