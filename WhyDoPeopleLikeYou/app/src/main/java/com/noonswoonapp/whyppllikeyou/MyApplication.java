package com.noonswoonapp.whyppllikeyou;

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.parse.Parse;
import com.parse.ParseInstallation;

import org.json.JSONObject;

import java.util.ArrayList;


public class MyApplication extends Application{

    private ArrayList<String> mCategory;
    private JSONObject mUserProfile;
    private String mParseId;
    private String mProfileImage;
    private String mUserName;
    private Boolean mIsDefaultImage = true;
    public static GoogleAnalytics analytics;
    public static Tracker tracker;

    @Override
    public void onCreate() {
        super.onCreate();

        Parse.enableLocalDatastore(this);
        Parse.initialize(this, getString(R.string.parse_app_id), getString(R.string.parse_client_key));
        ParseInstallation.getCurrentInstallation().saveInBackground();

        analytics = GoogleAnalytics.getInstance(this);
        analytics.setLocalDispatchPeriod(1800);

        tracker = analytics.newTracker(getString(R.string.google_analytic_tracker));
        tracker.enableExceptionReporting(true);
        tracker.enableAdvertisingIdCollection(true);
        tracker.enableAutoActivityTracking(true);

    }

    public static Tracker tracker() {
        return tracker;
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
