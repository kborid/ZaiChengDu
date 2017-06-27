package com.z012.chengdu.sc.ui.activity;

import java.lang.reflect.Field;
import java.net.ConnectException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;
import cn.jpush.android.api.JPushInterface;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dc.statistic.StatisticProxy;
import com.dc.statistic.bean.InitParameter;
import com.prj.sdk.app.AppContext;
import com.prj.sdk.net.bean.ResponseData;
import com.prj.sdk.net.data.DataCallback;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.net.image.ImageLoader;
import com.prj.sdk.net.image.ImageLoader.ImageCallback;
import com.prj.sdk.util.ActivityTack;
import com.prj.sdk.util.DateUtil;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.SharedPreferenceUtil;
import com.prj.sdk.util.StringUtil;
import com.prj.sdk.util.Utils;
import com.prj.sdk.widget.CustomToast;
import com.umeng.analytics.MobclickAgent;
import com.z012.chengdu.sc.R;
import com.z012.chengdu.sc.api.RequestBeanBuilder;
import com.z012.chengdu.sc.app.SessionContext;
import com.z012.chengdu.sc.constants.AppConst;
import com.z012.chengdu.sc.constants.NetURL;
import com.z012.chengdu.sc.net.bean.AdvertisementBean;
import com.z012.chengdu.sc.net.bean.AppInfoBean;
import com.z012.chengdu.sc.net.bean.AppListBean;
import com.z012.chengdu.sc.net.bean.AppOtherInfoBean;
import com.z012.chengdu.sc.net.bean.PushAppBean;
import com.z012.chengdu.sc.ui.base.BaseActivity;

/**
 * 欢迎页面
 * 
 * @author Liao
 * 
 */
public class WelcomeActivity extends BaseActivity implements DataCallback {
	/**
	 * 控制变量
	 */
	private long start = 0; // 记录启动时间
	private final long LOADING_TIME = 1500;
	private final long AD_TIME = 3000; // 等待广告加载时间
	private Map<Integer, Object> mTag = new HashMap<Integer, Object>(); // 请求结束标记，目的是判断是否显示广告
	private ImageView iv_advertisement;
	private FrameLayout layoutAd;
	private Button btn_skip;
	private AdvertisementBean mAdvertBean;
	private boolean isBreak; // 点击广告，终止本页面跳转流程

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ui_welcome);
		start = SystemClock.elapsedRealtime();
		initViews();
		initParams();
		initListeners();
	}

	@Override
	protected void onResume() {
		super.onResume();
		JPushInterface.onResume(this);// 用于“用户使用时长”，“活跃用户”，“用户打开次数”的统计，并上报到服务器，在
										// Portal 上展示给开发者
	}

	@Override
	protected void onPause() {
		super.onPause();
		JPushInterface.onPause(this);// 用于“用户使用时长”，“活跃用户”，“用户打开次数”的统计，并上报到服务器，在
										// Portal 上展示给开发者
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void initViews() {
		super.initViews();
		iv_advertisement = (ImageView) findViewById(R.id.iv_advertisement);
		layoutAd = (FrameLayout) findViewById(R.id.layoutAd);
		btn_skip = (Button) findViewById(R.id.btn_skip);
		layoutAd.setVisibility(View.GONE);
	}

	public void initParams() {
		super.initParams();
		SessionContext.initUserInfo();
		SessionContext.setAreaCode(getString(R.string.areaCode),
				getString(R.string.areaName));
		Utils.initScreenSize(this);// 设置手机屏幕大小
		loadCacheData();
		loadAppList();
		loadAppInfo();
		loadAppAdvertisement();
		loadAllProcotol();
	}

	@Override
	public void initListeners() {
		super.initListeners();
		btn_skip.setOnClickListener(this);
		iv_advertisement.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		// super.onClick(v);
		switch (v.getId()) {
		case R.id.btn_skip:
			intentActivity();
			break;
		case R.id.iv_advertisement:
			isBreak = true;
			// Utils.startWebView(WelcomeActivity.this,
			// mAdvertBean.linkaddress);
			Intent mIntent = new Intent(this, HtmlActivity.class);
			mIntent.putExtra("id", mAdvertBean.Id);
			mIntent.putExtra("title", mAdvertBean.adName);
			mIntent.putExtra("path", mAdvertBean.linkaddress);// temp.entry
			mIntent.putExtra("goBack", "Main");// Html返回处理
			startActivity(mIntent);
			this.finish();
			// 添加友盟自定义事件
			MobclickAgent.onEvent(this, "OpeningAdTouched");
			break;

		default:
			break;
		}
	}

	/**
	 * 预加载首页缓存数据
	 */
	public void loadCacheData() {
		try {

			byte[] pushService = DataLoader.getInstance().getCacheData(
					NetURL.PUSH_SERVICE_);
			if (pushService != null) {
				String json = new String(pushService, "UTF-8");
				ResponseData response = JSON.parseObject(json,
						ResponseData.class);
				if (response != null && response.body != null) {
					String mJson = JSON.parseObject(response.body.toString())
							.getString("datalist");
					List<PushAppBean> temp = JSON.parseArray(mJson,
							PushAppBean.class);
					if (temp.size() >= 8) {
						temp = temp.subList(0, 8);
						PushAppBean bean = new PushAppBean();
						bean.appname = "更多";
						bean.appurls = "ShowAllService";
						temp.add(bean);
					}
					SessionContext.addAppItem(temp);
				}
			}

			byte[] pushColunn = DataLoader.getInstance().getCacheData(
					NetURL.PUSH_MORE_SERVICE);
			if (pushColunn != null) {
				String json = new String(pushColunn, "UTF-8");
				ResponseData response = JSON.parseObject(json,
						ResponseData.class);
				if (response != null && response.body != null) {
					String mJson = JSON.parseObject(response.body.toString())
							.getString("datalist");
					List<AppListBean> temp = JSON.parseArray(mJson,
							AppListBean.class);
					SessionContext.getPushColumn().clear();
					SessionContext.setPushColumn(temp);
				}
			}

			byte[] allApp = DataLoader.getInstance().getCacheData(
					NetURL.ALL_APP);
			if (allApp != null) {
				String json = new String(allApp, "UTF-8");
				ResponseData response = JSON.parseObject(json,
						ResponseData.class);
				if (response != null && response.body != null) {
					String mJson = JSON.parseObject(response.body.toString())
							.getString("datalist");
					List<AppListBean> temp = JSON.parseArray(mJson,
							AppListBean.class);
					SessionContext.getAllAppList().clear();
					SessionContext.setAllAppItem(temp);
				}
			}
			// 获取广告
			String ad = SharedPreferenceUtil.getInstance().getString(
					AppConst.ADVERTISEMENT_INFO, "", false);
			if (StringUtil.notEmpty(ad)) {
				mAdvertBean = JSON.parseObject(ad, AdvertisementBean.class);
				loadImage(iv_advertisement, mAdvertBean.picture);
			}
			// int version =
			// SharedPreferenceUtil.getInstance().getInt(AppConst.CURRENT_VERSION,
			// 0);// 获取版本号
			// PackageManager manager = this.getPackageManager();
			// PackageInfo info = manager.getPackageInfo(this.getPackageName(),
			// 0);
			// int currentVersion = info.versionCode;
			// if (version < currentVersion) {
			// SharedPreferenceUtil.getInstance().setInt(AppConst.CURRENT_VERSION,
			// currentVersion);// 缓存版本号
			// DataLoader.getInstance().removeCacheData(NetURL.PUSH_COLUMN);//
			// 删除首页推荐栏目及应用数据
			// DataLoader.getInstance().removeCacheData(NetURL.WG_ALL);//
			// 删除围观缓存数据
			// }

		} catch (Exception e) {
		}
	}

	/**
	 * 加载更多栏目及所有应用信息
	 */
	public void loadAppList() {
		RequestBeanBuilder builder = RequestBeanBuilder.create(false);
		builder.addBody("getConfForMgr", "YES");

		ResponseData data = builder.syncRequest(builder);
		data.path = NetURL.ALL_APP;
		data.flag = 1;

		requestID = DataLoader.getInstance().loadData(this, data);
	}

	/**
	 * 获取app信息（分享、升级）
	 */
	public void loadAppInfo() {
		RequestBeanBuilder builder = RequestBeanBuilder.create(false);
		builder.addBody("getConfForMgr", "YES");
		builder.addBody("platform", "0");// 配置类型，0为安卓端，1为苹果端
		builder.addBody("cityCode", SessionContext.getAreaInfo(1));// 城市编码

		ResponseData data = builder.syncRequest(builder);
		data.path = NetURL.APP_INFO;
		data.flag = 2;

		requestID = DataLoader.getInstance().loadData(this, data);
	}

	/**
	 * 加载广告信息
	 */
	public void loadAppAdvertisement() {
		RequestBeanBuilder builder = RequestBeanBuilder.create(false);
		builder.addBody("getConfForMgr", "YES");
		builder.addBody("adStatus", "1");// 1上架0下架
		builder.addBody("startingTime", DateUtil.getCurDateStr("yyyy-MM-dd"));// 开始时间
		builder.addBody("endTime", DateUtil.getCurDateStr("yyyy-MM-dd"));// 结束时间

		ResponseData data = builder.syncRequest(builder);
		data.path = NetURL.ADVERTISEMENT;
		data.flag = 3;

		requestID = DataLoader.getInstance().loadData(this, data);
	}

	private void loadAllProcotol() {
		RequestBeanBuilder b = RequestBeanBuilder.create(false);
		b.addBody("areaid", SessionContext.getAreaInfo(1));

		ResponseData d = b.syncRequest(b);
		d.path = NetURL.APPSTORE;
		d.flag = 4;

		requestID = DataLoader.getInstance().loadData(this, d);
	}

	// @Override
	// public void onWindowFocusChanged(boolean hasFocus) {
	// super.onWindowFocusChanged(hasFocus);
	// }

	/**
	 * 跳转到相应Activity
	 */
	public void intentActivity() {
		if (isBreak) {
			return;
		}
		Intent intent = null;
		// 上次的版本code
		int last_version = SharedPreferenceUtil.getInstance().getInt(
				AppConst.LAST_USE_VERSIONCODE, 0);
		// 当前版本code
		int current = 0;
		try {
			current = this.getPackageManager().getPackageInfo(getPackageName(),
					0).versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		if (last_version == 0 || last_version < current) {
			intent = new Intent(WelcomeActivity.this, UserGuideActivity.class);
		} else {
			intent = new Intent(WelcomeActivity.this,
					MainFragmentActivity.class);
			String value = "";
			if (getIntent().getExtras() != null
					&& getIntent().getExtras().getString("path") != null) {
				value = getIntent().getExtras().getString("path");
				intent.putExtra("path", value);
			}
		}
		startActivity(intent);
		finish();
	}

	/**
	 * 是否显示广告
	 */
	private boolean isShowAdvert() {
		// 数据初始化完成 并且有广告图片，显示广告
		if (mTag != null && mTag.get(1) != null && mTag.get(2) != null
				&& iv_advertisement.getTag() != null) {
			return true;
		}
		return false;
	}

	/**
	 * 显示广告
	 */
	public void showAd() {
		long end = SystemClock.elapsedRealtime();

		if (end - start > LOADING_TIME) {
			if (isShowAdvert()) {// 显示隐藏广告界面
				layoutAd.setVisibility(View.VISIBLE);
			}
		} else {
			AppContext.mMainHandler.postDelayed(new Runnable() {// 延迟加载，显示LOADING_TIME时长的加载页

						@Override
						public void run() {
							if (isShowAdvert()) {// 显示隐藏广告界面
								layoutAd.setVisibility(View.VISIBLE);
							}
						}

					}, LOADING_TIME - (end - start));
		}
	}

	/**
	 * 跳转到下一个页面
	 */
	private void goToNextActivity() {
		if (mTag == null || mTag.size() != 2) {// 如果初始化数据没有加载完，就一直停留加载页
			return;
		}
		mTag.put(4, 4);

		long end = SystemClock.elapsedRealtime();

		if (end - start < LOADING_TIME) {
			// 延迟加载
			AppContext.mMainHandler.postDelayed(new Runnable() {

				@Override
				public void run() {

					AppContext.mMainHandler.postDelayed(new Runnable() {// 延迟加载3s，主要是做显示广告

								@Override
								public void run() {
									intentActivity();
								}
							}, AD_TIME);

				}

			}, LOADING_TIME - (end - start));
		} else {
			intentActivity();
		}
	}

	@Override
	public void preExecute(ResponseData request) {

	}

	@Override
	public void notifyMessage(ResponseData request, ResponseData response)
			throws Exception {
		if (request.flag == 1) {
			mTag.put(request.flag, request.flag);// 记录请求成功状态
			SessionContext.getAllAppList().clear();
			JSONObject mJson = JSON.parseObject(response.body.toString());
			String json = mJson.getString("datalist");
			List<AppListBean> temp = JSON.parseArray(json, AppListBean.class);
			SessionContext.setAllAppItem(temp);
		} else if (request.flag == 2) {
			AppInfoBean mAppInfo = JSON.parseObject(response.body.toString(),
					AppInfoBean.class);
			if (mAppInfo.isforce == 1
					&& AppContext.compareVersion(mAppInfo.vsid,
							AppContext.getVersion()) > 0) {// 是否强制升级 0 是 1 否 并且
															// 服务器版本大于当前版本
				showUpdateDialog(mAppInfo.apkurls, mAppInfo.updesc);
			} else {
				mTag.put(request.flag, request.flag);// 记录请求成功状态
			}
			// 缓存APP信息
			SharedPreferenceUtil.getInstance().setString(AppConst.APP_INFO,
					response.body.toString(), false);

		} else if (request.flag == 3) {
			mAdvertBean = JSON.parseObject(response.body.toString(),
					AdvertisementBean.class);
			loadImage(iv_advertisement, mAdvertBean.picture);
			SharedPreferenceUtil.getInstance().setString(
					AppConst.ADVERTISEMENT_INFO, response.body.toString(),
					false);
		} else if (request.flag == 4) {
			AppOtherInfoBean bean = JSON.parseObject(response.body.toString(),
					AppOtherInfoBean.class);
			SharedPreferenceUtil.getInstance().setString(AppConst.ABOUT_ICON,
					bean.AboutImage, true);
			SharedPreferenceUtil.getInstance().setString(AppConst.ABOUT_US,
					bean.AboutInfoURL, true);
			SharedPreferenceUtil.getInstance().setString(
					AppConst.REGISTER_AGEMENT, bean.RegisterProtocolURL, true);
			SharedPreferenceUtil.getInstance().setString(AppConst.PROBLEM,
					bean.FaqURL, true);
			SharedPreferenceUtil.getInstance().setString(
					AppConst.IDENTITY_PROTOCOL, bean.IdentityProtocolURL, true);
		}
		goToNextActivity();
	}

	@Override
	public void notifyError(ResponseData request, ResponseData response,
			Exception e) {
		removeProgressDialog();
		String message;
		if (e != null && e instanceof ConnectException) {
			message = getString(R.string.dialog_tip_net_error);
			CustomToast.show(message, Toast.LENGTH_LONG);
		}
		// else {
		// message = response != null && response.data != null ?
		// response.data.toString() : getString(R.string.dialog_tip_null_error);
		// }

		if (request.flag == 1) {
			if (SessionContext.getAllAppList().isEmpty()) {// 如果请求失败比且没有缓存数据就重连，必须有数据才能进入
				loadAppList();
				return;
			} else {
				mTag.put(request.flag, request.flag);
				goToNextActivity();
				return;
			}
		}
		if (request.flag == 2) {// app信息获取失败就重连
			loadAppInfo();
			return;
		}
		goToNextActivity();

	}

	/**
	 * 版本更新对话框
	 * 
	 * @param url
	 * @param description
	 */
	private void showUpdateDialog(final String url, final String description) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setInverseBackgroundForced(true);
		builder.setMessage(description);
		builder.setTitle("版本更新");
		builder.setCancelable(false);
		builder.setPositiveButton("下载", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				Utils.startWebView(WelcomeActivity.this, url);
				try {// 控制弹框的关闭

					Field field = dialog.getClass().getSuperclass()
							.getDeclaredField("mShowing");
					field.setAccessible(true);
					field.set(dialog, false);// true表示要关闭
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		builder.setNegativeButton("关闭", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				ActivityTack.getInstanse().exit();// 退出应用
			}
		});
		builder.create().show();
	}

	/***
	 * 加载广告图片
	 */
	public void loadImage(final ImageView iView, String url) {
		if (!url.startsWith("http")) {
			url = NetURL.API_LINK + url;// 拼接广告图片路径
		}
		Bitmap bm = ImageLoader.getInstance().getCacheBitmap(url);
		if (bm != null) {
			iv_advertisement.setTag("Y");// 设置标记
			iView.setImageBitmap(bm);
			showAd();
		} else {
			ImageLoader.getInstance().loadBitmap(new ImageCallback() {
				@Override
				public void imageCallback(Bitmap bm, String url, String imageTag) {
					if (bm != null) {
						iv_advertisement.setImageBitmap(bm);
						iv_advertisement.setTag("Y");
						showAd();
					}
				}

			}, url);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN
				&& event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			DataLoader.getInstance().clear(requestID);
			ActivityTack.getInstanse().exit();
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}
}
