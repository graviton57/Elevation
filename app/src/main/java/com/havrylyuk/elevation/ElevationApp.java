package com.havrylyuk.elevation;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 *
 * Created by Igor Havrylyuk on 23.03.2017.
 */

public class ElevationApp extends Application {

    private static SharedPreferences sSharedPreferences;

    @Override
    public void onCreate() {
        super.onCreate();
        sSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    }

    public static SharedPreferences getSharedPreferences() {
        return sSharedPreferences;
    }
}
