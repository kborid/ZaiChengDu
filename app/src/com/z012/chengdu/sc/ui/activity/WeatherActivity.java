package com.z012.chengdu.sc.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.widget.custom.MyListViewWidget;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshScrollView;
import com.prj.sdk.net.bean.ResponseData;
import com.prj.sdk.net.data.DataCallback;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.util.DateUtil;
import com.prj.sdk.util.StringUtil;
import com.prj.sdk.util.UIHandler;
import com.z012.chengdu.sc.R;
import com.z012.chengdu.sc.api.RequestBeanBuilder;
import com.z012.chengdu.sc.app.SessionContext;
import com.z012.chengdu.sc.constants.NetURL;
import com.z012.chengdu.sc.net.bean.WeatherCityInfo;
import com.z012.chengdu.sc.net.bean.WeatherForHomeBean;
import com.z012.chengdu.sc.net.bean.WeatherFutureInfoBean;
import com.z012.chengdu.sc.tools.WeatherInfoController;
import com.z012.chengdu.sc.ui.adapter.WeatherAdapter;
import com.z012.chengdu.sc.ui.base.BaseActivity;

import java.util.ArrayList;
import java.util.List;

public class WeatherActivity extends BaseActivity implements DataCallback {

    private static final int SELECTED_OK = 0x001;

    private PullToRefreshScrollView pullToRefreshSV;
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
    private WeatherForHomeBean weatherInfo = null;
    private String weatherCityCode;
    private String tempCityCode;
    private ArrayList<WeatherFutureInfoBean> list = new ArrayList<WeatherFutureInfoBean>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ui_weather_act);
        initViews();
        dealIntent();
        initParams();
        initListeners();
    }

    @Override
    public void initViews() {
        // TODO Auto-generated method stub
        super.initViews();
        pullToRefreshSV = (PullToRefreshScrollView) findViewById(R.id.scroll_view);
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
    }

    @Override
    public void dealIntent() {
        // TODO Auto-generated method stub
        super.dealIntent();
        Bundle bundle = getIntent().getExtras();
        if (bundle != null && bundle.getSerializable("weatherInfo") != null) {
            weatherInfo = (WeatherForHomeBean) bundle
                    .getSerializable("weatherInfo");
        }
        weatherCityCode = getString(R.string.cityId);
    }

    @Override
    public void initParams() {
        // TODO Auto-generated method stub
        super.initParams();
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
        // TODO Auto-generated method stub
        super.initListeners();
        tv_addr.setOnClickListener(this);
        pullToRefreshSV
                .setOnRefreshListener(new OnRefreshListener<ScrollView>() {

                    @Override
                    public void onRefresh(
                            PullToRefreshBase<ScrollView> refreshView) {
                        // TODO Auto-generated method stub
                        requestWeahterByCity(weatherCityCode);
                    }
                });
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        super.onClick(v);
        switch (v.getId()) {
            case R.id.tv_addr:
                Intent intent = new Intent(this, WeatherSelectActivity.class);
                startActivityForResult(intent, SELECTED_OK);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        if (!weatherCityCode.equals(tempCityCode)) {
            if (tempCityCode != null) {
                weatherCityCode = tempCityCode;
                UIHandler.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        pullToRefreshSV.setRefreshing();
                    }
                }, 300);
            }
        }
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }

    private void setWeather(WeatherForHomeBean temp) {
        if (StringUtil.notEmpty(temp.limitnumber)) {
            limit_lay.setVisibility(View.VISIBLE);
            String[] tmp = temp.limitnumber.split("\\|");
            tv_limit1.setText(tmp[0]);
            tv_limit2.setText(tmp[1]);
        } else {
            limit_lay.setVisibility(View.GONE);
        }

        String week = DateUtil.dateToWeek(DateUtil.str2Date(
                temp.savedate_weather, "yyyy-MM-dd"));
        tv_date.setText(temp.savedate_weather.replace("-", ".") + " " + week);
        tv_temp.setText(temp.temperature2 + "º" + " - " + temp.temperature1
                + "º");
        int weatherRes = 0;
        if (DateUtil.getDayOrNight()) {
            weatherRes = WeatherInfoController.getWeatherResForNight(temp.status2);
        } else {
            weatherRes = WeatherInfoController.getWeatherResForDay(temp.status1);
        }
        iv_weather.setImageResource(weatherRes);
        tv_weather.setText(temp.status1);
        weather_lay.setBackgroundResource(WeatherInfoController
                .getWeatherInfoBg(temp.status1, temp.status2));

        tv_pm.setText(temp.pmdata + temp.pmdesc);
        try {
            int n = Integer.parseInt(temp.pmdata);
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
        // TODO Auto-generated method stub
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
        // TODO Auto-generated method stub

    }

    @Override
    public void notifyMessage(ResponseData request, ResponseData response)
            throws Exception {
        // TODO Auto-generated method stub
        if (request.flag == 1) {
            pullToRefreshSV.onRefreshComplete();
            String res = response.body.toString();
            WeatherForHomeBean bean = JSON.parseObject(res,
                    WeatherForHomeBean.class);
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
        // TODO Auto-generated method stub
        pullToRefreshSV.onRefreshComplete();
    }
}
