package com.noonswoonapp.moonswoon;

import android.app.Application;

import com.parse.Parse;

import org.json.JSONObject;

import java.util.ArrayList;


public class UserProfile extends Application{

    private ArrayList<String> mCategory;
    private JSONObject mUserProfile;
    private String mParseId;
    private String mProfileImage;
    private String mUserName;
    private Boolean mIsDefaultImage = true;

    @Override
    public void onCreate() {
        super.onCreate();

        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "P8e35p55tPX8GyU5dEb3trHVwCRnaujVipX3tImV", "sXGT77ZMWiNaWOcdLqzy9WCEORQSLCMfHlyNeYpa");
    }

    public JSONObject getUserProfile() {
        return mUserProfile;
    }

    public void setUserProfile(JSONObject userProfile) {
        this.mUserProfile = userProfile;
    }

    public ArrayList<String> getCategory() {
        return mCategory;
    }

    public void setCategory(ArrayList<String> category) {
        this.mCategory = category;
    }

    public String getParseId() {
        return mParseId;
    }

    public void setParseId(String parseId) {
        mParseId = parseId;
    }

    public String getProfileImage() {
        return mProfileImage;
    }

    public void setProfileImage(String profileImage) {
        mProfileImage = profileImage;
    }

    public String getUserName() {
        return mUserName;
    }

    public void setUserName(String userName) {
        mUserName = userName;
    }

    public Boolean getIsDefaultImage() {
        return mIsDefaultImage;
    }

    public void setIsDefaultImage(Boolean isDefaultImage) {
        mIsDefaultImage = isDefaultImage;
    }

}
