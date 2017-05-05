package com.z012.chengdu.sc.ui.JSBridge.functions;

import com.prj.sdk.app.AppContext;
import com.z012.chengdu.sc.ui.JSBridge.WVJBWebViewClient.WVJBResponseCallback;

/**
 * 2.3. 获取一个缓存值
 * 
 * @author LiaoBo
 * 
 */
public class getCacheValue implements com.z012.chengdu.sc.ui.JSBridge.WVJBWebViewClient.WVJBHandler {
	@Override
	public void request(Object data, WVJBResponseCallback callback) {
		if (callback != null) {
			callback.callback(AppContext.mMemoryMap.get(data.toString()));
		}
	}

}