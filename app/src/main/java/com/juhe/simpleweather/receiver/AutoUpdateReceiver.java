package com.juhe.simpleweather.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.juhe.simpleweather.service.AutoUpdateService;

/**
 * Created by Administrator on 2016/5/9.
 */
public class AutoUpdateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent in = new Intent(context, AutoUpdateService.class);
        context.startService(in);
    }
}
