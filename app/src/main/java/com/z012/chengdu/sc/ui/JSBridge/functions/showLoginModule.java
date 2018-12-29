package com.z012.chengdu.sc.ui.JSBridge.functions;

import android.content.Intent;

import com.prj.sdk.app.AppContext;
import com.z012.chengdu.sc.broatcast.UnLoginBroadcastReceiver;
import com.z012.chengdu.sc.ui.JSBridge.WVJBWebViewClient.WVJBResponseCallback;

/**
 * 打开登录界面
 * 
 * @author LiaoBo
 * 
 */
public class showLoginModule implements com.z012.chengdu.sc.ui.JSBridge.WVJBWebViewClient.WVJBHandler {
	@Override
	public void request(Object data, WVJBResponseCallback callback) {
		AppContext.mMainContext.sendBroadcast(new Intent(UnLoginBroadcastReceiver.ACTION_NAME));
	}

}
