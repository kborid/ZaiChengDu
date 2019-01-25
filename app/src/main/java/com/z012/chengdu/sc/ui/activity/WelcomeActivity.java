package com.z012.chengdu.sc.ui.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.SparseIntArray;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.orhanobut.logger.Logger;
import com.prj.sdk.app.AppContext;
import com.prj.sdk.net.bean.ResponseData;
import com.prj.sdk.net.data.DataCallback;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.util.ActivityTack;
import com.prj.sdk.util.DateUtil;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.NetworkUtil;
import com.prj.sdk.util.SharedPreferenceUtil;
import com.prj.sdk.util.SystemUtil;
import com.prj.sdk.util.ToastUtil;
import com.prj.sdk.util.UIHandler;
import com.prj.sdk.util.Utils;
import com.thunisoft.common.util.SystemInfoUtils;
import com.thunisoft.common.util.ToastUtils;
import com.umeng.analytics.MobclickAgent;
import com.z012.chengdu.sc.BuildConfig;
import com.z012.chengdu.sc.R;
import com.z012.chengdu.sc.SessionContext;
import com.z012.chengdu.sc.constants.AppConst;
import com.z012.chengdu.sc.constants.NetURL;
import com.z012.chengdu.sc.entity.WebInfoEntity;
import com.z012.chengdu.sc.net.entity.AdvertisementBean;
import com.z012.chengdu.sc.net.entity.AppAllServiceInfoBean;
import com.z012.chengdu.sc.net.entity.AppInfoBean;
import com.z012.chengdu.sc.net.entity.AppListBean;
import com.z012.chengdu.sc.net.entity.AppOtherInfoBean;
import com.z012.chengdu.sc.net.entity.NewsListBean;
import com.z012.chengdu.sc.net.entity.PushAppBean;
import com.z012.chengdu.sc.net.request.RequestBeanBuilder;
import com.z012.chengdu.sc.ui.BaseActivity;
import com.z012.chengdu.sc.ui.permission.PermissionsDef;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.net.ConnectException;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.RuntimePermissions;

/**
 * 欢迎页面
 *
 * @author kborid
 */
@RuntimePermissions
public class WelcomeActivity extends BaseActivity implements DataCallback {

    private static final String TAG = WelcomeActivity.class.getSimpleName();

    private long start = 0; // 记录启动时间
    private final long LOADING_TIME = 1000;
    private final long AD_TIME = 3000; // 等待广告加载时间
    private SparseIntArray mTag = new SparseIntArray(); // 请求结束标记，目的是判断是否显示广告
    private AdvertisementBean mAdvertBean;
    private boolean isBreak; // 点击广告，终止本页面跳转流程

    @BindView(R.id.iv_ad)
    ImageView iv_ad;
    @BindView(R.id.layoutAd)
    FrameLayout layoutAd;
    @BindView(R.id.copyright)
    FrameLayout copyRight;

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // 设置页面全屏显示
        final View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN/* | View.SYSTEM_UI_FLAG_LAYOUT_STABLE*/);
        super.onCreate(savedInstanceState);
        start = SystemClock.elapsedRealtime();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.ui_welcome;
    }

    @Override
    protected void initParams() {
        super.initParams();
        if (BuildConfig.FLAVOR.equals("chongqing")) {
            copyRight.setVisibility(View.VISIBLE);
        }
        WelcomeActivityPermissionsDispatcher.dynamicObtainPermissionWithPermissionCheck(this);
    }

    @NeedsPermission({Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_COARSE_LOCATION})
    void dynamicObtainPermission() {
        Logger.t(TAG).d("dynamicObtainStoragePermission()");
        System.out.println(SystemInfoUtils.getDeviceId(this));
        startLoading();
    }

    @OnPermissionDenied({Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_COARSE_LOCATION})
    void showDenied() {
        Logger.t(TAG).d("showDenied()");
        ToastUtils.showToast("未获得权限");
        finish();
    }

    @SuppressLint("NeedOnRequestPermissionsResult")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        WelcomeActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    private void startLoading() {
        Collections.addAll(DataLoader.getInstance().mCacheUrls, NetURL.CACHE_URL);
        new WorkerAsyncTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private static class WorkerAsyncTask extends AsyncTask<Void, Void, Void> {
        private WeakReference<WelcomeActivity> ref;

        WorkerAsyncTask(WelcomeActivity activity) {
            ref = new WeakReference<>(activity);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (null != ref.get()) {
                if (NetworkUtil.isNetworkAvailable()) {
                    ref.get().loadAppList();
                    ref.get().loadAppInfo();
                    ref.get().loadAppAdvertisement();
                    ref.get().loadProtocol();
                } else {
                    ref.get().loadCacheData();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (!NetworkUtil.isNetworkAvailable()) {
                UIHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (null != ref.get()) {
                            ref.get().intentActivity();
                        }
                    }
                }, 1000);
            }
        }
    }

    public static void startWelcomeActivity() {
        Intent intent = new Intent(AppContext.mMainContext, WelcomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        ActivityCompat.startActivity(AppContext.mMainContext, intent, null);
    }

    @OnClick(R.id.tv_skip)
    void skip() {
        isBreak = false;
        intentActivity();
    }

    @OnClick(R.id.iv_ad)
    void ad() {
        isBreak = true;
        Intent intent = new Intent(this, HtmlActivity.class);
        intent.putExtra("webEntity", new WebInfoEntity(mAdvertBean.Id, mAdvertBean.adName, mAdvertBean.linkaddress));
        intent.putExtra("goBack", "Main");// Html返回处理
        startActivity(intent);
        finish();
        // 添加友盟自定义事件
        MobclickAgent.onEvent(this, "OpeningAdTouched");
    }

    /**
     * 预加载首页缓存数据
     */
    private void loadCacheData() {
        try {

            //首页热门服务缓存
            byte[] pushService = DataLoader.getInstance().getCacheData(NetURL.PUSH_SERVICE_);
            if (pushService != null) {
                String json = new String(pushService, "UTF-8");
                ResponseData response = JSON.parseObject(json, ResponseData.class);
                if (response != null && response.body != null) {
                    String mJson = JSON.parseObject(response.body.toString()).getString("datalist");
                    List<PushAppBean> temp = JSON.parseArray(mJson, PushAppBean.class);
                    SessionContext.setAppList(temp);
                }
            }

            //首页新闻缓存
            byte[] news = DataLoader.getInstance().getCacheData(NetURL.NEWS);
            if (null != news) {
                String json = new String(news, "UTF-8");
                ResponseData response = JSON.parseObject(json, ResponseData.class);
                JSONObject mJson = JSON.parseObject(response.body.toString());
                String mJsonString = mJson.getString("hotnewstodaylist");
                List<NewsListBean.NewsItemBean> temp = JSON.parseArray(mJsonString, NewsListBean.NewsItemBean.class);
                SessionContext.setNewsList(temp);
            }

            //获取首页所有服务缓存
            byte[] data = DataLoader.getInstance().getCacheData(NetURL.MORE_COLUMN);
            if (data != null) {
                String json = new String(data, "UTF-8");
                ResponseData response = JSON.parseObject(json, ResponseData.class);
                if (response != null && response.body != null) {
                    JSONObject mJson = JSON.parseObject(response.body.toString());
                    String jsonString = mJson.getString("datalist");
                    List<AppAllServiceInfoBean> temp = JSON.parseArray(jsonString, AppAllServiceInfoBean.class);
                    SessionContext.setHomeAllAppList(temp);
                }
            }

            //获取所有服务缓存
            byte[] allApp = DataLoader.getInstance().getCacheData(NetURL.ALL_APP);
            if (allApp != null) {
                String json = new String(allApp, "UTF-8");
                ResponseData response = JSON.parseObject(json, ResponseData.class);
                if (response != null && response.body != null) {
                    String mJson = JSON.parseObject(response.body.toString()).getString("datalist");
                    List<AppListBean> temp = JSON.parseArray(mJson, AppListBean.class);
                    SessionContext.setAllAppList(temp);
                }
            }

            // 获取广告缓存
            String ad = SharedPreferenceUtil.getInstance().getString(AppConst.ADVERTISEMENT_INFO, "", false);
            if (!TextUtils.isEmpty(ad)) {
                mAdvertBean = JSON.parseObject(ad, AdvertisementBean.class);
                loadAdImageIfNeed(iv_ad, mAdvertBean.picture);
            }
        } catch (Exception e) {
            e.printStackTrace();
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

        DataLoader.getInstance().loadData(this, data);
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

        DataLoader.getInstance().loadData(this, data);
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

        DataLoader.getInstance().loadData(this, data);
    }

    private void loadProtocol() {
        RequestBeanBuilder b = RequestBeanBuilder.create(false);
        b.addBody("areaid", SessionContext.getAreaInfo(1));

        ResponseData d = b.syncRequest(b);
        d.path = NetURL.APPSTORE;
        d.flag = 4;

        DataLoader.getInstance().loadData(this, d);
    }

    /**
     * 跳转到相应Activity
     */
    private void intentActivity() {
        if (isBreak) {
            return;
        }
        // 上次的版本code
        int last_version = SharedPreferenceUtil.getInstance().getInt(AppConst.LAST_USE_VERSIONCODE, 0);
        // 当前版本code
        int current = BuildConfig.VERSION_CODE;
        Intent intent = null;
        if (last_version < current) {
            intent = new Intent(this, UserGuideActivity.class);
        } else {
            intent = new Intent(this, MainFragmentActivity.class);
            if (null != getIntent().getExtras() && null != getIntent().getExtras().getString("path")) {
                intent.putExtra("path", getIntent().getExtras().getString("path"));
            }
        }
        startActivity(intent);
        finish();
    }

    /**
     * 显示广告
     */
    private void showAd() {
        LogUtil.d(TAG, "showAd()");
        long end = SystemClock.elapsedRealtime();
        LogUtil.d(TAG, "showAd() end - start = " + (end - start));
        long constTime = end - start;
        UIHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                layoutAd.setVisibility(View.VISIBLE);
            }
        }, constTime >= LOADING_TIME ? 0 : LOADING_TIME - constTime);
    }

    /**
     * 跳转到下一个页面
     */
    private void goToNextActivity() {
        LogUtil.i(TAG, "size = " + mTag.size());
        if (mTag.size() != 2) {// 如果初始化数据没有加载完，就一直停留加载页
            return;
        }

        mTag.put(4, 4);

        long end = SystemClock.elapsedRealtime();

        if (end - start < LOADING_TIME) {
            // 延迟加载
            UIHandler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    UIHandler.postDelayed(new Runnable() {// 延迟加载3s，主要是做显示广告

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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == PermissionsDef.PERMISSIONS_DENIED) {
            finish();
        }
    }

    @Override
    public void preExecute(ResponseData request) {

    }

    @Override
    public void notifyMessage(final ResponseData request, final ResponseData response) throws Exception {
        if (request.flag == 1) {
            mTag.put(request.flag, request.flag);// 记录请求成功状态
            SessionContext.getAllAppList().clear();
            JSONObject mJson = JSON.parseObject(response.body.toString());
            String json = mJson.getString("datalist");
            List<AppListBean> temp = JSON.parseArray(json, AppListBean.class);
            SessionContext.setAllAppList(temp);
        } else if (request.flag == 2) {
            AppInfoBean mAppInfo = JSON.parseObject(response.body.toString(), AppInfoBean.class);
            if (mAppInfo.isforce == 1 && SystemUtil.compareVersion(mAppInfo.vsid, BuildConfig.VERSION_NAME) > 0) {// 是否强制升级 0 是 1 否 并且服务器版本大于当前版本
                showUpdateDialog(mAppInfo.apkurls, mAppInfo.updesc);
            } else {
                mTag.put(request.flag, request.flag);// 记录请求成功状态
            }
            // 缓存APP信息
            SharedPreferenceUtil.getInstance().setString(AppConst.APP_INFO, response.body.toString(), false);
        } else if (request.flag == 3) {
            mAdvertBean = JSON.parseObject(response.body.toString(), AdvertisementBean.class);
            if (!TextUtils.isEmpty(mAdvertBean.picture)) {
                loadAdImageIfNeed(iv_ad, mAdvertBean.picture);
            }
            SharedPreferenceUtil.getInstance().setString(AppConst.ADVERTISEMENT_INFO, response.body.toString(), false);
        } else if (request.flag == 4) {
            AppOtherInfoBean bean = JSON.parseObject(response.body.toString(), AppOtherInfoBean.class);
            SharedPreferenceUtil.getInstance().setString(AppConst.ABOUT_ICON, bean.AboutImage, true);
            SharedPreferenceUtil.getInstance().setString(AppConst.ABOUT_US, bean.AboutInfoURL, true);
            SharedPreferenceUtil.getInstance().setString(AppConst.REGISTER_AGEMENT, bean.RegisterProtocolURL, true);
            SharedPreferenceUtil.getInstance().setString(AppConst.PROBLEM, bean.FaqURL, true);
            SharedPreferenceUtil.getInstance().setString(AppConst.IDENTITY_PROTOCOL, bean.IdentityProtocolURL, true);
        }
        goToNextActivity();
    }

    @Override
    public void notifyError(ResponseData request, ResponseData response, Exception e) {
        String message;
        if (e instanceof ConnectException) {
            message = getString(R.string.dialog_tip_net_error);
            ToastUtil.show(message, Toast.LENGTH_LONG);
        }

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
     * @param url         下载地址
     * @param description 更新描述
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
                    Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
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
    public void loadAdImageIfNeed(final ImageView iView, String url) {
        LogUtil.d(TAG, "loadAdImageIfNeed()");
        if (TextUtils.isEmpty(url)) {
            return;
        }
        LogUtil.d(TAG, "loadAdImageIfNeed() url = " + url);
        if (!url.startsWith("http")) {
            url = NetURL.API_LINK + url;// 拼接广告图片路径
        }
        Glide.with(this).load(url).crossFade().into(new SimpleTarget<GlideDrawable>() {
            @Override
            public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                if (null != resource) {
                    iView.setImageDrawable(resource);
                    showAd();
                }
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN
                && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            ActivityTack.getInstanse().exit();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }
}
