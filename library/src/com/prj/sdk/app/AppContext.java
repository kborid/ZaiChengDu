package com.prj.sdk.app;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import com.prj.sdk.db.DBManager;
import com.prj.sdk.net.data.DataLoader;

import java.util.HashMap;
import java.util.Map;

/**
 * 记录应用全局信息
 * 
 * @author LiaoBo
 * @body 2013-12-31
 */
public final class AppContext {

	public static Map<String, Object> mMemoryMap = null; // 提供调用memory存取值
	public static DBManager mDBManager = null;
	/*
	 * 初始化上下问
	 */
	public static Context mMainContext = null;

	public static void init(Context MainContext) {
		mMainContext = MainContext.getApplicationContext();
		mMemoryMap = new HashMap<String, Object>();
		mDBManager = DBManager.getInstance(MainContext, null);
	}

	/**
	 * 销毁全局变量
	 */
	public static void destory() {
		DataLoader.getInstance().clearRequests();
		mMemoryMap.clear();
		mDBManager = null;
	}

	/**
	 * 获取版本号
	 * 
	 * @return 当前应用的版本号
	 */
	public static String getVersion() {
		try {
			PackageManager manager = AppContext.mMainContext
					.getPackageManager();
			PackageInfo info = manager.getPackageInfo(
					AppContext.mMainContext.getPackageName(), 0);
			return info.versionName;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * 获取手机ip地址
	 * 
	 * @return
	 */
	public static String getLocalIpAddress() {
		WifiManager wifiManager = (WifiManager) AppContext.mMainContext
				.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		int ipAddress = wifiInfo.getIpAddress();
		if (ipAddress == 0) {
			return null;
		}
		// 对int类型的ip地址做一次转换
		return ((ipAddress & 0xff) + "." + (ipAddress >> 8 & 0xff) + "."
				+ (ipAddress >> 16 & 0xff) + "." + (ipAddress >> 24 & 0xff));
	}

	/**
	 * 比较系统版本号
	 * 
	 * @param v1
	 * @param v2
	 * @return 0--等；-1--小；1--大
	 */
	public static int compareVersion(String v1, String v2) {
		if (v1 == null) {
			return -1;
		}

		if (v2 == null) {
			return 1;
		}

		if (v1.equals(v2)) {
			return 0;
		}

		String[] arrayStr1 = v1.split("\\.");
		String[] arrayStr2 = v2.split("\\.");

		int leng1 = arrayStr1.length;
		int leng2 = arrayStr2.length;
		int leng = (leng1 > leng2) ? leng2 : leng1;

		int result = 0;
		for (int i = 0; i < leng; i++) {
			result = arrayStr1[i].length() - arrayStr2[i].length();
			if (result != 0) {
				return result > 0 ? 1 : -1;
			}
			result = arrayStr1[i].compareTo(arrayStr2[i]);
			if (result != 0) {
				return result > 0 ? 1 : -1;
			}
		}

		if (result == 0) {
			if (leng1 > leng2) {
				result = 1;
			} else if (leng1 < leng2) {
				result = -1;
			}
		}

		return result;
	}

	/**
	 * 获取application中指定的meta-data
	 * 
	 * @return 如果没有获取成功(没有对应值，或者异常)，则返回值为空
	 */
	public static String getAppMetaData(Context ctx, String key) {
		if (ctx == null || TextUtils.isEmpty(key)) {
			return null;
		}
		String resultData = null;
		try {
			PackageManager packageManager = ctx.getPackageManager();
			if (packageManager != null) {
				ApplicationInfo applicationInfo = packageManager
						.getApplicationInfo(ctx.getPackageName(),
								PackageManager.GET_META_DATA);
				if (applicationInfo != null) {
					if (applicationInfo.metaData != null) {
						resultData = applicationInfo.metaData.getString(key);
					}
				}

			}
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}

		return resultData;
	}

}
