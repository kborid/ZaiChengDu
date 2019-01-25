package com.z012.chengdu.sc.ui.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.DownloadListener;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baidu.location.BDLocation;
import com.common.share.ShareBeanInfo;
import com.common.widget.custom.CommonLoadingWidget;
import com.common.widget.custom.CustomShareView;
import com.prj.sdk.app.AppContext;
import com.prj.sdk.util.ActivityTack;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.SharedPreferenceUtil;
import com.prj.sdk.util.StringUtil;
import com.prj.sdk.util.ToastUtil;
import com.prj.sdk.util.UIHandler;
import com.prj.sdk.util.Utils;
import com.prj.sdk.widget.webview.ChooserFileController;
import com.prj.sdk.widget.webview.WebChromeClientCompat;
import com.z012.chengdu.sc.BuildConfig;
import com.z012.chengdu.sc.R;
import com.z012.chengdu.sc.SessionContext;
import com.z012.chengdu.sc.constants.AppConst;
import com.z012.chengdu.sc.constants.NetURL;
import com.z012.chengdu.sc.entity.WebInfoEntity;
import com.z012.chengdu.sc.helper.LocationManagerBD;
import com.z012.chengdu.sc.helper.LocationManagerBD.LocationCallback;
import com.z012.chengdu.sc.ui.BaseActivity;
import com.z012.chengdu.sc.ui.JSBridge.RegisterHandler;
import com.z012.chengdu.sc.ui.JSBridge.WVJBWebViewClient;
import com.z012.chengdu.sc.ui.activity.user.LoginActivity;
import com.z012.chengdu.sc.ui.activity.user.LoginActivity.onCancelLoginListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.OnClick;
import cmb.pb.util.CMBKeyboardFunc;

/**
 * 中间件处理，加载html5应用数据
 *
 * @author LiaoBo
 * @date 2014-7-8
 */
@SuppressLint("SetJavaScriptEnabled")
public class HtmlActivity extends BaseActivity implements onCancelLoginListener {
    public final static String CSS_STYLE = "<style>* {font-size:40px;padding:10px;}</style>";
    private WebView mWebView;
    private CommonLoadingWidget common_loading_widget;
    private EditText et_url;
    private ActivityResult mActivityForResult;
    private String loginUrl;
    private TextView tv_left_title_back, tv_left_title_close;
    private ChooserFileController mCtrl;
    private MyWebViewClient myWebViewClient = null;
    private ImageView iv_share;
    private PopupWindow popupWindow = null;
    private CustomShareView customShareView;
    private List<ShareBeanInfo> list = new ArrayList<>();

    private String mId, mTitle, mUrl;

    @Override
    protected int getLayoutResId() {
        return R.layout.ui_web_act;
    }

    private void createController() {
        mCtrl = new ChooserFileController(this);
    }

    /**
     * 开发者测试
     */
    public void initDevelop() {
        LinearLayout layout_test = (LinearLayout) findViewById(R.id.layout_test);
        et_url = (EditText) findViewById(R.id.et_url);
        Button btn_go = (Button) findViewById(R.id.btn_go);
        TextView tv_cur = (TextView) findViewById(R.id.tv_cur);
        StringBuilder sb = new StringBuilder();
        sb.append("\nCurrent Environment（")
                .append(SharedPreferenceUtil.getInstance().getInt(
                        AppConst.APPTYPE, 0)).append("：")
                .append(NetURL.API_LINK).append("）");
        tv_cur.setText(sb);
        tv_cur.setVisibility(View.VISIBLE);
        layout_test.setVisibility(View.VISIBLE);
        btn_go.setOnClickListener(new android.view.View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mUrl = et_url.getText().toString().trim();
                if ("0".equals(mUrl) || "1".equals(mUrl) || "2".equals(mUrl)) {
                    SharedPreferenceUtil.getInstance().setInt(AppConst.APPTYPE,
                            Integer.parseInt(mUrl));// 保存切换地址类型

                    UIHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            SessionContext.cleanUserInfo();
                            SessionContext.destroy();
                            SharedPreferenceUtil.getInstance().setString(
                                    AppConst.MAIN_IMG_DATA, "", false);
                            ActivityTack.getInstanse().exit();
                        }
                    }, 2000);
                    ToastUtil.show("切换成功，即将退出，请手动重启", 0);
                    return;
                }

                mWebView.loadUrl(mUrl);
            }
        });
    }

    @OnClick(R.id.tv_left_title_back)
    void back() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            goBack();
        }
    }

    @OnClick(R.id.tv_left_title_close)
    void close() {
        goBack();
    }

    @OnClick(R.id.iv_share)
    void share() {
        showPopupWindow();
    }

    private void showPopupWindow() {
        if (null == popupWindow) {
            popupWindow = new PopupWindow(customShareView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        }
//        popupWindow.setAnimationStyle(R.style.share_anmi);
        popupWindow.setBackgroundDrawable(new ColorDrawable(0));
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowManager.LayoutParams params = getWindow().getAttributes();
                params.alpha = 1.0f;
                getWindow().setAttributes(params);
            }
        });
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.alpha = 0.8f;
        getWindow().setAttributes(params);
        int value = 0;
//        if (Utils.hasNavBar()) {
//            value = Utils.mNavigationBarHeight;
//        }
        popupWindow.showAtLocation(iv_share, Gravity.BOTTOM, 0, value);
    }

    private CustomShareView.OnFunctionListener listener = new CustomShareView.OnFunctionListener() {
        @Override
        public void dismiss() {
            popupWindow.dismiss();
        }

        @Override
        public void action(int i) {
            myWebViewClient.callHandler(list.get(i).action, null, new WVJBWebViewClient.WVJBResponseCallback() {
                @Override
                public void callback(Object data) {
                    if (data != null) {
                        LogUtil.d("HtmlActivity", "data = " + data.toString());
                    }
                }
            });
        }

        @Override
        public void refresh() {
            mWebView.reload();
        }
    };

    @Override
    public void dealIntent() {
        super.dealIntent();
        Bundle bundle = getIntent().getExtras();
        if (null != bundle) {
            if (null != bundle.getSerializable("webEntity")) {
                WebInfoEntity webEntity = (WebInfoEntity) bundle.getSerializable("webEntity");
                if (null != webEntity) {
                    mId = webEntity.getId();
                    mTitle = webEntity.getTitle();
                    mUrl = webEntity.getUrl();
                }
            }
        }
    }

    public void initParams() {
        super.initParams();
        mWebView = (WebView) findViewById(R.id.webview);
        common_loading_widget = (CommonLoadingWidget) findViewById(R.id.common_loading_widget);
        tv_left_title_back = (TextView) findViewById(R.id.tv_left_title_back);
        tv_left_title_close = (TextView) findViewById(R.id.tv_left_title_close);
        iv_share = (ImageView) findViewById(R.id.iv_share);
        customShareView = new CustomShareView(this);

        createController();
        if (AppConst.ISDEVELOP) {
            if (getIntent().getBooleanExtra("ISDEVELOP", false)) {
                initDevelop();
            }
        }
        tv_center_title.setText(mTitle);
        LoginActivity.setCancelLogin(this);
        WebSettings webSetting = mWebView.getSettings();
        webSetting.setJavaScriptEnabled(true);
        webSetting.setJavaScriptCanOpenWindowsAutomatically(true);
        webSetting.setCacheMode(WebSettings.LOAD_DEFAULT);
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
        StringBuilder sb = new StringBuilder();
        sb.append(webSetting.getUserAgentString()).append(" Android/")
                .append(BuildConfig.APPLICATION_ID).append("/").append(BuildConfig.VERSION_NAME)// 名字+包名+版本号
                .append(" ").append("CQSMT_ANDROID/1.0");
        webSetting.setUserAgentString(sb.toString());// 追加修改ua特征标识（名字+包名+版本号）使得web端正确判断
        LogUtil.i("dw", "ua = " + webSetting.getUserAgentString());
        webSetting.setLoadsImagesAutomatically(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT); // 控制图片加载处理，提高view加载速度
        mWebView.loadUrl(mUrl);

        // 增加接口方法,让html页面调用
        // addJSInterfaces();
    }

    public void initListeners() {
        super.initListeners();
        myWebViewClient = new MyWebViewClient(mWebView);
        mWebView.setWebViewClient(myWebViewClient);
        mWebView.setWebChromeClient(new WebChromeClientCompat(this, mCtrl, tv_center_title));
        customShareView.setOnDismissListener(listener);
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
                        LocalBroadcastManager.getInstance(HtmlActivity.this).sendBroadcast(new Intent(AppConst.ACTION_UNLOGIN));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            mWebView.setEnabled(false);
        }

        @Override
        public void onPageFinished(WebView view, final String url) {
            super.onPageFinished(view, url);
            // 拦截巴士公交进行原生定位,向js注入定位结果
            if (url != null && url.startsWith("http://m.basbus.cn")) {
                if (mLatitude != 0 && mLongitude != 0) {
                    final StringBuilder script = new StringBuilder();
                    script.append("getic(").append(mLongitude).append(",")
                            .append(mLatitude).append(")");
                    executeJavascript(script.toString());
                } else if (!LocationManagerBD.getIns().isStart()) {
                    LocationManagerBD.getIns().startLocation(new LocationCallback() {
                        @Override
                        public void onLocationInfo(BDLocation locationInfo) {
                            if (locationInfo == null) {
                                return;
                            }
                            try {
                                mLatitude = locationInfo.getLatitude();
                                mLongitude = locationInfo
                                        .getLongitude();
                                LocationManagerBD.getIns().stopLocation();
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


            // 点击后退，设置标题
            if (view.canGoBack()) {
                String title = view.getTitle();
                if (StringUtil.empty(title)) {
                    title = mTitle;
                } else if (title.contains("http") && title.contains("www.zaichengdu.com") && title.contains("uat.zaichengdu.com")) {
                    title = mTitle;
                }
                tv_center_title.setText(title);
                tv_left_title_close.setVisibility(View.VISIBLE);
            } else {
                tv_center_title.setText(mTitle);// 第一级设置配置的标题
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

            callHandler("addActionMethodsToNative", null, new WVJBResponseCallback() {
                @Override
                public void callback(Object data) {
                    JSONArray jsonArray = JSON.parseArray(data.toString());
                    if (list != null && list.size() > 0) {
                        list.clear();
                    }
                    for (int i = 0; i < jsonArray.size(); i++) {
                        JSONObject mmjson = (JSONObject) jsonArray.get(i);
                        ShareBeanInfo info = new ShareBeanInfo();
                        if (mmjson.containsKey("title")) {
                            info.title = mmjson.getString("title");
                        }
                        if (mmjson.containsKey("imgURL")) {
                            info.imagePath = mmjson.getString("imgURL");
                        }
                        if (mmjson.containsKey("action")) {
                            info.action = mmjson.getString("action");
                        }
                        if (mmjson.containsKey("type")) {
                            info.type = mmjson.getString("type");
                        }
                        list.add(info);
                    }
                    customShareView.updateThirdMenu(list, mTitle, url);
                }
            });
            customShareView.updateThirdMenu(list, mTitle, url);
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
            ToastUtil.show("已忽略证书信息继续加载", 0);
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
            } else {
                mWebView.reload();// 刷新
            }
        }
    }
}
