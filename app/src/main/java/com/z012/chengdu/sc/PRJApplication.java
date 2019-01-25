package com.z012.chengdu.sc;

import android.app.Application;
import android.content.IntentFilter;
import android.os.Build;
import android.support.v4.content.LocalBroadcastManager;
import android.webkit.WebView;

import com.prj.sdk.app.AppContext;
import com.prj.sdk.util.SharedPreferenceUtil;
import com.squareup.leakcanary.LeakCanary;
import com.thunisoft.ThunisoftLogger;
import com.thunisoft.common.ThunisoftCommon;
import com.thunisoft.logger.LoggerConfig;
import com.thunisoft.ui.ThunisoftUI;
import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;
import com.z012.chengdu.sc.broadcast.UnLoginBroadcastReceiver;
import com.z012.chengdu.sc.constants.AppConst;
import com.z012.chengdu.sc.helper.CrashHandler;

import cn.jpush.android.api.JPushInterface;

public class PRJApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        CrashHandler.getInstance().init(this);
        ThunisoftCommon.init(this);
        ThunisoftUI.init(this);
        initLog();
        AppContext.init(this);
        registerLoginBroadcast();
        initUMeng();
        initJPush();
        initWebViewDebug();
        initLeakCanary();
        SessionContext.initUserInfo();
        SessionContext.setAreaCode(getString(R.string.areaCode), getString(R.string.areaName));
    }

    private void initLog() {
        ThunisoftLogger.initLogger(this, new LoggerConfig() {
            @Override
            public String getTag() {
                return "Digital";
            }

            @Override
            public boolean isDebug() {
                return BuildConfig.DEBUG;
            }
        });
    }

    private void registerLoginBroadcast() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AppConst.ACTION_UNLOGIN);
        LocalBroadcastManager.getInstance(this).registerReceiver(new UnLoginBroadcastReceiver(), intentFilter);
    }

    private void initUMeng() {
        MobclickAgent.setDebugMode(AppConst.ISDEVELOP);// 普通测试流程，打开调试模式
        MobclickAgent.updateOnlineConfig(this);// 友盟统计发送策略，在线参数如配置时间、开关等
        MobclickAgent.openActivityDurationTrack(false); // 禁止默认的页面统计方式
        UmengUpdateAgent.setUpdateOnlyWifi(false);// 允许在非wifi下检查更新
    }

    private void initJPush() {
        boolean mEnable = SharedPreferenceUtil.getInstance().getBoolean(AppConst.PUSH_ENABLE, true);
        if (mEnable) {
            JPushInterface.setDebugMode(AppConst.ISDEVELOP); // 设置开启日志,发布时请关闭日志
            JPushInterface.init(this); // 初始化 JPush
        }
    }

    private void initWebViewDebug() {
        if (AppConst.ISDEVELOP) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                WebView.setWebContentsDebuggingEnabled(true);
            }
        }
    }

    private void initLeakCanary() {
        if (AppConst.ISDEVELOP) {
            LeakCanary.install(this);
        }
    }
}
