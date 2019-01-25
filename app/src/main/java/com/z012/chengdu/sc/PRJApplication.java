package com.z012.chengdu.sc;

import android.app.Application;
import android.content.IntentFilter;
import android.os.Build;
import android.support.v4.content.LocalBroadcastManager;
import android.webkit.WebView;

import com.orhanobut.logger.DiskLogAdapter;
import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.PrettyFormatStrategy;
import com.prj.sdk.app.AppContext;
import com.prj.sdk.util.SharedPreferenceUtil;
import com.squareup.leakcanary.LeakCanary;
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
        initLog();
        AppContext.init(this);
        CrashHandler.getInstance().init(this);
        initLoginBroadcast();
        initUMeng();
        initJPush();
        initWebViewDebug();
        initLeakCanary();
    }

    private void initLog() {
        FormatStrategy formatStrategy = PrettyFormatStrategy.newBuilder()
                .showThreadInfo(false)  // (Optional) Whether to show thread info or not. Default true
                .methodCount(0)         // (Optional) How many method line to show. Default 2
                .methodOffset(0)        // (Optional) Hides internal method calls up to offset. Default 5
                .tag("Digital")         // (Optional) Global tag for every log. Default PRETTY_LOGGER
                .build();
        Logger.addLogAdapter(new DiskLogAdapter(formatStrategy));
    }

    private void initLoginBroadcast() {
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
            if (LeakCanary.isInAnalyzerProcess(this)) {
                return;
            }
            LeakCanary.install(this);
        }
    }
}
