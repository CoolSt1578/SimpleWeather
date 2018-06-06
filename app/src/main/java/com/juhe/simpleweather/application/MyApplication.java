package com.juhe.simpleweather.application;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;

import com.thinkland.sdk.android.JuheSDKInitializer;

/**
 * Created by Gdd on 2016/4/21.
 */
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        JuheSDKInitializer.initialize(getApplicationContext());

    }
}
