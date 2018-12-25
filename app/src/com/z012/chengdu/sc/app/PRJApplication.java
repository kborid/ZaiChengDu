package com.z012.chengdu.sc.app;

import android.app.Application;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Build;
import android.webkit.WebView;

import com.prj.sdk.app.AppContext;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.util.ActivityTack;
import com.prj.sdk.util.SharedPreferenceUtil;
import com.squareup.leakcanary.LeakCanary;
import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;
import com.z012.chengdu.sc.broatcast.UnLoginBroadcastReceiver;
import com.z012.chengdu.sc.constants.AppConst;
import com.z012.chengdu.sc.permission.PermissionsChecker;

import cn.jpush.android.api.JPushInterface;

/**
 * 应用全文
 * 
 * @author LiaoBo
 * 
 */
public class PRJApplication extends Application {
	
	private UnLoginBroadcastReceiver receiver;
	private static PRJApplication mInstance = null;

	private PermissionsChecker mPermissionsChecker;

	@Override
	protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        mInstance = this;
    }

	@Override
	public void onCreate() {
		super.onCreate();
		AppContext.init(this);
		CrashHandler.getInstance().init(this);

		// 动态注册登录广播
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(UnLoginBroadcastReceiver.ACTION_NAME);
		receiver = new UnLoginBroadcastReceiver();
		registerReceiver(receiver, intentFilter);

		initUmeng();
		initJPush();
		initWebViewDebug();
		initLeakCanary();
		mPermissionsChecker = new PermissionsChecker(this);
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		ActivityTack.getInstanse().exit();
		unregisterReceiver(receiver);
		DataLoader.getInstance().clearRequests();
	}

	public static PRJApplication getInstance() {
		return mInstance;
	}

    public static PermissionsChecker getPermissionsChecker(Context context) {
        PRJApplication app = (PRJApplication) context.getApplicationContext();
        return app.mPermissionsChecker;
    }

    private void initUmeng() {
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
		// Debug模式下打开webview debug开关
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
