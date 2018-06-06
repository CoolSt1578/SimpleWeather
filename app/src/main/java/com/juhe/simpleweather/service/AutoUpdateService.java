package com.juhe.simpleweather.service;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.juhe.simpleweather.R;
import com.juhe.simpleweather.activity.WeatherActivity;
import com.juhe.simpleweather.bean.WeatherBean;
import com.juhe.simpleweather.receiver.AutoUpdateReceiver;
import com.thinkland.sdk.android.DataCallBack;
import com.thinkland.sdk.android.JuheData;
import com.thinkland.sdk.android.Parameters;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2016/5/9.
 */
public class AutoUpdateService extends Service {

    private Context mContext;

    private String city;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        city = sp.getString("city", "");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        getWeather();
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int time = 60*1000;
        long triggerTime = SystemClock.elapsedRealtime() + time;
        Intent i = new Intent(this, AutoUpdateReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerTime, pi);
        return super.onStartCommand(intent, flags, startId);
    }

    //获取天气信息
    private void getWeather() {
        if(city == null){
            return ;
        }
        Parameters params = new Parameters();
        params.add("cityname", city);
        params.add("dtype", "json");
        params.add("format", 2);

        JuheData.executeWithAPI(mContext, 39, "http://v.juhe.cn/weather/index",
                JuheData.GET, params, new DataCallBack() {
                    //请求成功时调用，返回Json格式天气信息
                    @Override
                    public void onSuccess(int statusCode, String responseString) {
                        try {
                            JSONObject jsonObject = new JSONObject(responseString);
                            String resultcode = jsonObject.getString("resultcode");
                            //返回的不是天气信息
                            if (!"200".equals(resultcode)) {
                                Toast.makeText(mContext, R.string.search_failed, Toast.LENGTH_SHORT).show();
                                return;
                            }
                            saveWeather(responseString);
                            showNotification(responseString);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    //请求完成时调用，无论成功或失败
                    @Override
                    public void onFinish() {

                    }

                    //请求失败时调用，返回异常信息
                    @Override
                    public void onFailure(int statusCode, String responseString, Throwable throwable) {
                        Toast.makeText(mContext, R.string.search_failed, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveWeather(String jsonWeather) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(mContext).edit();
        editor.putString("weather", jsonWeather);
        editor.commit();
    }


    //通知
    public void showNotification(String jsonWeather) throws JSONException {
        if(jsonWeather == null){
            return;
        }
        JSONObject jsonObject = new JSONObject(jsonWeather);
        JSONObject result = jsonObject.getJSONObject("result");
        String temperature = result.getJSONObject("today").getString("temperature");
        String weather = result.getJSONObject("today").getString("weather");

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);

        String info = "天气预警";

        Bitmap largeIcon = ((BitmapDrawable)getResources().getDrawable(R.drawable.weather1)).getBitmap();

        Intent intent = new Intent(this, WeatherActivity.class);
        builder.setLargeIcon(largeIcon)
                .setSmallIcon(R.drawable.weather)
                .setContentTitle(info)
                .setContentText(city+":"+temperature +" "+ weather)
                .setTicker(info)
                .setContentIntent(PendingIntent.getActivity(this, 0, intent, 0));
        manager.notify(1, builder.getNotification());
    }
}
