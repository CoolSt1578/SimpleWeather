package com.juhe.simpleweather.bean;

import java.util.List;

/**
 * Created by Gdd on 2016/4/21.
 */
public class WeatherBean {

    private String temp;    //实时温度
    private String humidity;    //湿度
    private String time;    //时间

    private String city;
    private String temperature; //今天温度
    private String weather;     //今天天气
    private String fa;  //今天天气id
    private String wind;        //今天风力
    private String date_y;      //日期
    private String dressing_index;  //穿衣指数
    private String dressing_adivce; //穿衣建议
    private String uv_index;    //紫外线指数

    private List<FutureWeatherBean> futureWeatherList;

    public String getTemp() {
        return temp;
    }

    public String getTemperature() {
        return temperature;
    }

    public String getWeather() {
        return weather;
    }

    public String getFa() {
        return fa;
    }

    public String getWind() {
        return wind;
    }

    public String getDate_y() {
        return date_y;
    }

    public String getDressing_index() {
        return dressing_index;
    }

    public String getDressing_adivce() {
        return dressing_adivce;
    }

    public String getUv_index() {
        return uv_index;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public List<FutureWeatherBean> getFutureWeatherList() {
        return futureWeatherList;
    }

    public void setTemp(String temp) {
        this.temp = temp;
    }

    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public void setWeather(String weather) {
        this.weather = weather;
    }

    public void setFa(String fa) {
        this.fa = fa;
    }

    public void setWind(String wind) {
        this.wind = wind;
    }

    public void setDate_y(String date_y) {
        this.date_y = date_y;
    }

    public void setDressing_index(String dressing_index) {
        this.dressing_index = dressing_index;
    }

    public void setDressing_adivce(String dressing_adivce) {
        this.dressing_adivce = dressing_adivce;
    }

    public void setUv_index(String uv_index) {
        this.uv_index = uv_index;
    }

    public void setFutureWeatherList(List<FutureWeatherBean> futureWeatherList) {
        this.futureWeatherList = futureWeatherList;
    }
}
