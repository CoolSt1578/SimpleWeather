package com.juhe.simpleweather.service;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.juhe.simpleweather.R;
import com.thinkland.sdk.android.DataCallBack;
import com.thinkland.sdk.android.JuheData;
import com.thinkland.sdk.android.Parameters;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Administrator on 2016/5/6.
 */
public class CityService extends Service {

    public static final int GET_CITY = 1;
    public static final int CITY_OK = 2;

    private Context mContext;
    private CityBinder cityBinder = new CityBinder();
    private CityCallBack cityCallBack;
    //定位
    private LocationManager locationManager;
    private String provider;
    //经度、纬度
    private String latitude;
    private String longitude;
    private String[] cities = new String[2];

    CountDownLatch countDownLatch = new CountDownLatch(1);
    //定位监听器
    LocationListener locationListenner = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {

        }
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }
        @Override
        public void onProviderEnabled(String provider) {

        }
        @Override
        public void onProviderDisabled(String provider) {
        }
    };

    public void setCityCallBack(CityCallBack cityCallBack){
        this.cityCallBack = cityCallBack;
    }

    public void removeCityCallBack(){
        cityCallBack = null;
    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case GET_CITY:
                    getDistrict();
                    break;
                case CITY_OK:
                    Log.v("CityService", cities[0]+"2"+cities[1]);
                    cityCallBack.onCityCallback(cities);
                    break;
                default:
                    break;
            }
        }
    };

    //定位方法
    public void getDistrict() {
        getLocation();
        getCity();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    countDownLatch.await();
                    handler.sendEmptyMessage(CITY_OK);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    //获得经纬度
    private void getLocation() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        List<String> providerList = locationManager.getProviders(true);
        if (providerList.contains(LocationManager.GPS_PROVIDER)) {
            provider = LocationManager.GPS_PROVIDER;
        } else if (providerList.contains(LocationManager.NETWORK_PROVIDER)) {
            provider = LocationManager.NETWORK_PROVIDER;
        } else {
            //没有课定位的服务
            Toast.makeText(mContext, R.string.location, Toast.LENGTH_SHORT).show();
            return;
        }
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location location = locationManager.getLastKnownLocation(provider);
        locationManager.requestLocationUpdates(provider, 5000, 1000, locationListenner);

        if (location != null) {
            latitude = String.valueOf(location.getLatitude());
            longitude = String.valueOf(location.getLongitude());
        }
    }

    //定位
    private void getCity() {
        Parameters params = new Parameters();
        params.add("lng", longitude);
        params.add("lat", latitude);
        params.add("type", 3);

        JuheData.executeWithAPI(mContext, 15, "http://apis.juhe.cn/geo/", JuheData.GET, params, new DataCallBack() {
            @Override
            public void onSuccess(int statusCode, String responseString) {
                try {
                    JSONObject jsonObject = new JSONObject(responseString);
                    String resultcode = jsonObject.getString("resultcode");
                    if ("200".equals(resultcode)) {
                        String location_city = jsonObject.getJSONObject("result").getJSONObject("ext").getString("city");
                        cities[0] = location_city.substring(0, location_city.lastIndexOf("市"));
                        cities[1] = cities[0];
                        countDownLatch.countDown();
                        Log.v("CityService", cities[0]+"1"+cities[1]);
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

            }
        });
    }

    //移除定位监听器
    public void removeLocationListenner() {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.removeUpdates(locationListenner);
    }


    @Override
    public IBinder onBind(Intent intent) {
        return cityBinder;
    }

    @Override
    public void onCreate() {
        mContext = this;
        handler.sendEmptyMessage(GET_CITY);
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy(){
        if(locationManager != null){
            removeLocationListenner();
        }
        super.onDestroy();
    }

    public class CityBinder extends Binder {
        public CityService getService() {
            return CityService.this;
        }
    }

    public interface CityCallBack{
        void onCityCallback(String[] cities);
    }
}
