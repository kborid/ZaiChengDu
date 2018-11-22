package com.z012.chengdu.sc.ui.fragment;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshScrollView;
import com.prj.sdk.net.bean.ResponseData;
import com.prj.sdk.net.data.DataCallback;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.util.DateUtil;
import com.prj.sdk.util.NetworkUtil;
import com.prj.sdk.util.Utils;
import com.prj.sdk.widget.CustomToast;
import com.z012.chengdu.sc.R;
import com.z012.chengdu.sc.api.RequestBeanBuilder;
import com.z012.chengdu.sc.app.SessionContext;
import com.z012.chengdu.sc.constants.AppConst;
import com.z012.chengdu.sc.constants.NetURL;
import com.z012.chengdu.sc.net.bean.HomeBannerInfoBean;
import com.z012.chengdu.sc.net.bean.NewsBean;
import com.z012.chengdu.sc.net.bean.PushAppBean;
import com.z012.chengdu.sc.net.bean.WeatherForHomeBean;
import com.z012.chengdu.sc.net.bean.WeatherFutureInfoBean;
import com.z012.chengdu.sc.tools.WeatherInfoController;
import com.z012.chengdu.sc.ui.activity.HtmlActivity;
import com.z012.chengdu.sc.ui.activity.SearchActivity;
import com.z012.chengdu.sc.ui.adapter.GridViewAdapter;
import com.z012.chengdu.sc.ui.base.BaseFragment;
import com.z012.chengdu.sc.ui.dialog.CustomDialogUtil;
import com.z012.chengdu.sc.ui.dialog.CustomDialogUtil.onCallBackListener;
import com.z012.chengdu.sc.ui.widge.banner.CommonBannerLayout;
import com.z012.chengdu.sc.ui.widge.maqueue.IUPMarqueeListener;
import com.z012.chengdu.sc.ui.widge.maqueue.UPMarqueeBean;
import com.z012.chengdu.sc.ui.widge.maqueue.UPMarqueeView;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;

/**
 * 首页
 * 
 * @author kborid
 */
public class TabHomeFragment extends BaseFragment implements DataCallback, OnRefreshListener2<ScrollView> {

    private LinearLayout ll_title_panel;
    private TextView tv_center_title;
    private LinearLayout weather_lay;
    private ImageView iv_weather_icon;
    private TextView tv_weather_temp, tv_weather_air;
    private PullToRefreshScrollView mPullToRefreshScrollView;
    private CommonBannerLayout banner_lay;
    private GridView mHotServiceGridView;
    private GridViewAdapter mHotServiceAdapter;
    private List<PushAppBean> mServiceApp = new ArrayList<>();
	private UPMarqueeView marqueeView;

    private boolean isRefresh;
    private SparseIntArray mTag = new SparseIntArray(); // 全部请求结束标记

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View view = inflater.inflate(R.layout.fragment_tab_home, container,
				false);
		initViews(view);
		initParams();
		initListeners();
		tieleShade();
		return view;
	}

	protected void onInits() {
	}

	protected void onVisible() {
		super.onVisible();
        banner_lay.startBanner();
	}

	protected void onInvisible() {
		super.onInvisible();
		banner_lay.stopBanner();
	}

	@Override
	protected void initViews(View view) {
		super.initViews(view);
		ll_title_panel = (LinearLayout) view.findViewById(R.id.ll_title_panel);
        marqueeView = (UPMarqueeView) view.findViewById(R.id.marqueeView);
        mHotServiceGridView = (GridView) view.findViewById(R.id.gridview);
		tv_center_title = (TextView) view.findViewById(R.id.tv_center_title);
		mPullToRefreshScrollView = (PullToRefreshScrollView) view.findViewById(R.id.scroll_view);
        banner_lay = (CommonBannerLayout) view.findViewById(R.id.banner);
        weather_lay = (LinearLayout) view.findViewById(R.id.weather_lay);
        iv_weather_icon = (ImageView) view.findViewById(R.id.iv_weather);
        tv_weather_temp = (TextView) view.findViewById(R.id.tv_temp);
        tv_weather_air = (TextView) view.findViewById(R.id.tv_air);
	}

	@Override
	protected void initParams() {
		super.initParams();
		setNetworkMethod();
        setAppItem();
        updateNewsInfo();
        if (NetworkUtil.isNetworkAvailable()) {
            showProgressDialog(getString(R.string.loading), false);
            requestBanner();
            requestWeather();
            requestNews();
            requestHotService();
        }

        RelativeLayout.LayoutParams weatherRlp = (RelativeLayout.LayoutParams) banner_lay.getLayoutParams();
        weatherRlp.width = Utils.mScreenWidth;
        weatherRlp.height = (int) ((float) weatherRlp.width / 375 * 200);
        banner_lay.setLayoutParams(weatherRlp);
        banner_lay.setIndicatorLayoutMarginBottom(Utils.dip2px(30));
        banner_lay.setIndicatorLayoutMarginLeft(Utils.dip2px(20));
	}

	/**
	 * 加载banner
	 */
	public void requestBanner() {
		RequestBeanBuilder builder = RequestBeanBuilder.create(false);
		// builder.addBody("dataSize", "5");
		// builder.addBody("times", mTimeStamp);
		// builder.addBody("code", mCode);
		// builder.addBody("dataStart", "0");
		builder.addBody("getConfForMgr", "YES");

		ResponseData data = builder.syncRequest(builder);
		data.path = NetURL.BANNER;
		data.flag = 8;
		requestID = DataLoader.getInstance().loadData(this, data);
	}

	@Override
	public void initListeners() {
		super.initListeners();
		tv_center_title.setOnClickListener(this);
		mPullToRefreshScrollView.setOnRefreshListener(this);
		marqueeView.setUPMarqueeListener(new IUPMarqueeListener() {
            @Override
            public void callback(UPMarqueeBean bean) {
                if (null != bean && !TextUtils.isEmpty(bean.getUrl())) {
                    Intent intent = new Intent(getActivity(), HtmlActivity.class);
                    intent.putExtra("path", bean.getUrl());
                    intent.putExtra("title", "今日头条");
                    intent.putExtra("id", bean.getId());
                    startActivity(intent);
                }
            }
        });
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tv_center_title:
			Intent intent = new Intent(getActivity(), SearchActivity.class);
			startActivity(intent);
			break;
		default:
			break;
		}
	}

	/**
	 * 设置推荐app应用
	 */
	public void setAppItem() {
	    if (null == mHotServiceAdapter) {
	        mHotServiceAdapter = new GridViewAdapter(getActivity(), mServiceApp);
            mHotServiceGridView.setAdapter(mHotServiceAdapter);
        }
        List<PushAppBean> temp = SessionContext.getAppList();
        if (temp.size() >= 7) {
            temp = temp.subList(0, 7);
            PushAppBean bean = new PushAppBean();
            bean.appname = "全部";
            bean.appurls = "ShowAllService";
            temp.add(bean);
        }
        mServiceApp.clear();
        mServiceApp.addAll(temp);
        mHotServiceAdapter.notifyDataSetChanged();
	}

	/**
	 * 加载推荐app
	 * 
	 */
	public void requestHotService() {
		RequestBeanBuilder builder = RequestBeanBuilder.create(false);
		// builder.addBody("areaId", SessionContext.getAreaInfo(1));
		builder.addBody("getConfForMgr", "YES");

		// Map<String, Object> header = new HashMap<String, Object>();
		// header.put("Content-Type", "application/json");

		ResponseData data = builder.syncRequest(builder);
		data.path = NetURL.PUSH_SERVICE_;
		// data.header = header;
		data.flag = 1;

		// if (!isProgressShowing())
		// showProgressDialog(getString(R.string.loading), true);
		requestID = DataLoader.getInstance().loadData(this, data);
	}

	/**
	 * 加载今日头条
	 * 
	 */
	public void requestNews() {
		RequestBeanBuilder builder = RequestBeanBuilder.create(false);

		// builder.addBody("getConfForMgr", "YES");

		ResponseData data = builder.syncRequest(builder);
		data.path = NetURL.NEWS;
		data.flag = 3;

		requestID = DataLoader.getInstance().loadData(this, data);
	}

	/**
	 * 加载推荐问答数据
	 * 
	 * @return
	 */
	private void loadQAData() {
		RequestBeanBuilder builder = RequestBeanBuilder.create(false);
		// mark填写firstpage值时接口返回首页热门问答5条列表
		builder.addBody("PAGE_INDEX", "1")
				.addBody("PAGE_COUNT", AppConst.COUNT)
				.addBody("mark", "firstpage");

		ResponseData data = builder.syncRequest(builder);
		data.path = NetURL.WG_ALL;
		data.flag = 2;
		requestID = DataLoader.getInstance().loadData(this, data);

	}

	/**
	 * 加载天气
	 */
	public void requestWeather() {
		RequestBeanBuilder b = RequestBeanBuilder.create(false);
		b.addBody("cityCode", SessionContext.getAreaInfo(1));
		b.addBody("cityId", getString(R.string.cityId));
		ResponseData d = b.syncRequest(b);
		d.path = NetURL.WEATHER_SERVER;
		d.flag = 4;
		requestID = DataLoader.getInstance().loadData(this, d);
	}

	/**
	 * 打开网络设置
	 */
	private void setNetworkMethod() {
		if (!NetworkUtil.isNetworkAvailable()) {// 判断网络是否可用
			CustomDialogUtil mTip = new CustomDialogUtil(getActivity());
			mTip.setBtnText("设置", "取消");
			mTip.show("网络连接不可用,是否进行设置?");
			mTip.setListeners(new onCallBackListener() {
				public void leftBtn(CustomDialogUtil dialog) {
					Intent intent = null;
					// 判断手机系统的版本 即API大于10 就是3.0或以上版本
					if (android.os.Build.VERSION.SDK_INT > 10) {
						intent = new Intent(
								android.provider.Settings.ACTION_WIRELESS_SETTINGS);
					} else {
						intent = new Intent();
						ComponentName component = new ComponentName(
								"com.android.settings",
								"com.android.settings.WirelessSettings");
						intent.setComponent(component);
						intent.setAction("android.intent.action.VIEW");
					}
					startActivity(intent);
					dialog.dismiss();
				}

				public void rightBtn(CustomDialogUtil dialog) {
					dialog.dismiss();
				}
			});
			mTip = null;// 释放
		}
	}

	@Override
	public void onPullDownToRefresh(PullToRefreshBase<ScrollView> refreshView) {
		isRefresh = true;
		ll_title_panel.setVisibility(View.GONE);
		mTag.clear();
		requestBanner();
		requestWeather();
		requestNews();
		requestHotService();
	}

	@Override
	public void onPullUpToRefresh(PullToRefreshBase<ScrollView> refreshView) {

	}

	@Override
	public void preExecute(ResponseData request) {

	}

	@Override
	public void notifyMessage(ResponseData request, ResponseData response)
			throws Exception {
		if (request.flag == 3) {// 头条
			mTag.put(request.flag, request.flag);
			JSONObject mJson = JSON.parseObject(response.body.toString());
			String json = mJson.getString("hotnewstodaylist");
			List<NewsBean> temp = JSON.parseArray(json, NewsBean.class);
			SessionContext.setNewsList(temp);
			updateNewsInfo();
		} else if (request.flag == 4) {// 气温，日期，天气
			mTag.put(request.flag, request.flag);
			String res = response.body.toString();
			WeatherForHomeBean weatherbean = JSON.parseObject(res, WeatherForHomeBean.class);
			JSONObject json = JSON.parseObject(res);
			String future = json.getString("future");
			List<WeatherFutureInfoBean> futureInfo = JSON.parseArray(future,
					WeatherFutureInfoBean.class);
			SessionContext.setWeatherInfo(futureInfo);
			setWeather(weatherbean);
		} else if (request.flag == 1) {// //热门服务
			mTag.put(request.flag, request.flag);
			JSONObject mJson = JSON.parseObject(response.body.toString());
			String json = mJson.getString("datalist");
			List<PushAppBean> temp = JSON.parseArray(json, PushAppBean.class);
			SessionContext.setAppList(temp);
			setAppItem();

//		} else if (request.flag == 2) {// 有问必答
//			mTag.put(request.flag, request.flag);
//			JSONObject mJson = JSON.parseObject(response.body.toString());
//			String json = mJson.getString("page");
//			QAListBean temp = JSON.parseObject(json, QAListBean.class);
//			List<QAListBean.Result> data = new ArrayList<QAListBean.Result>();
//			if (temp != null && temp.result != null && temp.result.size() > 3) {
//				data = temp.result.subList(0, 3);// 最多显示3个
//				mBean.clear();
//				mBean.addAll(data);
//			} else {
//				mBean.clear();
//				mBean.addAll(temp.result);
//			}
		} else if (request.flag == 8) {// banner
			mTag.put(request.flag, request.flag);
			JSONObject mJson = JSON.parseObject(response.body.toString());
			String json = mJson.getString("datalist");
			List<HomeBannerInfoBean> temp = JSON.parseArray(json, HomeBannerInfoBean.class);
//			if (null != temp && temp.size() > 0) {
//			    temp.remove(0);
//			    for (int i = 0; i < 4; i++) {
//			        temp.add(new HomeBannerInfoBean());
//                }
//            }
            banner_lay.setImageResource(temp);
		}

		if (mTag != null && mTag.size() == 4) {// 更新成功标记
			removeProgressDialog();
			mPullToRefreshScrollView.onRefreshComplete();
			ll_title_panel.setVisibility(View.VISIBLE);
			if (isRefresh) {
				isRefresh = false;
				CustomToast.show("更新成功", 0);
			}
		}

	}

	@Override
	public void notifyError(ResponseData request, ResponseData response,
			Exception e) {
		removeProgressDialog();
		mPullToRefreshScrollView.onRefreshComplete();
		ll_title_panel.setVisibility(View.VISIBLE);
		String message;
		if (e != null && e instanceof ConnectException) {
			message = getString(R.string.dialog_tip_net_error);
			CustomToast.show(message, Toast.LENGTH_LONG);
		} else {
			if (request.flag == 3 || request.flag == 4 || request.flag == 5
					|| request.flag == 6) {
				isRefresh = false;
				message = response != null && response.data != null ? response.data
						.toString() : getString(R.string.dialog_tip_null_error);
				CustomToast.show("首页部分信息加载失败，请下拉刷新", Toast.LENGTH_LONG);
			}
		}

	}

	private void updateNewsInfo() {
	    List<NewsBean> temp = SessionContext.getNewsList();
        if (temp != null && !temp.isEmpty()) {
            List<UPMarqueeBean> beans = new ArrayList<>();
            for (int i = 0; i < temp.size(); i++) {
                UPMarqueeBean bean = new UPMarqueeBean(temp.get(i));
                beans.add(bean);
            }
            if (beans.size() > 1 && beans.size() % 2 == 1) {
                beans.addAll(beans);
            }
            marqueeView.setViews(beans);
            marqueeView.startAnimal(beans.size());
        }
    }

	/**
	 * 设置天气
	 */
	private void setWeather(WeatherForHomeBean temp) {
	    if (null == temp) {
            weather_lay.setVisibility(View.GONE);
            return;
        }
		tv_weather_temp.setText(temp.temperature1 + "ºC");
//		String week = DateUtil.dateToWeek(DateUtil.str2Date(temp.savedate_weather, "yyyy-MM-dd"));
//		tv_date.setText(temp.savedate_weather.replace("-", ".") + " " + week);
		int weatherRes = 0;
		if (DateUtil.getDayOrNight()) {
			weatherRes = WeatherInfoController.getWeatherResForNight(temp.status2);
		} else {
			weatherRes = WeatherInfoController.getWeatherResForDay(temp.status1);
		}
		iv_weather_icon.setImageResource(weatherRes);
//		tv_weather.setText(temp.status1);
//		weather_lay.setBackgroundResource(WeatherInfoController.getWeatherInfoBg(temp.status1, temp.status2));

		tv_weather_air.setText("空气" + temp.pmdesc);

//		try {
//			int n = Integer.parseInt(temp.pmdata);
//			if (n < 100) {// 优
//				tv_pm.setBackgroundResource(R.drawable.pm2_5_1bg);
//			} else if (n > 100 && n < 200) {// 良
//				tv_pm.setBackgroundResource(R.drawable.pm2_5_2bg);
//			} else {
//				tv_pm.setBackgroundResource(R.drawable.pm2_5_3bg);
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
        weather_lay.setVisibility(View.VISIBLE);
	}

	/**
	 * 使标题栏渐变
	 */
	public void tieleShade() {
		ll_title_panel.getBackground().mutate().setAlpha(0);
		final int titleHeight = Utils.dip2px(50);
		mPullToRefreshScrollView
				.setOnScrollChangedListener(new PullToRefreshScrollView.OnScrollChangedListener() {

					@Override
					public void onScrollChanged(int l, int t, int oldl, int oldt) {
						if (banner_lay != null && banner_lay.getHeight() > 0) {
							// define it for scroll height
							int assignHeight = banner_lay.getHeight()
									- titleHeight;
							if (t < assignHeight) {
								int progress = (int) ((float) t / assignHeight * 255);
								ll_title_panel.getBackground().mutate()
										.setAlpha(progress);
							} else {
								ll_title_panel.getBackground().mutate()
										.setAlpha(255);
							}
						}
					}
				});
	}
}
