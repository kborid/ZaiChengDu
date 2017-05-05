package com.z012.chengdu.sc.app;

import java.util.Collections;

import android.app.Application;
import android.content.IntentFilter;
import cn.jpush.android.api.JPushInterface;

import com.prj.sdk.app.AppContext;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.util.ActivityTack;
import com.prj.sdk.util.SharedPreferenceUtil;
import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;
import com.z012.chengdu.sc.action.LocationManagerBD;
import com.z012.chengdu.sc.broatcast.UnLoginBroadcastReceiver;
import com.z012.chengdu.sc.constants.AppConst;
import com.z012.chengdu.sc.constants.NetURL;

/**
 * 应用全文
 * 
 * @author LiaoBo
 * 
 */
public class PRJAppplcation extends Application {
	
	private UnLoginBroadcastReceiver receiver;
	@Override
	public void onCreate() {
		super.onCreate();
		AppContext.init(this);
		CrashHandler.getInstance().init(this);
		MobclickAgent.setDebugMode(false);// 普通测试流程，打开调试模式
		Collections.addAll(DataLoader.getInstance().mCacheUrls,
				NetURL.CACHE_URL);
		MobclickAgent.updateOnlineConfig(this);// 友盟统计发送策略，在线参数如配置时间、开关等
		MobclickAgent.openActivityDurationTrack(false); // 禁止默认的页面统计方式
		UmengUpdateAgent.setUpdateOnlyWifi(false);// 允许在非wifi下检查更新
		boolean mEnable = SharedPreferenceUtil.getInstance().getBoolean(
				AppConst.PUSH_ENABLE, true);
		if (mEnable) {
			JPushInterface.setDebugMode(false); // 设置开启日志,发布时请关闭日志
			JPushInterface.init(this); // 初始化 JPush
		}
		
		LocationManagerBD.getIns().initBaiduLocation(this);

		// 动态注册登录广播
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(UnLoginBroadcastReceiver.ACTION_NAME);
		receiver = new UnLoginBroadcastReceiver();
		registerReceiver(receiver, intentFilter);
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
	}

	@Override
	public void onTerminate() {
		ActivityTack.getInstanse().exit();
		unregisterReceiver(receiver);
		DataLoader.getInstance().clearRequests();
	}

}
