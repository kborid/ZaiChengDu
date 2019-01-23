package com.z012.chengdu.sc.ui.fragment;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.prj.sdk.net.bean.ResponseData;
import com.prj.sdk.net.data.DataCallback;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.net.image.ImageLoader;
import com.prj.sdk.util.DateUtil;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.NetworkUtil;
import com.prj.sdk.util.UIHandler;
import com.prj.sdk.util.Utils;
import com.prj.sdk.widget.CustomToast;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;
import com.scwang.smartrefresh.layout.header.FalsifyHeader;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.z012.chengdu.sc.R;
import com.z012.chengdu.sc.SessionContext;
import com.z012.chengdu.sc.constants.NetURL;
import com.z012.chengdu.sc.helper.ForbidFastClickHelper;
import com.z012.chengdu.sc.helper.WeatherInfoHelper;
import com.z012.chengdu.sc.net.RequestBeanBuilder;
import com.z012.chengdu.sc.net.bean.AllServiceColumnBean;
import com.z012.chengdu.sc.net.bean.AppAllServiceInfoBean;
import com.z012.chengdu.sc.net.bean.HomeBannerInfoBean;
import com.z012.chengdu.sc.net.bean.NewsBean;
import com.z012.chengdu.sc.net.bean.PushAppBean;
import com.z012.chengdu.sc.net.bean.WeatherForHomeBean;
import com.z012.chengdu.sc.net.bean.WeatherFutureInfoBean;
import com.z012.chengdu.sc.ui.activity.HtmlActivity;
import com.z012.chengdu.sc.ui.activity.SearchActivity;
import com.z012.chengdu.sc.ui.adapter.GridViewAdapter;
import com.z012.chengdu.sc.ui.adapter.ServiceHomeAdapter;
import com.z012.chengdu.sc.ui.base.BaseFragment;
import com.z012.chengdu.sc.ui.dialog.CustomDialog;
import com.z012.chengdu.sc.ui.dialog.CustomDialog.onCallBackListener;
import com.z012.chengdu.sc.ui.widge.banner.CommonBannerLayout;
import com.z012.chengdu.sc.ui.widge.maqueue.IUPMarqueeListener;
import com.z012.chengdu.sc.ui.widge.maqueue.UPMarqueeBean;
import com.z012.chengdu.sc.ui.widge.maqueue.UPMarqueeView;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 首页
 * 
 * @author kborid
 */
public class TabHomeFragment extends BaseFragment implements DataCallback {

    private static final int FLAG_BANNER = 0;
    private static final int FLAG_NEWS = 1;
    private static final int FLAG_WEATHER = 2;
    private static final int FLAG_HOT_SERVICE = 3;
    private static final int FLAG_ALL_SERVICE = 4;

    @BindView(R.id.smartRefreshLayout) SmartRefreshLayout smartRefreshLayout;
    @BindView(R.id.scrollView) ScrollView scrollView;
    @BindView(R.id.ll_title_panel) LinearLayout ll_title_panel;
    @BindView(R.id.newsBgView) View newsBgView;
    @BindView(R.id.weather_lay) LinearLayout weather_lay;
    @BindView(R.id.iv_weather) ImageView iv_weather_icon;
    @BindView(R.id.tv_temp) TextView tv_weather_temp;
    @BindView(R.id.tv_air) TextView tv_weather_air;
    @BindView(R.id.banner) CommonBannerLayout banner_lay;
    @BindView(R.id.gridview) GridView mHotServiceGridView;
    @BindView(R.id.marqueeView) UPMarqueeView marqueeView;
    @BindView(R.id.service_lay) LinearLayout service_lay;

    private GridViewAdapter mHotServiceAdapter;
    private List<PushAppBean> mServiceApp = new ArrayList<>();
    private List<AllServiceColumnBean> mCatalogBean	= new ArrayList<>();
    private List<AppAllServiceInfoBean> mAllServiceBean = new ArrayList<>();

    private boolean isRefresh;
    private SparseIntArray mTag = new SparseIntArray(); // 全部请求结束标记

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_tab_home;
    }

    @Override
    protected void onInit() {
    }

    protected void onVisible() {
		super.onVisible();
        banner_lay.startBanner();
        marqueeView.startAnimal(SessionContext.getNewsList().size());
	}

	protected void onInvisible() {
		super.onInvisible();
		banner_lay.stopBanner();
		marqueeView.stopFlipping();
	}

	@Override
	protected void initParams() {
		super.initParams();
        titleShadow();
		setNetworkMethod();
        refreshHotService();
        refreshAllService();
        updateNewsInfo();
        if (NetworkUtil.isNetworkAvailable()) {
            showProgressDialog(getString(R.string.loading), false);
            requestBanner();
            requestWeather();
            requestNews();
            requestHotService();
            requestAllService();
        }

        smartRefreshLayout.setRefreshHeader(new ClassicsHeader(getActivity()));
        smartRefreshLayout.setEnableLoadMore(false);
        smartRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshLayout) {
                isRefresh = true;
                ll_title_panel.setVisibility(View.GONE);
                mTag.clear();
                requestBanner();
                requestWeather();
                requestNews();
                requestHotService();
                requestAllService();
            }
        });

        marqueeView.setUPMarqueeListener(new IUPMarqueeListener() {
            @Override
            public void callback(UPMarqueeBean bean) {
                if (null != bean && !TextUtils.isEmpty(bean.getUrl())) {
                    Intent intent = new Intent(getActivity(), HtmlActivity.class);
                    intent.putExtra("path", bean.getUrl());
                    intent.putExtra("title", "今日重庆");
                    intent.putExtra("id", bean.getId());
                    startActivity(intent);
                }
            }
        });

        RelativeLayout.LayoutParams weatherRlp = (RelativeLayout.LayoutParams) banner_lay.getLayoutParams();
        weatherRlp.width = Utils.mScreenWidth;
        weatherRlp.height = (int) ((float) weatherRlp.width / 375 * 200);
        banner_lay.setLayoutParams(weatherRlp);
        banner_lay.setIndicatorLayoutMarginBottom(Utils.dip2px(30));
        banner_lay.setIndicatorLayoutMarginLeft(Utils.dip2px(20));

        FrameLayout.LayoutParams newLlp = (FrameLayout.LayoutParams) newsBgView.getLayoutParams();
        newLlp.width = Utils.mScreenWidth;
        newLlp.height = (int) ((float) newLlp.width / 375 * 56);
        newsBgView.setLayoutParams(newLlp);
	}

	private void refreshAllService() {
        service_lay.removeAllViews();
        mAllServiceBean.clear();
        mAllServiceBean.addAll(SessionContext.getHomeAllAppList());

        if (null != mAllServiceBean && !mAllServiceBean.isEmpty()) {
            //分别获取一级菜单
            List<AppAllServiceInfoBean> firstGradeList = new ArrayList<>();
            List<AppAllServiceInfoBean> secondGradeList = new ArrayList<>();
            for (AppAllServiceInfoBean bean : mAllServiceBean) {
                if (bean.menutype == 1) {
                    firstGradeList.add(bean);
                } else if (bean.menutype == 2) {
                    secondGradeList.add(bean);
                }
            }

            int firstSize = firstGradeList.size();
            for (int i = 0; i < firstSize; i++) {
                View view = LayoutInflater.from(getActivity()).inflate(R.layout.lv_service_home_item, null);
                LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                if (i == firstSize - 1) {
                    llp.bottomMargin = Utils.dip2px(5);
                } else {
                    llp.bottomMargin = 0;
                }
                service_lay.addView(view, llp);

                //更新内容
                LinearLayout serviceTitleLayout = (LinearLayout) view.findViewById(R.id.serviceTitleLayout);
                final ImageView iv_icon = (ImageView) view.findViewById(R.id.iv_icon);
                final TextView tv_name = (TextView) view.findViewById(R.id.tv_name);
                GridView gridview = (GridView) view.findViewById(R.id.gridview);

                serviceTitleLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (ForbidFastClickHelper.isForbidFastClick()) {
                            return;
                        }
//						EventBus.getDefault().post(tv_name.getText().toString());
//                        if (null != getActivity()) {
//                            ((MainFragmentActivity) getActivity()).changeTabService();
//                        }
                    }
                });

                final AppAllServiceInfoBean bean = mAllServiceBean.get(i);
                String imgUrl = bean.imgurls1;
                if (!TextUtils.isEmpty(imgUrl)) {
                	if (!imgUrl.startsWith("http")) {
                		imgUrl = NetURL.API_LINK + imgUrl;
					}
				}
                ImageLoader.getInstance().loadBitmap(new ImageLoader.ImageCallback() {
                    @Override
                    public void imageCallback(Bitmap bm, String url, String imageTag) {
                        if (null != bm) {
                            iv_icon.setImageBitmap(bm);
                        }
                    }
                }, imgUrl);
                tv_name.setText(bean.name);

                ServiceHomeAdapter adapter = new ServiceHomeAdapter(getActivity(), getColumnApp(bean.id, secondGradeList));
                gridview.setAdapter(adapter);
            }
        }
    }

    private List<AppAllServiceInfoBean> getColumnApp(int id, List<AppAllServiceInfoBean> list) {
        List<AppAllServiceInfoBean> mBean = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            if (id == list.get(i).pid) {
                mBean.add(list.get(i));
            }
        }
        return mBean;
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
		data.flag = FLAG_BANNER;
		requestID = DataLoader.getInstance().loadData(this, data);
	}

    /**
     * 加载今日头条
     *
     */
    private void requestNews() {
        RequestBeanBuilder builder = RequestBeanBuilder.create(false);
        ResponseData data = builder.syncRequest(builder);
        data.path = NetURL.NEWS;
        data.flag = FLAG_NEWS;
        requestID = DataLoader.getInstance().loadData(this, data);
    }

    /**
     * 加载天气
     */
    private void requestWeather() {
        RequestBeanBuilder b = RequestBeanBuilder.create(false);
        b.addBody("cityCode", SessionContext.getAreaInfo(1));
        b.addBody("cityId", getString(R.string.cityId));
        ResponseData d = b.syncRequest(b);
        d.path = NetURL.WEATHER_SERVER;
        d.flag = FLAG_WEATHER;
        requestID = DataLoader.getInstance().loadData(this, d);
    }

    /**
     * 加载首页热门服务
     */
    private void requestHotService() {
        RequestBeanBuilder builder = RequestBeanBuilder.create(false);
        builder.addBody("getConfForMgr", "YES");
        ResponseData data = builder.syncRequest(builder);
        data.path = NetURL.PUSH_SERVICE_;
        data.flag = FLAG_HOT_SERVICE;
        requestID = DataLoader.getInstance().loadData(this, data);
    }

    /**
     * 加载首页全部服务
     */
    private void requestAllService() {
        RequestBeanBuilder builder = RequestBeanBuilder.create(false);
        builder.addBody("getConfForMgr", "YES");
        ResponseData data = builder.syncRequest(builder);
        data.path = NetURL.MORE_COLUMN;
        data.flag = FLAG_ALL_SERVICE;
        requestID = DataLoader.getInstance().loadData(this, data);
    }

    @OnClick(R.id.tv_center_title) void title() {
        startActivity(new Intent(getActivity(), SearchActivity.class));
    }

	private void refreshHotService() {
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
	 * 打开网络设置
	 */
	private void setNetworkMethod() {
		if (!NetworkUtil.isNetworkAvailable()) {// 判断网络是否可用
			CustomDialog mTip = new CustomDialog(getActivity());
			mTip.setBtnText("设置", "取消");
			mTip.show("网络连接不可用,是否进行设置?");
			mTip.setListeners(new onCallBackListener() {
				public void leftBtn(CustomDialog dialog) {
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

				public void rightBtn(CustomDialog dialog) {
					dialog.dismiss();
				}
			});
			mTip = null;// 释放
		}
	}

	@Override
	public void preExecute(ResponseData request) {

	}

	@Override
	public void notifyMessage(ResponseData request, ResponseData response)
			throws Exception {
		if (request.flag == FLAG_NEWS) {// 头条
            mTag.put(FLAG_NEWS, FLAG_NEWS);
			JSONObject mJson = JSON.parseObject(response.body.toString());
			String json = mJson.getString("hotnewstodaylist");
			List<NewsBean> temp = JSON.parseArray(json, NewsBean.class);
			SessionContext.setNewsList(temp);
			updateNewsInfo();
		} else if (request.flag == FLAG_WEATHER) {// 气温，日期，天气
            mTag.put(FLAG_WEATHER, FLAG_WEATHER);
			String res = response.body.toString();
			WeatherForHomeBean weatherbean = JSON.parseObject(res, WeatherForHomeBean.class);
			JSONObject json = JSON.parseObject(res);
			String future = json.getString("future");
			List<WeatherFutureInfoBean> futureInfo = JSON.parseArray(future, WeatherFutureInfoBean.class);
			SessionContext.setWeatherInfo(futureInfo);
			setWeather(weatherbean);
		} else if (request.flag == FLAG_HOT_SERVICE) {// //热门服务
            mTag.put(FLAG_HOT_SERVICE, FLAG_HOT_SERVICE);
			JSONObject mJson = JSON.parseObject(response.body.toString());
			String json = mJson.getString("datalist");
			List<PushAppBean> temp = JSON.parseArray(json, PushAppBean.class);
			SessionContext.setAppList(temp);
            refreshHotService();

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
		} else if (request.flag == FLAG_BANNER) {// banner
            mTag.put(FLAG_BANNER, FLAG_BANNER);
			JSONObject mJson = JSON.parseObject(response.body.toString());
			String json = mJson.getString("datalist");
			List<HomeBannerInfoBean> temp = JSON.parseArray(json, HomeBannerInfoBean.class);
            banner_lay.setImageResource(temp);
		} else if (request.flag == FLAG_ALL_SERVICE) {
		    mTag.put(FLAG_ALL_SERVICE, FLAG_ALL_SERVICE);
            LogUtil.i("dw", response.body.toString());
		    JSONObject mJson = JSON.parseObject(response.body.toString());
            String json = mJson.getString("datalist");
            List<AppAllServiceInfoBean> temp = JSONObject.parseArray(json, AppAllServiceInfoBean.class);
            SessionContext.setHomeAllAppList(temp);
            refreshAllService();
        }

		if (mTag != null && mTag.size() == 5) {// 更新成功标记
			finishRefresh();
			if (isRefresh) {
				isRefresh = false;
				CustomToast.show("更新成功", 0);
			}
		}

	}

	private void finishRefresh() {
	    removeProgressDialog();
	    smartRefreshLayout.finishRefresh();
	    UIHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ll_title_panel.setVisibility(View.VISIBLE);
            }
        }, 1000);
    }

	@Override
	public void notifyError(ResponseData request, ResponseData response,
			Exception e) {
		finishRefresh();
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
			weatherRes = WeatherInfoHelper.getWeatherResForNight(temp.status2);
		} else {
			weatherRes = WeatherInfoHelper.getWeatherResForDay(temp.status1);
		}
		iv_weather_icon.setImageResource(weatherRes);
//		tv_weather.setText(temp.status1);
//		weather_lay.setBackgroundResource(WeatherInfoHelper.getWeatherInfoBg(temp.status1, temp.status2));

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
	@SuppressLint("NewApi")
    private void titleShadow() {
        ll_title_panel.getBackground().mutate().setAlpha(0);
        scrollView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int l, int t, int oldScrollX, int oldScrollY) {
                if (banner_lay != null && banner_lay.getHeight() > 0) {
                    int distance = banner_lay.getHeight() - ll_title_panel.getHeight();
                    if (t < distance) {
                        if (t < 0) t = 0;
                        int progress = (int) ((float) t / distance * 255);
                        ll_title_panel.getBackground().mutate().setAlpha(progress);
                        ll_title_panel.setClickable(false);
                    } else {
                        ll_title_panel.getBackground().mutate().setAlpha(255);
                        ll_title_panel.setClickable(true);
                    }
                }
            }
        });
	}
}
