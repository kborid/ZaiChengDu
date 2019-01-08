package com.z012.chengdu.sc.ui.JSBridge.functions;

import com.alibaba.fastjson.JSON;
import com.z012.chengdu.sc.SessionContext;
import com.z012.chengdu.sc.ui.JSBridge.WVJBWebViewClient.WVJBResponseCallback;

/**
 * 2.7.	获取USER数据
 * 
 * @author LiaoBo
 */
public class getUser implements com.z012.chengdu.sc.ui.JSBridge.WVJBWebViewClient.WVJBHandler {
	@Override
	public void request(Object data, WVJBResponseCallback callback) {
		try {

			if (callback != null)
				callback.callback(JSON.toJSONString(SessionContext.mUser));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
