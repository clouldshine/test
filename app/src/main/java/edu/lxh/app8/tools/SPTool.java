package edu.lxh.app8.tools;

import android.content.Context;
import android.content.SharedPreferences;

public class SPTool {
    private static volatile SPTool instance = null;
    private final Context appCtx;
    private final String SP_NAME = "edu_app_8";
    private SharedPreferences mSharedPrefs;
    private SharedPreferences.Editor mEditor;

    public static SPTool getInstance(Context context) {
        if (instance == null) {
            synchronized (SPTool.class) {
                if (instance == null) {
                    instance = new SPTool(context);
                }
            }
        }
        return instance;
    }

    private SPTool(Context context) {
        appCtx = context.getApplicationContext();
        mSharedPrefs = appCtx.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        mEditor = mSharedPrefs.edit();
    }


    public void putString(String name, String value) {
        mEditor.putString(name, value).apply();
    }

    public String getString(String name, String defaultValue) {
        return mSharedPrefs.getString(name, defaultValue);
    }

    public void clear() {
        mEditor.clear().apply();
    }

}
