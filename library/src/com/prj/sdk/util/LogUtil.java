package com.prj.sdk.util;

import android.util.Log;

import com.prj.sdk.BuildConfig;

/**
 * 对android自带日志的一个简单封装，方便调用
 * @author kborid
 * 
 */
public class LogUtil {

	public static boolean isDebug() {
		return BuildConfig.DEBUG;
	}

	// 冗余信息输出
	public static void v(String tag, String msg) {
		if (isDebug()) {
			Log.v(tag, "{Thread:" + Thread.currentThread().getName() + "}" + msg);
		}
	}

	// 调试信息输出
	public static void d(String tag, String msg) {
		if (isDebug()) {
			Log.d(tag, "{Thread:" + Thread.currentThread().getName() + "}" + msg);
		}
	}

	// 提示信息输出
	public static void i(String tag, String msg) {
		if (isDebug()) {
			Log.i(tag, "{Thread:" + Thread.currentThread().getName() + "}" + msg);
		}
	}

	// 警告信息输出
	public static void w(String tag, String msg) {
		if (isDebug()) {
			Log.w(tag, "{Thread:" + Thread.currentThread().getName() + "}" + msg);
		}
	}

	// 错误信息输出
	public static void e(String tag, String msg) {
		if (isDebug()) {
			Log.e(tag, "{Thread:" + Thread.currentThread().getName() + "}" + msg);
		}
	}
}
