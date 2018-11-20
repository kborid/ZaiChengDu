package com.z012.chengdu.sc.ui.JSBridge.functions;

import android.content.Context;
import android.content.Intent;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.prj.sdk.app.AppContext;
import com.prj.sdk.util.StringUtil;
import com.z012.chengdu.sc.broatcast.UnLoginBroadcastReceiver;
import com.z012.chengdu.sc.ui.JSBridge.WVJBWebViewClient.WVJBResponseCallback;

/**
 * 8. Private_处理错误信息
 * 
 * @author LiaoBo
 */
public class handleError implements com.z012.chengdu.sc.ui.JSBridge.WVJBWebViewClient.WVJBHandler {
	private Context	mContext;

	public handleError(Context context) {
		mContext = context;
	}

	@Override
	public void request(Object data, WVJBResponseCallback callback) {
		try {

//			if (callback == null) {
//				return;
//			}
			if (StringUtil.empty(data)) {
				return;
			}
			// 解析请求参数
			JSONObject mJson = JSON.parseObject(data.toString());
			String rtnCode = mJson.getString("rtnCode");
			String rtnMsg = mJson.getString("rtnMsg");
			if (rtnCode != null && (rtnCode.equals("900902") || rtnCode.equals("310001"))) {// 900902，310001//登陆过期
				AppContext.mMainContext.sendBroadcast(new Intent(UnLoginBroadcastReceiver.ACTION_NAME));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
