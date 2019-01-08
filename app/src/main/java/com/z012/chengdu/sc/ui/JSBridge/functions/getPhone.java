package com.z012.chengdu.sc.ui.JSBridge.functions;

import org.json.JSONObject;

import android.content.Intent;

import com.prj.sdk.app.AppContext;
import com.z012.chengdu.sc.SessionContext;
import com.z012.chengdu.sc.broatcast.UnLoginBroadcastReceiver;
import com.z012.chengdu.sc.ui.JSBridge.WVJBWebViewClient.WVJBResponseCallback;

/**
 * 获取手机号,用户未登录时调用此接口框架会要求用户去登录
 * 
 * @author LiaoBo
 */
public class getPhone implements com.z012.chengdu.sc.ui.JSBridge.WVJBWebViewClient.WVJBHandler {

	@Override
	public void request(Object data, WVJBResponseCallback callback) {
		try {

			if (callback != null) {
				if (SessionContext.isLogin()) {
					JSONObject mJson = new JSONObject();
					mJson.put("phone", SessionContext.mUser.USERAUTH.mobilenum);
					callback.callback(mJson.toString());
				} else {
					AppContext.mMainContext.sendBroadcast(new Intent(UnLoginBroadcastReceiver.ACTION_NAME));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}