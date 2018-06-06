package com.juhe.simpleweather.bean;

/**
 * Created by Gdd on 2016/4/21.
 */
public class FutureWeatherBean {

    private String temperature; //气温
    private String weather; //天气
    private String weather_id;  //天气图标
    private String week;    //周几

    public String getTemperature() {
        return temperature;
    }

    public String getWeather() {
        return weather;
    }

    public String getWeather_id() {
        return weather_id;
    }

    public String getWeek() {
        return week;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public void setWeather(String weather) {
        this.weather = weather;
    }

    public void setWeather_id(String weather_id) {
        this.weather_id = weather_id;
    }

    public void setWeek(String week) {
        this.week = week;
    }
}
