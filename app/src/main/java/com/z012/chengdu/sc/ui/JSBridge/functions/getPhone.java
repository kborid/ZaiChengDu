package com.z012.chengdu.sc.ui.JSBridge.functions;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.prj.sdk.app.AppContext;
import com.z012.chengdu.sc.SessionContext;
import com.z012.chengdu.sc.constants.AppConst;
import com.z012.chengdu.sc.ui.JSBridge.WVJBWebViewClient.WVJBResponseCallback;

import org.json.JSONObject;

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
					LocalBroadcastManager.getInstance(AppContext.mMainContext).sendBroadcast(new Intent(AppConst.ACTION_UNLOGIN));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}