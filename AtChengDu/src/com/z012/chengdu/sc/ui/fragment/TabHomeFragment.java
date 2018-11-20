package com.z012.chengdu.sc.ui.fragment;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshScrollView;
import com.prj.sdk.app.AppContext;
import com.prj.sdk.net.bean.ResponseData;
import com.prj.sdk.net.data.DataCallback;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.net.image.ImageLoader;
import com.prj.sdk.net.image.ImageLoader.ImageCallback;
import com.prj.sdk.util.DateUtil;
import com.prj.sdk.util.NetworkUtil;
import com.prj.sdk.util.StringUtil;
import com.prj.sdk.util.ThumbnailUtil;
import com.prj.sdk.util.Utils;
import com.prj.sdk.widget.CustomToast;
import com.z012.chengdu.sc.R;
import com.z012.chengdu.sc.api.RequestBeanBuilder;
import com.z012.chengdu.sc.app.SessionContext;
import com.z012.chengdu.sc.constants.AppConst;
import com.z012.chengdu.sc.constants.NetURL;
import com.z012.chengdu.sc.net.bean.MainBannerBean;
import com.z012.chengdu.sc.net.bean.NewsBean;
import com.z012.chengdu.sc.net.bean.PushAppBean;
import com.z012.chengdu.sc.net.bean.QAListBean;
import com.z012.chengdu.sc.net.bean.WeatherForHomeBean;
import com.z012.chengdu.sc.net.bean.WeatherFutureInfoBean;
import com.z012.chengdu.sc.tools.WeatherInfoController;
import com.z012.chengdu.sc.ui.activity.HtmlActivity;
import com.z012.chengdu.sc.ui.activity.SearchActivity;
import com.z012.chengdu.sc.ui.activity.WeatherActivity;
import com.z012.chengdu.sc.ui.adapter.BannerImageAdapter;
import com.z012.chengdu.sc.ui.adapter.GridViewAdapter;
import com.z012.chengdu.sc.ui.adapter.QAListAdapter;
import com.z012.chengdu.sc.ui.base.BaseFragment;
import com.z012.chengdu.sc.ui.dialog.CustomDialogUtil;
import com.z012.chengdu.sc.ui.dialog.CustomDialogUtil.onCallBackListener;

/**
 * 首页
 * 
 * @author kborid
 */
public class TabHomeFragment extends BaseFragment implements DataCallback,
		OnPageChangeListener, OnRefreshListener2<ScrollView> {

	private TextView tv_news;
	public GridView mGridView;
	private GridViewAdapter mGridAdapter;
	public PullToRefreshScrollView mPullToRefreshScrollView;
	private boolean isRefresh;
	private String mNewsTargeturl, mNewsTargeturlId;
	private LinearLayout tv_center_title_layout, tv_right_title_layout,
			weather_lay;
	private RelativeLayout limit_lay;
	private ListView listView;
	private QAListAdapter mHotWDAdapter;
	private ArrayList<QAListBean.Result> mBean = new ArrayList<QAListBean.Result>();
	private TextView tv_date, tv_limit1, tv_limit2, tv_temp, tv_pm, tv_weather;
	private RelativeLayout addr_lay;
	private ImageView iv_weather, iv_right_title;
	private WeatherForHomeBean weatherbean = null;
	@SuppressLint("UseSparseArrays")
	private SparseIntArray mTag = new SparseIntArray(); // 全部请求结束标记
	// ----------top banner--------
	private ViewPager viewPager;
	private ArrayList<View> mTopBannerViews = new ArrayList<View>();
	private LinearLayout mIndicatorLayout;
	private int mCurrentItem = 0;
	private BannerImageAdapter mBannerImageAdapter;
	private LinearLayout ll_title_panel;

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
		if (mTopBannerViews.size() > 1)
			AppContext.mMainHandler.postDelayed(mPagerActionRunnable, 3000);
	}

	protected void onInvisible() {
		super.onInvisible();
		AppContext.mMainHandler.removeCallbacks(mPagerActionRunnable);
	}

	@Override
	protected void initViews(View view) {
		super.initViews(view);
		ll_title_panel = (LinearLayout) view.findViewById(R.id.ll_title_panel);
		showProgressDialog(getString(R.string.loading), false);
		iv_right_title = (ImageView) view.findViewById(R.id.iv_right_title);
		tv_news = (TextView) view.findViewById(R.id.tv_news);
		mGridView = (GridView) view.findViewById(R.id.gridview);
		listView = (ListView) view.findViewById(R.id.listView);
		tv_right_title_layout = (LinearLayout) view
				.findViewById(R.id.tv_right_title_layout);
		tv_center_title_layout = (LinearLayout) view
				.findViewById(R.id.tv_center_title_layout);
		mPullToRefreshScrollView = (PullToRefreshScrollView) view
				.findViewById(R.id.scroll_view);

		View v = LayoutInflater.from(getActivity()).inflate(
				R.layout.weather_layout, (ViewGroup) view, false);

		addr_lay = (RelativeLayout) v.findViewById(R.id.addr_lay);
		tv_date = (TextView) v.findViewById(R.id.tv_date);
		tv_limit1 = (TextView) v.findViewById(R.id.tv_limit1);
		tv_limit2 = (TextView) v.findViewById(R.id.tv_limit2);
		tv_temp = (TextView) v.findViewById(R.id.tv_temp);
		tv_pm = (TextView) v.findViewById(R.id.tv_pm);
		iv_weather = (ImageView) v.findViewById(R.id.iv_weather);
		tv_weather = (TextView) v.findViewById(R.id.tv_weather);
		weather_lay = (LinearLayout) v.findViewById(R.id.weather_lay);
		limit_lay = (RelativeLayout) v.findViewById(R.id.limit_lay);
		mIndicatorLayout = (LinearLayout) view
				.findViewById(R.id.point_indicator);
		viewPager = (ViewPager) view.findViewById(R.id.viewPager);
	}

	@Override
	protected void initParams() {
		super.initParams();
		addr_lay.setVisibility(View.INVISIBLE);
		refreshHeadPortrait();
		setNetworkMethod();
		setAppItem();
		setMoAppItem();
		loadWeather();
		loadNews();
		loadQAData();
		loadPushService();
		loadBannerData();
		try {
			// mContentList.clear();
			// String json =
			// SharedPreferenceUtil.getInstance().getString(AppConst.MAIN_IMG_DATA,
			// "[]", false);
			// mContentList.addAll(JSON.parseArray(json, MainBannerBean.class));
			//
			// if (mGalleryAdapter == null && !mContentList.isEmpty()) {
			// mGalleryAdapter = new BannerImageAdapter(getActivity(),
			// mContentList);
			// mTopGallery.setAdapter(mGalleryAdapter);
			// }

			mTopBannerViews.add(weather_lay);
			viewPager.setAdapter(new BannerImageAdapter(mTopBannerViews));
			viewPager.setCurrentItem(0);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * 图片滚动线程
	 */

	Runnable mPagerActionRunnable = new Runnable() {
		@Override
		public void run() {
			int size = mTopBannerViews.size();
			if (size > 1) {
				mCurrentItem++;
				viewPager.setCurrentItem(mCurrentItem);

			}
			AppContext.mMainHandler.postDelayed(mPagerActionRunnable, 3000);
		}
	};

	/**
	 * 加载banner
	 */
	public void loadBannerData() {
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
		tv_news.setOnClickListener(this);
		weather_lay.setOnClickListener(this);
		tv_center_title_layout.setOnClickListener(this);
		mPullToRefreshScrollView.setOnRefreshListener(this);
		tv_right_title_layout.setOnClickListener(this);
		viewPager.setOnPageChangeListener(this);
	}

	/**
	 * 刷新头像
	 * 
	 */
	public void refreshHeadPortrait() {
		try {
			if (iv_right_title == null) {
				return;
			}
			if (SessionContext.isLogin()) {
				String url = SessionContext.mUser.USERBASIC.getHeadphotourl();
				if (url != null && url.length() > 0) {
					if (!url.startsWith("http")) {
						url = NetURL.API_LINK + url;
					}
					
					ImageLoader.getInstance().loadBitmap(new ImageCallback() {
						@Override
						public void imageCallback(Bitmap bm, String url,
								String imageTag) {
							if (bm != null) {
								iv_right_title.setImageBitmap(ThumbnailUtil
										.getRoundImage(bm));
							}
						}

					}, url);
				}
			} else {
				iv_right_title.setImageResource(R.drawable.ic_user);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onClick(View v) {
		// super.onClick(v);
		Intent intent = null;
		switch (v.getId()) {
		case R.id.tv_center_title_layout:
			intent = new Intent(getActivity(), SearchActivity.class);
			startActivity(intent);
			break;
		case R.id.tv_news:
			if (mNewsTargeturl != null) {
				intent = new Intent(getActivity(), HtmlActivity.class);
				intent.putExtra("path", mNewsTargeturl);
				// intent.putExtra("path",
				// "http://192.168.1.99/smpay/payment/service/YiwtPay.topay.do");
				intent.putExtra("title", "今日头条");
				intent.putExtra("id", mNewsTargeturlId);
				startActivity(intent);
			}
			break;
		case R.id.tv_right_title_layout:
			break;
		case R.id.weather_lay:
			if (weatherbean != null) {
				intent = new Intent(getActivity(), WeatherActivity.class);
				intent.putExtra("weatherInfo", weatherbean);
				startActivity(intent);
			}
			break;
		default:
			break;
		}
	}

	/**
	 * 设置推荐app应用
	 */
	public void setAppItem() {
		if (mGridAdapter == null) {
			mGridAdapter = new GridViewAdapter(getActivity(),
					SessionContext.getAppList());
			mGridView.setAdapter(mGridAdapter);
		}

		mGridAdapter.notifyDataSetChanged();
	}

	/**
	 * 设置热门有问必答
	 */
	public void setMoAppItem() {
		if (mHotWDAdapter == null) {
			mHotWDAdapter = new QAListAdapter(getActivity(), mBean);
			listView.setAdapter(mHotWDAdapter);
		}
		// AppListBean clb = new AppListBean();
		// clb.name = "有问必答";
		// clb.descstr = "解决您的一切疑问";
		// SessionContext.addPushColumnItem(clb);
		// mMoreAdapter.notifyDataSetChanged();
	}

	/**
	 * 加载推荐app
	 * 
	 */
	public void loadPushService() {
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
	public void loadNews() {
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
	public void loadWeather() {
		RequestBeanBuilder b = RequestBeanBuilder.create(false);
		b.addBody("cityCode", SessionContext.getAreaInfo(1));
		b.addBody("cityId", getString(R.string.cityId));
		ResponseData d = b.syncRequest(b);
		d.path = NetURL.WEATHER_SERVER;
		d.flag = 4;
		requestID = DataLoader.getInstance().loadData(this, d);
	}

	/**
	 * 加载天气
	 */
	public void loadPM2_5() {
		RequestBeanBuilder builder = RequestBeanBuilder.create(false);
		builder.addBody("getConfForMgr", "YES");

		ResponseData data = builder.syncRequest(builder);
		// data.path = NetURL.PM_2_5;
		data.flag = 5;

		requestID = DataLoader.getInstance().loadData(this, data);
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
		loadWeather();
		loadNews();
		// loadMore();
		loadQAData();
		loadPushService();
		loadBannerData();
		// String date = DateUtil.getCurDateStr("yyyy-MM-dd HH:mm:ss");
		// StringBuilder label = new StringBuilder();
		// label.append("更新于：").append(date);
		// // 显示最后更新的时间
		// refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
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
			if (temp != null && !temp.isEmpty()) {
				mNewsTargeturl = temp.get(0).targeturl;
				tv_news.setText(temp.get(0).text);
				mNewsTargeturlId = temp.get(0).id;
				temp = null;
			}

		} else if (request.flag == 4) {// 气温，日期，天气
			mTag.put(request.flag, request.flag);
			String res = response.body.toString();
			weatherbean = JSON.parseObject(res, WeatherForHomeBean.class);
			JSONObject json = JSON.parseObject(res);
			String future = json.getString("future");
			List<WeatherFutureInfoBean> futureInfo = JSON.parseArray(future,
					WeatherFutureInfoBean.class);
			SessionContext.setWeatherInfo(futureInfo);
			if (weatherbean != null) {
				setWeather(weatherbean);
			}
		} else if (request.flag == 1) {// //热门服务
			mTag.put(request.flag, request.flag);
			JSONObject mJson = JSON.parseObject(response.body.toString());
			String json = mJson.getString("datalist");
			List<PushAppBean> temp = JSON.parseArray(json, PushAppBean.class);
			if (temp.size() >= 8) {
				temp = temp.subList(0, 8);
				PushAppBean bean = new PushAppBean();
				bean.appname = "更多";
				bean.appurls = "ShowAllService";
				temp.add(bean);
			}
			SessionContext.addAppItem(temp);

			setAppItem();

		} else if (request.flag == 2) {// 有问必答
			mTag.put(request.flag, request.flag);
			// JSONObject mJson = JSON.parseObject(response.body.toString());
			// String json = mJson.getString("datalist");
			// List<AppListBean> temp = JSON.parseArray(json,
			// AppListBean.class);
			// SessionContext.getPushColumn().clear();
			// SessionContext.setPushColumn(temp);
			// setMoAppItem();

			JSONObject mJson = JSON.parseObject(response.body.toString());
			String json = mJson.getString("page");
			QAListBean temp = JSON.parseObject(json, QAListBean.class);
			List<QAListBean.Result> data = new ArrayList<QAListBean.Result>();
			if (temp != null && temp.result != null && temp.result.size() > 3) {
				data = temp.result.subList(0, 3);// 最多显示3个
				mBean.clear();
				mBean.addAll(data);
				mHotWDAdapter.notifyDataSetChanged();
			} else {
				mBean.clear();
				mBean.addAll(temp.result);
				mHotWDAdapter.notifyDataSetChanged();
			}
			/*
			 * } else if (request.flag == 5) {// pm2.5 mTag.put(request.flag,
			 * request.flag); JSONObject mJson =
			 * JSON.parseObject(response.body.toString()); String num =
			 * mJson.getString("data"); String txt = mJson.getString("desc");
			 * tv_pm.setText(num + txt);
			 * 
			 * try { int n = Integer.parseInt(num); if (n < 100) {// 优
			 * tv_pm.setBackgroundResource(R.drawable.pm2_5_1bg); } else if (n >
			 * 100 && n < 200) {// 良
			 * tv_pm.setBackgroundResource(R.drawable.pm2_5_2bg); } else {
			 * tv_pm.setBackgroundResource(R.drawable.pm2_5_3bg); } } catch
			 * (Exception e) { }
			 */
		} else if (request.flag == 8) {// banner
			mTag.put(request.flag, request.flag);
			mTopBannerViews.clear();
			mTopBannerViews.add(weather_lay);// 添加天气view
			JSONObject mJson = JSON.parseObject(response.body.toString());
			String json = mJson.getString("datalist");
			List<MainBannerBean> temp = JSON.parseArray(json,
					MainBannerBean.class);
			if (temp != null && !temp.isEmpty()) {
				// 第一条数据不做展示，url为天气url;
				// weather_lay.setTag(R.id.banner_url, temp.get(0).linkurls);
				// weather_lay.setTag(R.id.banner_name, temp.get(0).bnname);
				temp.remove(0);// 移除第0条数据

				for (int i = 0; i < temp.size(); i++) {
					MainBannerBean bean = temp.get(i);
					ImageView view = new ImageView(getActivity());
					view.setLayoutParams(new LayoutParams(
							LayoutParams.MATCH_PARENT,
							LayoutParams.MATCH_PARENT));
					loadImg(i, bean.imgurls, view);
					view.setScaleType(ImageView.ScaleType.FIT_XY);
					view.setTag(R.id.banner_url, bean.linkurls);// 记录跳转url
					view.setTag(R.id.banner_name, bean.bnname);
					view.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							String intentUrl = v.getTag(R.id.banner_url)
									.toString();
							String name = v.getTag(R.id.banner_name).toString();
							if (StringUtil.empty(intentUrl)) {
								return;
							}
							Intent intent = new Intent(getActivity(),
									HtmlActivity.class);
							intent.putExtra("path", intentUrl);
							intent.putExtra("title", name);
							startActivity(intent);
						}
					});
					mTopBannerViews.add(view);
				}

				if (mBannerImageAdapter == null) {
					mBannerImageAdapter = new BannerImageAdapter(
							mTopBannerViews);
					viewPager.setAdapter(mBannerImageAdapter);
					// // 默认在中间附近，使用户看不到边界
					// int mid = Integer.MAX_VALUE / 2;
					// // //初始显示第一个
					// mCurrentItem = mid - mid /
					// mTopBannerViews.size();//数据跳转过大，有anr的bug
					mCurrentItem = mTopBannerViews.size() * 30;// /设置ViewPager的默认项,
																// 设置为总数的30倍，一开始才能往左滑动
					viewPager.setCurrentItem(mCurrentItem);
				} else {
					mBannerImageAdapter.notifyDataSetChanged();
				}

				AppContext.mMainHandler.removeCallbacks(mPagerActionRunnable);
				if (mTopBannerViews.size() > 1) {
					initTopIndicator();
					AppContext.mMainHandler.postDelayed(mPagerActionRunnable,
							5000);
				}
			}
		}

		if (mTag != null && mTag.size() == 5) {// 更新成功标记
			removeProgressDialog();
			mPullToRefreshScrollView.onRefreshComplete();
			ll_title_panel.setVisibility(View.VISIBLE);
			if (isRefresh) {
				isRefresh = false;
				CustomToast.show("更新成功", 0);
			}
			System.gc();
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
			// message = getString(R.string.dialog_tip_null_error);
			if (request.flag == 3 || request.flag == 4 || request.flag == 5
					|| request.flag == 6) {
				isRefresh = false;
				message = response != null && response.data != null ? response.data
						.toString() : getString(R.string.dialog_tip_null_error);
				CustomToast.show("首页部分信息加载失败，请下拉刷新", Toast.LENGTH_LONG);
			}
		}

	}

	/**
	 * 设置天气
	 */
	private void setWeather(WeatherForHomeBean temp) {
		if (StringUtil.notEmpty(temp.limitnumber)) {
			limit_lay.setVisibility(View.VISIBLE);
			String[] tmp = temp.limitnumber.split("\\|");
			tv_limit1.setText(tmp[0]);
			tv_limit2.setText(tmp[1]);
		} else {
			limit_lay.setVisibility(View.GONE);
		}

		tv_temp.setText(temp.temperature2 + "º" + " - " + temp.temperature1
				+ "º");
		String week = DateUtil.dateToWeek(DateUtil.str2Date(
				temp.savedate_weather, "yyyy-MM-dd"));
		tv_date.setText(temp.savedate_weather.replace("-", ".") + " " + week);
		int weatherRes = 0;
		if (DateUtil.getDayOrNight()) {
			weatherRes = WeatherInfoController
					.getWeatherResForNight(temp.status2);
		} else {
			weatherRes = WeatherInfoController
					.getWeatherResForDay(temp.status1);
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

	/**
	 * 加载图片
	 * 
	 * @param position
	 * @param url
	 * @param imageView
	 */
	public void loadImg(int position, String url, final ImageView imageView) {
		// 图片绑定
		String tag;
		if (url != null) {
			if (!url.startsWith("http")) {
				url = NetURL.API_LINK + url;
			}
			tag = url + position;

			Bitmap bm = ImageLoader.getInstance().getCacheBitmap(url);
			if (bm != null) {
				imageView.setImageBitmap(bm);
				imageView.setTag(R.id.image_url, null);
			} else {
				imageView.setBackgroundResource(R.drawable.loading);
				imageView.setTag(R.id.image_url, url);
				ImageLoader.getInstance().loadBitmap(new ImageCallback() {
					@Override
					public void imageCallback(Bitmap bm, String url,
							String imageTag) {
						if (bm != null) {
							imageView.setImageBitmap(bm);
						}
					}

				}, url, tag, 480, 320, -1);
			}
		}
	}

	@Override
	public void onPageScrollStateChanged(int state) {
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {

	}

	/* 更新手动滑动时的位置 */
	@Override
	public void onPageSelected(int arg0) {
		mCurrentItem = arg0;
		int size = mTopBannerViews.size();
		if (size > 1) {
			updateTopGalleryItem(arg0 % size);
		}

	}

	/**
	 * 初始化小圆点Indicator
	 */
	public void initTopIndicator() {
		if (mTopBannerViews.size() == 0) {
			mIndicatorLayout.setVisibility(View.GONE);
			return;
		} else {
			mIndicatorLayout.setVisibility(View.VISIBLE);
		}

		mIndicatorLayout.removeAllViews();
		for (int i = 0; i < mTopBannerViews.size(); i++) {
			ImageView img = new ImageView(getActivity());
			img.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
			img.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT));
			img.setImageResource(R.drawable.img_list);
			img.setPadding(10, 10, 10, 10);
			mIndicatorLayout.addView(img);
		}

		updateTopGalleryItem(0);
	}

	/**
	 * 更新 Indicator
	 * 
	 * @param index
	 */
	public synchronized void updateTopGalleryItem(int index) {
		if (mTopBannerViews.size() == 0) {
			return;
		}
		for (int i = 0; i < mIndicatorLayout.getChildCount(); i++) {
			if (i == index) {
				((ImageView) mIndicatorLayout.getChildAt(i))
						.setImageResource(R.drawable.img_bg);
			} else {
				((ImageView) mIndicatorLayout.getChildAt(i))
						.setImageResource(R.drawable.img_list);
			}
		}
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
						if (viewPager != null && viewPager.getHeight() > 0) {
							// define it for scroll height
							int assignHeight = viewPager.getHeight()
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
