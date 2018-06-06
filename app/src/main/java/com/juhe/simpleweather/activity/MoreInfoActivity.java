package com.juhe.simpleweather.activity;

import android.app.Activity;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.juhe.simpleweather.R;
import com.juhe.simpleweather.bean.PmBean;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2016/5/4.
 */
public class MoreInfoActivity extends Activity {


    private TextView tv_pm2,
            tv_pm10,
            tv_co,
            tv_no2,
            tv_so2;

    private ImageView iv_back;
    private String jsonPm;

    private PmBean pmBean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moreinfo);

        jsonPm = getIntent().getStringExtra("jsonPm");
        init();
        jsonToPmBean();
        showPm();
    }

    private void init() {

        pmBean = new PmBean();

        tv_pm2 = (TextView) findViewById(R.id.tv_pm2);
        tv_pm10 = (TextView) findViewById(R.id.tv_pm10);
        tv_co = (TextView) findViewById(R.id.tv_co);
        tv_no2 = (TextView) findViewById(R.id.tv_no2);
        tv_so2 = (TextView) findViewById(R.id.tv_so2);

        iv_back = (ImageView) findViewById(R.id.iv_back);

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void jsonToPmBean() {
        try {
            JSONObject jsonObject = new JSONObject(jsonPm);
            JSONObject result = (JSONObject) jsonObject.getJSONArray("result").get(0);
            pmBean.setPm(result.getString("PM2.5"));
            pmBean.setPm10(result.getString("PM10"));
            pmBean.setCo(result.getString("CO"));
            pmBean.setNo2(result.getString("NO2"));
            pmBean.setSo2(result.getString("SO2"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void showPm() {
        tv_pm2.setText(pmBean.getPm());
        tv_pm10.setText(pmBean.getPm10());
        tv_co.setText(pmBean.getCo());
        tv_no2.setText(pmBean.getNo2());
        tv_so2.setText(pmBean.getSo2());
    }

}
