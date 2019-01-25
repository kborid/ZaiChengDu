package com.z012.chengdu.sc.ui.JSBridge;

import com.z012.chengdu.sc.ui.JSBridge.functions.getLocation;

import android.content.Context;

/**
 * 注册处理程序，使JavaScript可以调用
 * 
 * @author LiaoBo
 * 
 */
public class RegisterHandler {

	private WVJBWebViewClient	mWVJBWebViewClient;
	private Context				mContext;

	/**
	 * 构造函数
	 * 
	 * @param mContext
	 */
	public RegisterHandler(WVJBWebViewClient mWVJBWebViewClient, Context mContext) {
		this.mWVJBWebViewClient = mWVJBWebViewClient;
		this.mContext = mContext;
	}

	/**
	 * 初始化，注册处理程序
	 */
	public void init() {
		// mWVJBWebViewClient.registerHandler("showLoginModule", new com.z012.chengdu.sc.ui.JSBridge.functions.showLoginModule());
		// mWVJBWebViewClient.registerHandler("setCacheValue", new com.z012.chengdu.sc.ui.JSBridge.functions.setCacheValue());
		// mWVJBWebViewClient.registerHandler("getNetworkStatus", new com.z012.chengdu.sc.ui.JSBridge.functions.getNetworkStatus());
		// mWVJBWebViewClient.registerHandler("getCacheValue", new com.z012.chengdu.sc.ui.JSBridge.functions.getCacheValue());
		// mWVJBWebViewClient.registerHandler("getCacheValue", new com.z012.chengdu.sc.ui.JSBridge.functions.loadRequest());
		mWVJBWebViewClient.registerHandler("openURL", new com.z012.chengdu.sc.ui.JSBridge.functions.openURL(mContext));
		mWVJBWebViewClient.registerHandler("getUserTicket", new com.z012.chengdu.sc.ui.JSBridge.functions.getUserTicket());
		mWVJBWebViewClient.registerHandler("getPicturesUpload", new com.z012.chengdu.sc.ui.JSBridge.functions.getPicturesUpload(mContext));
		mWVJBWebViewClient.registerHandler("getUserId", new com.z012.chengdu.sc.ui.JSBridge.functions.getUserId());
		mWVJBWebViewClient.registerHandler("getPhone", new com.z012.chengdu.sc.ui.JSBridge.functions.getPhone());
		mWVJBWebViewClient.registerHandler("getDeviceId", new com.z012.chengdu.sc.ui.JSBridge.functions.getDeviceId());
		mWVJBWebViewClient.registerHandler("showException", new com.z012.chengdu.sc.ui.JSBridge.functions.showException(mContext));
		mWVJBWebViewClient.registerHandler("getCityInfo", new com.z012.chengdu.sc.ui.JSBridge.functions.getCityInfo());
		mWVJBWebViewClient.registerHandler("handleError", new com.z012.chengdu.sc.ui.JSBridge.functions.handleError(mContext));
		mWVJBWebViewClient.registerHandler("payOrder", new com.z012.chengdu.sc.ui.JSBridge.functions.payOrder(mContext));
		mWVJBWebViewClient.registerHandler("getLocation", new com.z012.chengdu.sc.ui.JSBridge.functions.getLocation());
	}

}
