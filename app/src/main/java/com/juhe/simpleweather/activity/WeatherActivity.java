package com.juhe.simpleweather.activity;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.juhe.simpleweather.R;
import com.juhe.simpleweather.bean.FutureWeatherBean;
import com.juhe.simpleweather.bean.PmBean;
import com.juhe.simpleweather.bean.WeatherBean;
import com.juhe.simpleweather.service.AutoUpdateService;
import com.juhe.simpleweather.service.CityService;
import com.juhe.simpleweather.swiperefresh.PullToRefreshBase;
import com.juhe.simpleweather.swiperefresh.PullToRefreshScrollView;
import com.thinkland.sdk.android.DataCallBack;
import com.thinkland.sdk.android.JuheData;
import com.thinkland.sdk.android.Parameters;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


/**
 * Created by Gdd on 2016/4/20.
 */
public class WeatherActivity extends Activity {

    private PullToRefreshScrollView mPullToRefreshScrollView;
    private ScrollView mScrollView;

    private Context mContext;

    private TextView tv_city,   //城市
            tv_date,        //日期
            tv_now_temp,        //现在温度
            tv_now_weather,     //现在天气
            tv_today_temp,      //今天气温
            tv_aqi,     //PM指数
            tv_quality,     //空气质量
            tv_dressing,    //穿衣指数
            tv_uv_index,    //紫外线指数
            tv_wind,    //风力指数
            tv_today_temp_a,        //今天最高气温
            tv_today_temp_b,        //今天最低气温
            tv_tomorrow,    //明日日期
            tv_tomorrow_temp_a,     //明日最高气温
            tv_tomorrow_temp_b,     //明日最低气温
            tv_thirdday,        //第三日日期
            tv_thirdday_temp_a,      //第三日最高气温
            tv_thirdday_temp_b,     //第三日最低气温
            tv_fourthday,        //第四日日期
            tv_fourthday_temp_a,      //第四日最高气温
            tv_fourthday_temp_b,     //第四日最低气温
            tv_fifthday,        //第五日日期
            tv_fifthday_temp_a,      //第五日最高气温
            tv_fifthday_temp_b;     //第五日最低气温

    private ImageView iv_city,      //城市选择
            iv_now_weather,     //现在天气图标
            iv_today_weather,       //今天天气图标
            iv_tomorrow_weather,        //明日天气图标
            iv_thirdday_weather,        //第三日天气图标
            iv_fourthday_weather,       //第四日天气图标
            iv_fifthday_weather;        //第五日气温图标

    private LinearLayout ll_moreinfo;

    private WeatherBean weatherBean;
    private PmBean pmBean;

    private String city;    //城市
    private String pm_city;

    private String jsonPm;

    public static final String tag = "CityService";

    private CityService cityService;
    private ServiceConnection cityConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            Log.v(tag, "onServiceConnected");
            cityService = ((CityService.CityBinder)service).getService();

            cityService.setCityCallBack(new CityService.CityCallBack() {
                @Override
                public void onCityCallback(String[] cities) {
                    city = cities[0];
                    pm_city = cities[1];
                    saveCity(city);
                    getWeather();
                    getPm();
                }
            });
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.v(tag, "onServiceDisconnected");
            cityService.removeCityCallBack();
        }
    };

    private void saveCity(String city) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(mContext).edit();
        editor.putString("city", city);
        editor.commit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        mContext = this;
        //初始化控件、注册监听
        init();
        //获得天气
        showSharedPreferenceWeather();
        showSharedPreferencePm();
        //启动定位服务
        initCityService();
        //启动自动更新
        Intent intent = new Intent(mContext, AutoUpdateService.class);
        startService(intent);
    }

    //判断当前是否联网
    private boolean isConnectNet() {
        ConnectivityManager cManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cManager != null) {
            NetworkInfo info = cManager.getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                if (info.getState() == NetworkInfo.State.CONNECTED) {
                    return true;
                }
            }
        }
        return false;
    }

    private void initCityService() {
        Log.v(tag, "initCityService");
        Intent intent = new Intent(mContext, CityService.class);
        startService(intent);
        bindService(intent, cityConnection, BIND_AUTO_CREATE);
    }

    private void init() {
        //下拉刷新
        mPullToRefreshScrollView = (PullToRefreshScrollView) findViewById(R.id.pull_refresh_scrollview);
        mPullToRefreshScrollView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ScrollView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ScrollView> refreshView) {
                if (!isConnectNet()) {
                    Toast.makeText(mContext, R.string.net, Toast.LENGTH_SHORT).show();
                    mPullToRefreshScrollView.onRefreshComplete();
                    return;
                }
                //下拉刷新获取天气信息
                getWeather();
                getPm();
                mPullToRefreshScrollView.onRefreshComplete();
            }

        });

        mScrollView = mPullToRefreshScrollView.getRefreshableView();

        tv_city = (TextView) findViewById(R.id.tv_city);
        tv_date = (TextView) findViewById(R.id.tv_date);
        tv_now_temp = (TextView) findViewById(R.id.tv_now_temp);
        tv_now_weather = (TextView) findViewById(R.id.tv_now_weather);
        tv_today_temp = (TextView) findViewById(R.id.tv_today_temp);
        tv_aqi = (TextView) findViewById(R.id.tv_aqi);
        tv_quality = (TextView) findViewById(R.id.tv_quality);
        tv_dressing = (TextView) findViewById(R.id.tv_dressing);
        tv_wind = (TextView) findViewById(R.id.tv_wind);
        tv_uv_index = (TextView) findViewById((R.id.tv_uv_index));
        tv_today_temp_a = (TextView) findViewById(R.id.tv_today_temp_a);
        tv_today_temp_b = (TextView) findViewById(R.id.tv_today_temp_b);
        tv_tomorrow = (TextView) findViewById(R.id.tv_tomorrow);
        tv_tomorrow_temp_a = (TextView) findViewById(R.id.tv_tomorrow_temp_a);
        tv_tomorrow_temp_b = (TextView) findViewById(R.id.tv_tomorrow_temp_b);
        tv_thirdday = (TextView) findViewById(R.id.tv_thirdday);
        tv_thirdday_temp_a = (TextView) findViewById(R.id.tv_thirdday_temp_a);
        tv_thirdday_temp_b = (TextView) findViewById(R.id.tv_thirdday_temp_b);
        tv_fourthday = (TextView) findViewById(R.id.tv_fourthday);
        tv_fourthday_temp_a = (TextView) findViewById(R.id.tv_fourthday_temp_a);
        tv_fourthday_temp_b = (TextView) findViewById(R.id.tv_fourthday_temp_b);
        tv_fifthday = (TextView) findViewById(R.id.tv_fifthday);
        tv_fifthday_temp_a = (TextView) findViewById(R.id.tv_fifthday_temp_a);
        tv_fifthday_temp_b = (TextView) findViewById(R.id.tv_fifthday_temp_b);

        iv_city = (ImageView) findViewById(R.id.iv_city);
        iv_now_weather = (ImageView) findViewById(R.id.iv_now_weather);
        iv_today_weather = (ImageView) findViewById(R.id.iv_today_weather);
        iv_tomorrow_weather = (ImageView) findViewById(R.id.iv_tomorrow_weather);
        iv_thirdday_weather = (ImageView) findViewById(R.id.iv_thirdday_weather);
        iv_fourthday_weather = (ImageView) findViewById(R.id.iv_fourthday_weather);
        iv_fifthday_weather = (ImageView) findViewById(R.id.iv_fifthday_weather);

        iv_city.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, CityActivity.class);
                startActivityForResult(intent, 1);
            }
        });

        ll_moreinfo = (LinearLayout) findViewById(R.id.ll_moreinfo);
        ll_moreinfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, MoreInfoActivity.class);
                if(!isConnectNet()){
                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
                    jsonPm = sp.getString("pm", "");
                }
                intent.putExtra("jsonPm", jsonPm);
                startActivity(intent);
            }
        });
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
                            jsonToWeatherBean(responseString);
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

    private void jsonToWeatherBean(String jsonWeather) throws JSONException {
        if(jsonWeather == null){
            return;
        }
        JSONObject jsonObject = new JSONObject(jsonWeather);
        weatherBean = new WeatherBean();
        JSONObject result = jsonObject.getJSONObject("result");
        weatherBean.setTemp(result.getJSONObject("sk").getString("temp"));
        weatherBean.setTime(result.getJSONObject("sk").getString("time"));
        weatherBean.setHumidity(result.getJSONObject("sk").getString("humidity"));
        weatherBean.setTemperature(result.getJSONObject("today").getString("temperature"));
        weatherBean.setWeather(result.getJSONObject("today").getString("weather"));
        weatherBean.setFa(result.getJSONObject("today").getJSONObject("weather_id").getString("fa"));
        weatherBean.setWind(result.getJSONObject("today").getString("wind"));
        weatherBean.setCity(result.getJSONObject("today").getString("city"));
        weatherBean.setDate_y(result.getJSONObject("today").getString("date_y"));
        weatherBean.setDressing_index(result.getJSONObject("today").getString("dressing_index"));
        weatherBean.setDressing_adivce(result.getJSONObject("today").getString("dressing_advice"));
        weatherBean.setUv_index(result.getJSONObject("today").getString("uv_index"));

        JSONArray jsonArray = result.getJSONArray("future");
        List<FutureWeatherBean> list = new ArrayList<>();
        FutureWeatherBean futureWeatherBean;
        if (null != jsonArray && jsonArray.length() >= 5) {
            for (int i = 0; i < 5; i++) {
                futureWeatherBean = new FutureWeatherBean();
                futureWeatherBean.setTemperature(jsonArray.getJSONObject(i).getString("temperature"));
                futureWeatherBean.setWeather_id(
                        jsonArray.getJSONObject(i).getJSONObject("weather_id").getString("fa"));
                futureWeatherBean.setWeek(jsonArray.getJSONObject(i).getString("week"));

                list.add(futureWeatherBean);
            }
            weatherBean.setFutureWeatherList(list);
        }
        showWeather(weatherBean);
    }

    private void saveWeather(String jsonWeather) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(mContext).edit();
        editor.putString("weather", jsonWeather);
        editor.commit();
    }

    private void showSharedPreferenceWeather() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        String jsonWeather = sp.getString("weather", "");
        Log.v("WeatherActivity", jsonWeather);
        if(jsonWeather == null || "".equals(jsonWeather)){
            return;
        }
        try {
            jsonToWeatherBean(jsonWeather);
            showWeather(weatherBean);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void showWeather(WeatherBean weatherBean) {

        tv_city.setText(weatherBean.getCity());
        tv_date.setText(weatherBean.getDate_y().substring(5));
        tv_now_temp.setText(weatherBean.getTemp() + "℃");
        tv_now_weather.setText(weatherBean.getWeather());
        tv_today_temp.setText(weatherBean.getTemperature());

        int time = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String preffix;
        if (time >= 6 && time < 18) {
            preffix = "d";
        } else {
            preffix = "n";
        }

        List<FutureWeatherBean> list = weatherBean.getFutureWeatherList();

        iv_now_weather.setImageResource(getResources().
                getIdentifier(preffix + list.get(0).getWeather_id(),
                        "drawable", "com.juhe.simpleweather"));
        tv_dressing.setText(weatherBean.getDressing_index());
        tv_wind.setText(weatherBean.getWind());
        tv_uv_index.setText(weatherBean.getUv_index());

        showFutureWeather(null, iv_today_weather, tv_today_temp_a, tv_today_temp_b, list.get(0));
        showFutureWeather(tv_tomorrow, iv_tomorrow_weather, tv_tomorrow_temp_a, tv_tomorrow_temp_b, list.get(1));
        showFutureWeather(tv_thirdday, iv_thirdday_weather, tv_thirdday_temp_a, tv_thirdday_temp_b, list.get(2));
        showFutureWeather(tv_fourthday, iv_fourthday_weather, tv_fourthday_temp_a, tv_fourthday_temp_b, list.get(3));
        showFutureWeather(tv_fifthday, iv_fifthday_weather, tv_fifthday_temp_a, tv_fifthday_temp_b, list.get(4));
    }

    private void showFutureWeather(TextView tv_week, ImageView iv_weather,
                                   TextView tv_temp_a, TextView tv_temp_b, FutureWeatherBean futureWeatherBean) {
        if (tv_week != null) {
            tv_week.setText(futureWeatherBean.getWeek());
        }
        if (iv_weather != null) {
            int time = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            String preffix;
            if (time >= 6 && time < 18) {
                preffix = "d";
            } else {
                preffix = "n";
            }
            iv_weather.setImageResource(getResources().
                    getIdentifier(preffix + futureWeatherBean.getWeather_id(),
                            "drawable", "com.juhe.simpleweather"));
        }
        String[] strings = futureWeatherBean.getTemperature().split("~");
        tv_temp_a.setText(strings[0]);
        tv_temp_b.setText(strings[1]);
    }

    private void getPm() {
        if(pm_city == null){
            return ;
        }
        Parameters params = new Parameters();
        params.add("city", pm_city);

        JuheData.executeWithAPI(mContext, 33, "http://web.juhe.cn:8080/environment/air/pm",
                JuheData.GET, params, new DataCallBack() {
                    @Override
                    public void onSuccess(int statusCode, String responseString) {
                        try {
                            JSONObject jsonObject = new JSONObject(responseString);
                            String resultcode = jsonObject.getString("resultcode");
                            //返回的不是天气信息
                            if ("200".equals(resultcode)) {
                                jsonPm = responseString;
                                savePm(responseString);
                                jsonToPmBean(responseString);
                            } else {
                                if (pm_city != null) {
                                    Toast.makeText(mContext, R.string.pm_null, Toast.LENGTH_SHORT).show();
                                    showPMNull();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFinish() {

                    }

                    @Override
                    public void onFailure(int statusCode, String responseString, Throwable throwable) {
                        Toast.makeText(mContext, R.string.search_failed, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void jsonToPmBean(String jsonPm) throws JSONException {
        JSONObject jsonObject = new JSONObject(jsonPm);
        JSONObject jObject = (JSONObject) (jsonObject.getJSONArray("result").get(0));

        pmBean = new PmBean();
        pmBean.setPm(jObject.getString("PM2.5"));
        pmBean.setAqi(jObject.getString("AQI"));
        pmBean.setQuality(jObject.getString("quality"));
        pmBean.setPm10(jObject.getString("PM10"));
        pmBean.setCo(jObject.getString("CO"));
        pmBean.setNo2(jObject.getString("NO2"));
        pmBean.setSo2(jObject.getString("SO2"));

        showPm(pmBean);
    }

    private void savePm(String jsonPm) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(mContext).edit();
        editor.putString("pm", jsonPm);
        editor.commit();
    }

    private void showSharedPreferencePm() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        String jsonPm = sp.getString("pm", "");
        Log.v("WeatherActivity", jsonPm);
        if(jsonPm == null || "".equals(jsonPm)){
            return;
        }
        try {
            jsonToPmBean(jsonPm);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void showPm(PmBean pmBean) {
        tv_aqi.setText(pmBean.getAqi());
        tv_quality.setText(pmBean.getQuality());
    }

    private void showPMNull() {
        tv_aqi.setText(R.string.default_text);
        tv_quality.setText(R.string.default_text);
    }

    //处理城市返回的结果
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    city = data.getStringExtra("district");
                    pm_city = data.getStringExtra("city");
                    getWeather();
                    getPm();
                }
        }
    }

    @Override
    protected void onDestroy() {
        unbindService(cityConnection);
        cityService.stopSelf();
        super.onDestroy();
    }

}
