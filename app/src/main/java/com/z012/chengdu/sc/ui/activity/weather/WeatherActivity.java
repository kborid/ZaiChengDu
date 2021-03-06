package com.z012.chengdu.sc.ui.activity.weather;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.widget.custom.MyListViewWidget;
import com.prj.sdk.net.bean.ResponseData;
import com.prj.sdk.net.data.DataCallback;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.util.DateUtil;
import com.prj.sdk.util.StringUtil;
import com.prj.sdk.util.UIHandler;
import com.z012.chengdu.sc.R;
import com.z012.chengdu.sc.SessionContext;
import com.z012.chengdu.sc.constants.NetURL;
import com.z012.chengdu.sc.helper.WeatherInfoHelper;
import com.z012.chengdu.sc.net.entity.WeatherCityInfo;
import com.z012.chengdu.sc.net.entity.WeatherFutureInfoBean;
import com.z012.chengdu.sc.net.entity.WeatherInfoBean;
import com.z012.chengdu.sc.net.request.RequestBeanBuilder;
import com.z012.chengdu.sc.ui.BaseActivity;
import com.z012.chengdu.sc.ui.adapter.WeatherAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.OnClick;

public class WeatherActivity extends BaseActivity implements DataCallback {

    private static final int SELECTED_OK = 0x001;

    //    private PullToRefreshScrollView pullToRefreshSV;
    private LinearLayout weather_lay;
    private RelativeLayout limit_lay;
    private TextView tv_addr;
    private TextView tv_date;
    private TextView tv_limit1;
    private TextView tv_limit2;
    private ImageView iv_weather;
    private TextView tv_temp;
    private TextView tv_weather;
    private TextView tv_pm;
    private MyListViewWidget weather_list;
    private WeatherAdapter adapter;
    private WeatherInfoBean weatherInfo = null;
    private String weatherCityCode;
    private String tempCityCode;
    private ArrayList<WeatherFutureInfoBean> list = new ArrayList<WeatherFutureInfoBean>();

    @Override
    protected int getLayoutResId() {
        return R.layout.ui_weather_act;
    }

    @Override
    public void dealIntent() {
        super.dealIntent();
        Bundle bundle = getIntent().getExtras();
        if (bundle != null && bundle.getSerializable("weatherInfo") != null) {
            weatherInfo = (WeatherInfoBean) bundle
                    .getSerializable("weatherInfo");
        }
        weatherCityCode = getString(R.string.cityId);
    }

    @Override
    public void initParams() {
        super.initParams();
//        pullToRefreshSV = (PullToRefreshScrollView) findViewById(R.id.scroll_view);
        weather_lay = (LinearLayout) findViewById(R.id.weather_lay);
        limit_lay = (RelativeLayout) findViewById(R.id.limit_lay);
        tv_addr = (TextView) findViewById(R.id.tv_addr);
        tv_date = (TextView) findViewById(R.id.tv_date);
        tv_limit1 = (TextView) findViewById(R.id.tv_limit1);
        tv_limit2 = (TextView) findViewById(R.id.tv_limit2);
        iv_weather = (ImageView) findViewById(R.id.iv_weather);
        tv_temp = (TextView) findViewById(R.id.tv_temp);
        tv_weather = (TextView) findViewById(R.id.tv_weather);
        tv_pm = (TextView) findViewById(R.id.tv_pm);
        weather_list = (MyListViewWidget) findViewById(R.id.weather_list);

        tv_addr.setText(SessionContext.getAreaInfo(0));
        tv_center_title.setText("天气");
        if (weatherInfo != null) {
            setWeather(weatherInfo);
        }

        if (SessionContext.getWeatherInfo() != null) {
            list.clear();
            list.addAll(SessionContext.getWeatherInfo());
        }
        adapter = new WeatherAdapter(this, list);
        weather_list.setAdapter(adapter);
    }

    @Override
    public void initListeners() {
        super.initListeners();
//        pullToRefreshSV
//                .setOnRefreshListener(new OnRefreshListener<ScrollView>() {
//
//                    @Override
//                    public void onRefresh(
//                            PullToRefreshBase<ScrollView> refreshView) {
//                        requestWeahterByCity(weatherCityCode);
//                    }
//                });
    }

    @OnClick(R.id.tv_addr)
    void address() {
        startActivityForResult(new Intent(this, WeatherSelectActivity.class), SELECTED_OK);
    }

    @Override
    protected void onResume() {
        if (!weatherCityCode.equals(tempCityCode)) {
            if (tempCityCode != null) {
                weatherCityCode = tempCityCode;
                UIHandler.postDelayed(new Runnable() {

                    @Override
                    public void run() {
//                        pullToRefreshSV.setRefreshing();
                    }
                }, 300);
            }
        }
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void setWeather(WeatherInfoBean temp) {
        if (StringUtil.notEmpty(temp.getLimitnumber())) {
            limit_lay.setVisibility(View.VISIBLE);
            String[] tmp = temp.getLimitnumber().split("\\|");
            tv_limit1.setText(tmp[0]);
            tv_limit2.setText(tmp[1]);
        } else {
            limit_lay.setVisibility(View.GONE);
        }

        String week = DateUtil.dateToWeek(DateUtil.str2Date(
                temp.getSavedate_weather(), "yyyy-MM-dd"));
        tv_date.setText(temp.getSavedate_weather().replace("-", ".") + " " + week);
        tv_temp.setText(temp.getTemperature2() + "º" + " - " + temp.getTemperature1()
                + "º");
        int weatherRes = 0;
        if (DateUtil.getDayOrNight()) {
            weatherRes = WeatherInfoHelper.getWeatherResForNight(temp.getStatus2());
        } else {
            weatherRes = WeatherInfoHelper.getWeatherResForDay(temp.getStatus1());
        }
        iv_weather.setImageResource(weatherRes);
        tv_weather.setText(temp.getStatus1());
        weather_lay.setBackgroundResource(WeatherInfoHelper
                .getWeatherInfoBg(temp.getStatus1(), temp.getStatus2()));

        tv_pm.setText(temp.getPmdata() + temp.getPmdesc());
        try {
            int n = Integer.parseInt(temp.getPmdata());
            if (n < 100) {// 优
                tv_pm.setBackgroundResource(R.drawable.pm2_5_1bg);
            } else if (n > 100 && n < 200) {// 良
                tv_pm.setBackgroundResource(R.drawable.pm2_5_2bg);
            } else {
                tv_pm.setBackgroundResource(R.drawable.pm2_5_3bg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void requestWeahterByCity(String cityCode) {
        RequestBeanBuilder b = RequestBeanBuilder.create(false);
        b.addBody("cityCode", SessionContext.getAreaInfo(1));
        b.addBody("nowdate", DateUtil.getCurDateStr("yyyy-MM-dd"));
        b.addBody("cityId", cityCode);
        ResponseData d = b.syncRequest(b);
        d.path = NetURL.WEATHER_SERVER;
        d.flag = 1;
        requestID = DataLoader.getInstance().loadData(this, d);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SELECTED_OK:
                if (data != null) {
                    WeatherCityInfo info = (WeatherCityInfo) data
                            .getSerializableExtra("city");
                    tv_addr.setText(info.name);
                    tempCityCode = info.code;
                }
                break;

            default:
                break;
        }
    }

    @Override
    public void preExecute(ResponseData request) {
    }

    @Override
    public void notifyMessage(ResponseData request, ResponseData response)
            throws Exception {
        if (request.flag == 1) {
//            pullToRefreshSV.onRefreshComplete();
            String res = response.body.toString();
            WeatherInfoBean bean = JSON.parseObject(res,
                    WeatherInfoBean.class);
            JSONObject json = JSON.parseObject(res);
            String future = json.getString("future");
            List<WeatherFutureInfoBean> temp = JSON.parseArray(future,
                    WeatherFutureInfoBean.class);
            SessionContext.setWeatherInfo(temp);
            if (bean != null) {
                setWeather(bean);
            }
            list.clear();
            list.addAll(SessionContext.getWeatherInfo());
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void notifyError(ResponseData request, ResponseData response,
                            Exception e) {
//        pullToRefreshSV.onRefreshComplete();
    }
}
