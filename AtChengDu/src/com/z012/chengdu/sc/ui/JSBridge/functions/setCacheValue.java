package com.z012.chengdu.sc.ui.JSBridge.functions;

import com.google.gson.Gson;
import com.prj.sdk.app.AppContext;
import com.prj.sdk.util.StringUtil;
import com.z012.chengdu.sc.ui.JSBridge.WVJBWebViewClient.WVJBResponseCallback;

/**
 * 缓存一个值，将在webView关闭时释放
 * 
 * @author LiaoBo
 * 
 */
public class setCacheValue implements com.z012.chengdu.sc.ui.JSBridge.WVJBWebViewClient.WVJBHandler {
	@Override
	public void request(Object data, WVJBResponseCallback callback) {
		if (StringUtil.empty(data)) {
			return;
		}
		try {
			Gson gson = new Gson();
			Bean mJson = gson.fromJson(data.toString(), Bean.class);
			AppContext.mMemoryMap.put(mJson.key, mJson.value);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	class Bean {
		public String	key;
		public String	value;
	}

}