package com.noonswoonapp.moonswoon;

import android.app.Application;
import android.net.Uri;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.parse.Parse;

import org.json.JSONObject;

import java.util.ArrayList;


public class UserProfile extends Application{

    public static GoogleAnalytics analytics;
    public static Tracker tracker;
    private ArrayList<String> mCategory;
    private JSONObject mUserProfile;
    private String mParseId;
    private Uri mProfileImage;
    private String mUserName;

    @Override
    public void onCreate() {
        super.onCreate();

        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "P8e35p55tPX8GyU5dEb3trHVwCRnaujVipX3tImV", "sXGT77ZMWiNaWOcdLqzy9WCEORQSLCMfHlyNeYpa");
        analytics = GoogleAnalytics.getInstance(this);
        analytics.setLocalDispatchPeriod(1800);

        tracker = analytics.newTracker("UA-64903227-1");
        tracker.enableExceptionReporting(true);
        tracker.enableAdvertisingIdCollection(true);
        tracker.enableAutoActivityTracking(true);
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

    public Uri getProfileImage() {
        return mProfileImage;
    }

    public void setProfileImage(Uri profileImage) {
        mProfileImage = profileImage;
    }

    public String getUserName() {
        return mUserName;
    }

    public void setUserName(String userName) {
        mUserName = userName;
    }
}
