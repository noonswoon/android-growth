package com.noonswoonapp.singlelevel;

import android.app.Application;

import com.parse.Parse;

import org.json.JSONObject;


public class MyApplication extends Application {
    private JSONObject mUserProfile;
    private String mProfileImage;
    private String mParseId;
    private String mUserGender;

    @Override
    public void onCreate() {
        super.onCreate();
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, getString(R.string.parse_app_id), getString(R.string.parse_client_key));

    }

    public JSONObject getUserProfile() {
        return mUserProfile;
    }

    public void setUserProfile(JSONObject userProfile) {
        mUserProfile = userProfile;
    }

    public String getProfileImage() {
        return mProfileImage;
    }

    public void setProfileImage(String profileImage) {
        mProfileImage = profileImage;
    }

    public String getParseId() {
        return mParseId;
    }

    public void setParseId(String parseId) {
        mParseId = parseId;
    }

    public String getUserGender() {
        return mUserGender;
    }

    public void setUserGender(String userGender) {
        mUserGender = userGender;
    }
}
