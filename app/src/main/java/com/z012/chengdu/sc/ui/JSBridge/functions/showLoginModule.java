package com.z012.chengdu.sc.ui.JSBridge.functions;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.prj.sdk.app.AppContext;
import com.z012.chengdu.sc.constants.AppConst;
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
		LocalBroadcastManager.getInstance(AppContext.mMainContext).sendBroadcast(new Intent(AppConst.ACTION_UNLOGIN));
	}

}
