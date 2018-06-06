package com.juhe.simpleweather.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.juhe.simpleweather.R;
import com.juhe.simpleweather.bean.CityBean;
import com.juhe.simpleweather.db.WeatherDB;
import com.thinkland.sdk.android.DataCallBack;
import com.thinkland.sdk.android.JuheData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Gdd on 2016/4/27.
 */
public class CityActivity extends Activity{

    //页面级别
    public static final int LEVEL_PROVINCE = 1;
    public static final int LEVEL_CITY = 2;
    public static final int LEVEL_DISTRICT = 3;

    private ProgressDialog progressDialog;
    CountDownLatch countDownLatch = new CountDownLatch(1);

    //当前页面级别
    private int current_level;

    private Context mContext;

    private WeatherDB weatherDB;
    private List<String> dataList = new ArrayList<>();

    //省列表
    private List<String> provinceList;
    //市列表
    private List<String> cityList;
    //地区列表
    private List<String> districtList;
    //选中的身份
    private String selected_province;
    //选中的城市
    private String selected_city;
    //选中的天气地区
    private String district;

    private ImageView iv_back;
    private ListView list_city;

    private ArrayAdapter<String> adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city);

        init();

        //加载省份信息
        if(weatherDB.isNotNull()){
            queryProvince();
        }else{
            showDialog();
            sendRequest();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        countDownLatch.await();
                        closeDialog();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    //初始化
    private void init() {

        mContext = this;
        weatherDB = WeatherDB.getIntance(mContext);

        //返回按钮
        iv_back = (ImageView) findViewById(R.id.iv_back);
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(current_level == LEVEL_DISTRICT){
                    queryCity();
                }else if(current_level == LEVEL_CITY){
                    queryProvince();
                }else{
                    finish();
                }
            }
        });

        //列表
        list_city = (ListView) findViewById(R.id.list_city);
        adapter = new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1, dataList);
        list_city.setAdapter(adapter);
        list_city.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (current_level == LEVEL_PROVINCE) {
                    selected_province = provinceList.get(position);
                    queryCity();
                } else if (current_level == LEVEL_CITY) {
                    selected_city = cityList.get(position);
                    queryDistrict();
                } else {
                    district = districtList.get(position);
                    Intent intent = new Intent();
                    intent.putExtra("city", selected_city);
                    intent.putExtra("district", district);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });
    }

    //获取城市列表
    private void sendRequest() {
        //从服务器取得数据
        JuheData.executeWithAPI(mContext, 39, "http://v.juhe.cn/weather/citys",
                JuheData.GET, null, new DataCallBack() {
                    @Override
                    public void onSuccess(int statusCode, String responseString) {
                        try {
                            JSONObject jsonObject = new JSONObject(responseString);
                            String resultCode = jsonObject.getString("resultcode");
                            if (!"200".equals(resultCode)) {
                                return;
                            }
                            JSONArray jsonArray = jsonObject.getJSONArray("result");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                CityBean cityBean = new CityBean();
                                cityBean.setProvince(jsonArray.getJSONObject(i).getString("province"));
                                cityBean.setCity(jsonArray.getJSONObject(i).getString("city"));
                                cityBean.setDistrict(jsonArray.getJSONObject(i).getString("district"));
                                weatherDB.saveCityBean(cityBean);
                            }
                            countDownLatch.countDown();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        queryProvince();
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

    //查询省份信息
    private void queryProvince(){
        provinceList = weatherDB.getProvinceList();
        if(provinceList.size() > 0){
            dataList.clear();
            for(String province : provinceList){
                dataList.add(province);
            }
            adapter.notifyDataSetChanged();
            list_city.setSelection(0);
            current_level = LEVEL_PROVINCE;
        }
    }

    //查询选中省份下的城市信息
    private void queryCity() {
        cityList = weatherDB.getCityList(selected_province);
        if (cityList.size() > 0) {
            dataList.clear();
            for (String city : cityList) {
                dataList.add(city);
            }
            adapter.notifyDataSetChanged();
            list_city.setSelection(0);
            current_level = LEVEL_CITY;
        }
    }

    //查询选中城市下的地区信息
    private void queryDistrict() {
        districtList = weatherDB.getDistrictList(selected_city);
        if(districtList.size() > 0){
            dataList.clear();
            for(String district : districtList){
                dataList.add(district);
            }
            adapter.notifyDataSetChanged();
            list_city.setSelection(0);
            current_level = LEVEL_DISTRICT;
        }
    }

    //显示进度框
    private void showDialog(){
        if(progressDialog == null){
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    //关闭进度框
    private void closeDialog(){
        if(progressDialog != null){
            progressDialog.dismiss();
        }
    }
}
