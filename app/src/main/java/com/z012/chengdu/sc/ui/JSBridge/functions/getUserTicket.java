package com.z012.chengdu.sc.ui.JSBridge.functions;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.prj.sdk.app.AppContext;
import com.prj.sdk.util.StringUtil;
import com.z012.chengdu.sc.SessionContext;
import com.z012.chengdu.sc.constants.AppConst;
import com.z012.chengdu.sc.ui.JSBridge.WVJBWebViewClient.WVJBResponseCallback;

import org.json.JSONObject;

/**
 * 2.5. 获取访问凭据，Null时为未登录
 * 
 * @author LiaoBo
 */
public class getUserTicket implements com.z012.chengdu.sc.ui.JSBridge.WVJBWebViewClient.WVJBHandler {
	@Override
	public void request(Object data, WVJBResponseCallback callback) {
		try {

			if (callback != null) {
				if (StringUtil.notEmpty(SessionContext.getTicket())) {
					JSONObject mJson = new JSONObject();
					mJson.put("userTicket", SessionContext.getTicket());
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
