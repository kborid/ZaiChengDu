package com.z012.chengdu.sc.ui.JSBridge.functions;

import org.json.JSONObject;

import com.z012.chengdu.sc.app.SessionContext;
import com.z012.chengdu.sc.ui.JSBridge.WVJBWebViewClient.WVJBResponseCallback;

/**
 * 3.5 获取城市信息
 * 
 * @author LiaoBo
 */
public class getCityInfo implements com.z012.chengdu.sc.ui.JSBridge.WVJBWebViewClient.WVJBHandler {

	@Override
	public void request(Object data, WVJBResponseCallback callback) {
		try {

			if (callback != null) {
				//{“cityCode”:”500000”,”cityName”:”重庆"}
				JSONObject mJson = new JSONObject();
				mJson.put("cityCode", SessionContext.getAreaInfo(1));
				mJson.put("cityName", SessionContext.getAreaInfo(2));
				callback.callback(mJson.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
