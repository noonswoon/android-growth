package com.noonswoonapp.singlequiz;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.parse.Parse;
import com.parse.ParseInstallation;

import org.json.JSONObject;

import io.fabric.sdk.android.Fabric;


public class MyApplication extends Application {
    private JSONObject mUserProfile;
    private String mProfileImage;
    private String mParseId;
    private String mUserGender;
    public static GoogleAnalytics analytics;
    public static Tracker tracker;

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
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
