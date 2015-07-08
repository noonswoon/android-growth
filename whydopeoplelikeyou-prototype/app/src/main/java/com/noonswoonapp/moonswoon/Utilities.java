package com.noonswoonapp.moonswoon;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;

public class Utilities {

    public static boolean saveArray(String[] array, String arrayName, String dbName, Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences(dbName, 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(arrayName + "_size", array.length);
        for (int i = 0; i < array.length; i++)
            editor.putString(arrayName + "_" + i, array[i]);
        return editor.commit();
    }

    public static ProgressDialog createProgressDialog(String Message, Context context) {
        ProgressDialog mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setMessage(Message);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();
        return mProgressDialog;
    }
}
