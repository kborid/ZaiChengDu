package com.z012.chengdu.sc.ui.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import cmb.pb.util.CMBKeyboardFunc;

import com.baidu.location.BDLocation;
import com.common.widget.custom.CommonLoadingWidget;
import com.prj.sdk.app.AppContext;
import com.prj.sdk.net.bean.ResponseData;
import com.prj.sdk.net.data.DataCallback;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.util.ActivityTack;
import com.prj.sdk.util.DateUtil;
import com.prj.sdk.util.SharedPreferenceUtil;
import com.prj.sdk.util.Utils;
import com.prj.sdk.widget.CustomToast;
import com.prj.sdk.widget.webview.ChooserFileController;
import com.prj.sdk.widget.webview.WebChromeClientCompat;
import com.z012.chengdu.sc.R;
import com.z012.chengdu.sc.action.LocationManagerBD;
import com.z012.chengdu.sc.action.LocationManagerBD.LocationCallback;
import com.z012.chengdu.sc.api.RequestBeanBuilder;
import com.z012.chengdu.sc.app.SessionContext;
import com.z012.chengdu.sc.broatcast.UnLoginBroadcastReceiver;
import com.z012.chengdu.sc.constants.AppConst;
import com.z012.chengdu.sc.constants.NetURL;
import com.z012.chengdu.sc.ui.JSBridge.RegisterHandler;
import com.z012.chengdu.sc.ui.JSBridge.WVJBWebViewClient;
import com.z012.chengdu.sc.ui.activity.LoginActivity.onCancelLoginListener;
import com.z012.chengdu.sc.ui.base.BaseActivity;

/**
 * 中间件处理，加载html5应用数据
 * 
 * @author LiaoBo
 * @date 2014-7-8
 */
@SuppressLint("SetJavaScriptEnabled")
public class HtmlActivity extends BaseActivity implements
		onCancelLoginListener, DataCallback {
	public final static String CSS_STYLE = "<style>* {font-size:40px;padding:10px;}</style>";
	private WebView mWebView;
	private CommonLoadingWidget common_loading_widget;
	private String URL, mTitle, loginUrl;
	private LinearLayout layout_test;
	private EditText et_url;
	private Button btn_go;
	private TextView tv_cur;
	private ActivityResult mActivityForResult;
	private String mID;
	private LinearLayout tv_left_title_layout, tv_left_title_close;
	private ChooserFileController mCtrl;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ui_web_act);
		createController();
		initViews();
		dealIntent();
		initParams();
		initListeners();
		// important , so that you can use js to call Uemng APIs
		// new MobclickAgentJSInterface(this, mWebView, new
		// MyWebChromeClient());
		if (AppConst.ISDEVELOP) {
			if (getIntent().getBooleanExtra("ISDEVELOP", false)) {
				initDevelop();
			}
		}
	}

	private void createController() {
		mCtrl = new ChooserFileController(this);
	}

	/**
	 * 开发者测试
	 */
	public void initDevelop() {
		layout_test = (LinearLayout) findViewById(R.id.layout_test);
		et_url = (EditText) findViewById(R.id.et_url);
		btn_go = (Button) findViewById(R.id.btn_go);
		tv_cur = (TextView) findViewById(R.id.tv_cur);
		StringBuilder sb = new StringBuilder();
		sb.append("\nCurrent Environment（")
				.append(SharedPreferenceUtil.getInstance().getInt(
						AppConst.APPTYPE, 0)).append("：")
				.append(NetURL.getApi()).append("）");
		tv_cur.setText(sb);
		tv_cur.setVisibility(View.VISIBLE);
		layout_test.setVisibility(View.VISIBLE);
		btn_go.setOnClickListener(new android.view.View.OnClickListener() {

			@Override
			public void onClick(View v) {
				URL = et_url.getText().toString().trim();
				if ("0".equals(URL) || "1".equals(URL) || "2".equals(URL)) {
					SharedPreferenceUtil.getInstance().setInt(AppConst.APPTYPE,
							Integer.parseInt(URL));// 保存切换地址类型

					AppContext.mMainHandler.postDelayed(new Runnable() {
						@Override
						public void run() {
							SessionContext.cleanUserInfo();
							SessionContext.destroy();
							SharedPreferenceUtil.getInstance().setString(
									AppConst.MAIN_IMG_DATA, "", false);
							ActivityTack.getInstanse().exit();
						}
					}, 2000);
					CustomToast.show("切换成功，即将退出，请手动重启", 0);
					return;
				}

				mWebView.loadUrl(URL);
			}
		});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tv_left_title_layout:
			if (mWebView.canGoBack()) {
				mWebView.goBack();
			} else {
				goBack();
			}
			break;
		case R.id.tv_left_title_close:
			goBack();
			break;

		default:
			break;
		}
	}

	public void initViews() {
		super.initViews();
		mWebView = (WebView) findViewById(R.id.webview);
		tv_left_title_layout = (LinearLayout) findViewById(R.id.tv_left_title_layout);
		common_loading_widget = (CommonLoadingWidget) findViewById(R.id.common_loading_widget);
		tv_left_title_close = (LinearLayout) findViewById(R.id.tv_left_title_close);
	}

	@Override
	public void dealIntent() {
		super.dealIntent();
		if (getIntent().getExtras() != null
				&& getIntent().getExtras().getString("path") != null) {
			URL = getIntent().getExtras().getString("path");
		}
		if (getIntent().getExtras() != null
				&& getIntent().getExtras().getString("title") != null) {
			mTitle = getIntent().getExtras().getString("title");
		}
		if (getIntent().getExtras() != null
				&& getIntent().getExtras().getString("id") != null) {
			mID = getIntent().getExtras().getString("id");
		}

	}

	public void initParams() {
		super.initParams();
		tv_center_title.setText(mTitle);
		LoginActivity.setCancelLogin(this);
		WebSettings webSetting = mWebView.getSettings();
		webSetting.setJavaScriptEnabled(true);
		webSetting.setJavaScriptCanOpenWindowsAutomatically(true);
		webSetting.setCacheMode(WebSettings.LOAD_DEFAULT);
		// webSetting.setCacheMode(WebSettings.LOAD_CACHE_ONLY);
		webSetting.setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);// 自动缩放
		webSetting.setUseWideViewPort(true);// 将图片调整到适合webview的大小
		webSetting.setLoadWithOverviewMode(true);// 充满全屏。
		mWebView.setHorizontalScrollBarEnabled(false);// 水平不显示
		mWebView.setVerticalScrollBarEnabled(false); // 垂直不显示
		webSetting.setAllowFileAccess(true);// 允许访问文件数据
		webSetting.setDomStorageEnabled(true);// 开启Dom存储Api(启用地图、定位之类的都需要)
		webSetting.setRenderPriority(WebSettings.RenderPriority.HIGH);// 提高渲染的优先级
		// 应用可以有缓存
		webSetting.setAppCacheEnabled(true);// 开启 Application Caches 功能
		mWebView.getSettings().setAppCachePath(Utils.getFolderDir("webCache"));
		// 应用可以有数据库
		webSetting.setDatabaseEnabled(true);// 启用数据库
		webSetting.setDatabasePath(Utils.getFolderDir("webDatabase"));
		webSetting.setGeolocationEnabled(true); // 启用地理定位
		webSetting.setDefaultTextEncodingName("utf-8");
		mWebView.setDownloadListener(new MyWebViewDownLoadListener());// 开启文件下载功能
		try {
			StringBuilder sb = new StringBuilder();
			String pkName = this.getPackageName();
			String versionName = this.getPackageManager().getPackageInfo(
					pkName, 0).versionName;
			sb.append(webSetting.getUserAgentString()).append(" Android/")
					.append(pkName).append("/").append(versionName);// 名字+包名+版本号
			webSetting.setUserAgentString(sb.toString());// 追加修改ua特征标识（名字+包名+版本号）使得web端正确判断
		} catch (Exception e) {
		}
		if (Build.VERSION.SDK_INT >= 19) { // 控制图片加载处理，提高view加载速度
			webSetting.setLoadsImagesAutomatically(true);
		} else {
			webSetting.setLoadsImagesAutomatically(false);
		}
		// URL = "file:///android_asset/index.html";
		mWebView.loadUrl(URL);
		uploadData();

		// 增加接口方法,让html页面调用
		// addJSInterfaces();
	}

	public void initListeners() {
		super.initListeners();
		mWebView.setWebViewClient(new MyWebViewClient(mWebView));
		mWebView.setWebChromeClient(new WebChromeClientCompat(this, mCtrl,
				tv_center_title));
		tv_left_title_layout.setOnClickListener(this);
		tv_left_title_close.setOnClickListener(this);
	}

	/**
	 * 处理返回，如果是广告，就跳首页
	 */
	public void goBack() {
		if (getIntent().getExtras() != null
				&& getIntent().getExtras().getString("goBack") != null) {
			Intent intent = new Intent(this, MainFragmentActivity.class);
			startActivity(intent);
		} else {
			this.finish();
		}
	}

	/**
	 * 数据埋点
	 * 
	 */
	public void uploadData() {
		try {
			RequestBeanBuilder builder = RequestBeanBuilder.create(false);
			if (SessionContext.isLogin()) {
				builder.addBody("userid", SessionContext.mUser.USERBASIC.id);
			} else {
				builder.addBody("userid", "unlogin");
			}
			builder.addBody("nodeid", mID);
			builder.addBody("accurls", URL);
			builder.addBody("acctime",
					DateUtil.getCurDateStr("yyyy-MM-dd HH:mm:ss"));
			builder.addBody("channel", "2");// 1：ios2：android3微信4支护宝；5web
			builder.addBody("ipaddresses", AppContext.getLocalIpAddress());
			ResponseData data = builder.syncRequest(builder);
			data.path = NetURL.NODE;
			requestID = DataLoader.getInstance().loadData(this, data);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	class MyWebViewClient extends WVJBWebViewClient {

		public MyWebViewClient(WebView webView) {
			// support js send
			// super(webView, new WVJBWebViewClient.WVJBHandler() {
			// @Override
			// public void request(Object data, WVJBResponseCallback callback) {
			// Toast.makeText(HtmlActivity.this,
			// "Java Received message from JS:" + data,
			// Toast.LENGTH_LONG).show();
			// callback.callback("Response for message from Java");
			// }
			// });
			super(webView);
			new RegisterHandler(this, HtmlActivity.this).init();
		}

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			// TODO Auto-generated method stub
			// 使用当前的WebView加载页面
			CMBKeyboardFunc kbFunc = new CMBKeyboardFunc(HtmlActivity.this);
			if (kbFunc.HandleUrlCall(view, url) == false) {
				return super.shouldOverrideUrlLoading(view, url);
			} else {
				return true;
			}
			// return super.shouldOverrideUrlLoading(view, url);
		}

		@Override
		public void onPageStarted(final WebView view, String url, Bitmap favicon) {
			super.onPageStarted(view, url, favicon);
			common_loading_widget.startLoading();
			try {
				// 拦截H5应用跳转网页版登录，使用app登录页面登录，登录成功加载loginUrl的链接
				if (url != null
						&& (url.contains("rtnCode=900902") || url
								.contains("rtnCode=310001"))) {
					if (loginUrl != null) {
						goBack();
					} else {
						loginUrl = Uri.parse(url).getQueryParameter("loginUrl");
						sendBroadcast(new Intent(
								UnLoginBroadcastReceiver.ACTION_NAME));
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			mWebView.setEnabled(false);
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);
			// 拦截巴士公交进行原生定位,向js注入定位结果
			if (url != null && url.startsWith("http://m.basbus.cn")) {
				if (mLatitude != 0 && mLongitude != 0) {
					final StringBuilder script = new StringBuilder();
					script.append("getic(").append(mLongitude).append(",")
							.append(mLatitude).append(")");
					executeJavascript(script.toString());
				} else if (!LocationManagerBD.getIns().isStart()) {
					LocationManagerBD.getIns().startBaiduLocation(
							HtmlActivity.this.getApplicationContext(),
							new LocationCallback() {
								@Override
								public void onLocationInfo(
										BDLocation locationInfo) {
									if (locationInfo == null) {
										return;
									}
									try {
										mLatitude = locationInfo.getLatitude();
										mLongitude = locationInfo
												.getLongitude();
										LocationManagerBD.getIns()
												.stopBaiduLocation();
										StringBuilder script = new StringBuilder();
										script.append("getic(")
												.append(mLongitude).append(",")
												.append(mLatitude).append(")");
										executeJavascript(script.toString());
									} catch (Exception e) {
										e.printStackTrace();
									}
								}
							});
				}
			}

			String title = view.getTitle();
			if (title != null
					&& !title
							.startsWith(NetURL.getApi().replace("http://", ""))
					&& !title.equals(url.replace("http://", ""))) {
				mTitle = title;
				tv_center_title.setText(mTitle);// 点击后退，设置标题
			}
			// tv_center_title.setText(view.getTitle());// 点击后退，设置标题
			if (view.canGoBack()) {
				tv_left_title_close.setVisibility(View.VISIBLE);
			} else {
				tv_left_title_close.setVisibility(View.GONE);
			}

			if (url.contains("weather/service/Weather.getWeatherInfo.do")) {
				FrameLayout.LayoutParams rl1 = (FrameLayout.LayoutParams) mWebView
						.getLayoutParams();
				rl1.topMargin = Utils.dip2px(-15);
				mWebView.setLayoutParams(rl1);
			}

			common_loading_widget.closeLoading();
			if (!view.getSettings().getLoadsImagesAutomatically()) {
				view.getSettings().setLoadsImagesAutomatically(true);
			}
			mWebView.setEnabled(true);
		}

		public void onReceivedError(WebView webview, int errorCode,
				String description, String failingUrl) {
			// super.onReceivedError(webview, errorCode, description,
			// failingUrl);
			try {
				webview.stopLoading();
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (mWebView.canGoBack()) {
				mWebView.goBack();
			}
			if (errorCode == WebViewClient.ERROR_CONNECT
					|| errorCode == WebViewClient.ERROR_TIMEOUT
					|| errorCode == WebViewClient.ERROR_HOST_LOOKUP) {
				mWebView.loadDataWithBaseURL(null, CSS_STYLE
						+ "哎呀，出错了,请检查网络并关闭重试！", "text/html", "utf-8", null);// 显示空白，屏蔽显示出错的网络地址url
			}

		}

		// 当load有ssl层的https页面时，如果这个网站的安全证书在Android无法得到认证，WebView就会变成一个空白页，而并不会像PC浏览器中那样跳出一个风险提示框
		@Override
		public void onReceivedSslError(WebView view, SslErrorHandler handler,
				SslError error) {
			// 忽略证书的错误继续Load页面内容
			CustomToast.show("已忽略证书信息继续加载", 0);
			handler.proceed();// 忽略证书信息继续加载
			// handler.cancel(); // Android默认的处理方式
			// handleMessage(Message msg); // 进行其他处理
			// super.onReceivedSslError(view, handler, error);
		}

	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mWebView.saveState(outState);
	}

	/**
	 * webview 下载文件
	 * 
	 * @author LiaoBo
	 */
	class MyWebViewDownLoadListener implements DownloadListener {

		@Override
		public void onDownloadStart(String url, String userAgent,
				String contentDisposition, String mimetype, long contentLength) {
			Utils.startWebView(HtmlActivity.this, url);
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		mWebView.stopLoading();
	}

	public void onDestroy() {
		super.onDestroy();
		destroyView();
		LoginActivity.setCancelLogin(null);
		common_loading_widget.closeLoading();
	}

	/**
	 * 销毁视图
	 */
	public void destroyView() {
		mWebView.stopLoading();
		mWebView.removeAllViews();
		mWebView.clearAnimation();
		mWebView.clearFormData();
		mWebView.clearHistory();
		mWebView.clearMatches();
		mWebView.clearSslPreferences();
		// mWebView.clearCache(true);
		mWebView.destroy();
		AppContext.mMemoryMap.clear();// 清空提供给网页的缓存
		mCtrl.onDestroy();
	}

	/**
	 * 增加JS调用接口 让html页面调用
	 */
	// @SuppressLint("JavascriptInterface")
	// public void addJSInterfaces() {
	// mWebView.addJavascriptInterface(new HostJsDeal(mWebView, this),
	// "DCJSBridge");
	// }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (mActivityForResult != null) {
			mActivityForResult.onActivityResult(requestCode, resultCode, data);
		}
		mCtrl.onActivityResult(requestCode, resultCode, data);

	}

	/**
	 * Activity回调数据
	 * 
	 * @author LiaoBo
	 */
	public interface ActivityResult {
		public void onActivityResult(int requestCode, int resultCode,
				Intent data);
	}

	public void setActivityForResult(ActivityResult mResult) {
		mActivityForResult = mResult;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (mWebView.canGoBack()) {
				mWebView.goBack();
			} else {
				goBack();
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void isCancelLogin(boolean isCancel) {
		if (isCancel) {
			if (mWebView.canGoBack()) {
				mWebView.goBack();
			} else {
				this.finish();
			}
		} else {
			if (loginUrl != null) {// 如果拦截到网页登录，登录成功则跳转到loginUrl
				mWebView.loadUrl(loginUrl);
				// loginUrl = null;
			} else {
				mWebView.reload();// 刷新
			}
		}
	}

	@Override
	public void preExecute(ResponseData request) {

	}

	@Override
	public void notifyMessage(ResponseData request, ResponseData response)
			throws Exception {

	}

	@Override
	public void notifyError(ResponseData request, ResponseData response,
			Exception e) {

	}

}
