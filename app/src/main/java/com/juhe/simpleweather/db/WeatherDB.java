package com.juhe.simpleweather.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.juhe.simpleweather.bean.CityBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by Administrator on 2016/4/29.
 */
public class WeatherDB {

    public static final String DB_NAME = "city";

    /**
     * 数据库版本
     */
    public static final int VERSION = 1;

    private static WeatherDB weatherDB;

    private SQLiteDatabase db;

    private WeatherDB(Context context){
        WeatherOpenHelper dbHelper = new WeatherOpenHelper(context, DB_NAME, null, VERSION);
        db = dbHelper.getWritableDatabase();
    }

    //获取WeatherDB的实例
    public synchronized static WeatherDB getIntance(Context context){
        if(weatherDB == null){
            weatherDB = new WeatherDB(context);
        }
        return weatherDB;
    }

    //将CityBean实例存储到数据库
    public void saveCityBean(CityBean cityBean){
        if (cityBean != null){
            String sql = "insert into City (province_name, city_name, district_name) values (?, ?, ?)";
            db.execSQL(sql, new String[] {cityBean.getProvince(), cityBean.getCity(), cityBean.getDistrict()});
        }
    }

    public boolean isNotNull(){
        Cursor cursor = db.rawQuery("select province_name from City where province_name = ?", new String[]{"北京"});
        if(cursor.moveToFirst()){
            cursor.close();
            return true;
        }else{
            cursor.close();
            return false;
        }
    }

    //从数据库读取所有省份信息
    public List<String> getProvinceList(){
        List<String> provinceList = new ArrayList<>();
        Set<String> set = new TreeSet<>();
        Cursor cursor = db.rawQuery("select province_name from City", null);
        if(cursor.moveToFirst()){
            do{
                String province_name = cursor.getString(cursor.getColumnIndex("province_name"));
                set.add(province_name);
            } while (cursor.moveToNext());
        }
        cursor.close();
        provinceList.addAll(set);
        return provinceList;
    }

    //从数据库读取某省份下城市信息
    public List<String> getCityList(String province_name){
        List<String> cityList = new ArrayList<>();
        Set<String> set = new TreeSet<>();
        Cursor cursor = db.rawQuery("select city_name from City where province_name = ?", new String[]{province_name});
        if(cursor.moveToFirst()){
            do{
                String city_name = cursor.getString(cursor.getColumnIndex("city_name"));
                set.add(city_name);
            } while (cursor.moveToNext());
        }
        cursor.close();
        cityList.addAll(set);
        return cityList;
    }

    //从数据库读取某城市下地区信息
    public List<String> getDistrictList(String city_name){
        List<String> districtList = new ArrayList<>();
        Cursor cursor = db.rawQuery("select district_name from City where city_name = ?", new String[]{city_name});
        if(cursor.moveToFirst()){
            do{
                String district_name = cursor.getString(cursor.getColumnIndex("district_name"));
                districtList.add(district_name);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return districtList;
    }
}
