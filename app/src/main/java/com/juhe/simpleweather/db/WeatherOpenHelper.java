package com.juhe.simpleweather.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

/**
 * Created by Administrator on 2016/4/29.
 */
public class WeatherOpenHelper extends SQLiteOpenHelper {

    public static final String CREATE_CITY = "create table City("
            + "id integer primary key autoincrement, "
            + "province_name text, "
            + "city_name text, "
            + "district_name text) ";

    public WeatherOpenHelper(Context context, String name, CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_CITY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
