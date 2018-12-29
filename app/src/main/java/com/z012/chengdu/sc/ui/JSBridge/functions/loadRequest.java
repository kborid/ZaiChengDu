package com.z012.chengdu.sc.ui.JSBridge.functions;

import com.alibaba.fastjson.JSON;
import com.prj.sdk.net.bean.ResponseData;
import com.prj.sdk.net.data.DataCallback;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.util.StringUtil;
import com.z012.chengdu.sc.ui.JSBridge.WVJBWebViewClient.WVJBResponseCallback;

/**
 * 2.8. 加载一个request
 * 
 * @author LiaoBo
 * 
 */
public class loadRequest implements com.z012.chengdu.sc.ui.JSBridge.WVJBWebViewClient.WVJBHandler {
	@Override
	public void request(Object data, final WVJBResponseCallback callback) {
		if (callback == null) {
			return;
		}
		if (StringUtil.empty(data)) {
			return;
		}
		// 解析请求参数
		Bean bean = JSON.parseObject(data.toString(), Bean.class);
		// 封装请求
		ResponseData request = new ResponseData();
		request.path = bean.url;
		request.type = bean.type;
		request.data = bean.params;
		request.isLocal = true;

		DataLoader.getInstance().loadData(new DataCallback() {

			@Override
			public void preExecute(ResponseData request) {

			}

			@Override
			public void notifyMessage(final ResponseData request, final ResponseData response) throws Exception {
				// JSONObject mJson = JSON.parseObject(response.body.toString());
				callback.callback(response.body.toString());
			}

			@Override
			public void notifyError(ResponseData request, ResponseData response, Exception e) {
				callback.callback(response.body.toString());
			}
		}, request);
	}

	class Bean {
		public String	url;
		public String	type;
		public String	params;
	}

}