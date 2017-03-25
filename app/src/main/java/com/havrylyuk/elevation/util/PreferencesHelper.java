package com.havrylyuk.elevation.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.havrylyuk.elevation.ElevationApp;
import com.havrylyuk.elevation.R;



/**
 * Preferences Helper
 * Created by Igor Havrylyuk on 23.03.2017.
 */

public class PreferencesHelper {


    private static PreferencesHelper sInstance = null;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public static PreferencesHelper getInstance() {
        if(sInstance == null) {
            sInstance = new PreferencesHelper();
        }
        return sInstance;
    }

    public PreferencesHelper() {
        this.sharedPreferences = ElevationApp.getSharedPreferences();
        this.editor = this.sharedPreferences.edit();
    }

    public void setApiType(Context context, int index){
        editor.putInt(context.getString(R.string.pref_api_type_key), SourceType.values()[index].ordinal());
        editor.apply();
    }

    public int getApiType(Context context){
        return sharedPreferences.getInt(context.getString(R.string.pref_api_type_key),
                SourceType.SRTM1.ordinal());
    }

}
