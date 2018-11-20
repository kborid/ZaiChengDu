package com.z012.chengdu.sc.ui.JSBridge.functions;

import android.content.Context;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.prj.sdk.util.Utils;
import com.z012.chengdu.sc.ui.JSBridge.WVJBWebViewClient.WVJBResponseCallback;

/**
 * 2.6. 使用外部浏览器打开一个URL {‘url’:'http://www.baidu.com’}
 * 
 * @author LiaoBo
 */
public class openURL implements com.z012.chengdu.sc.ui.JSBridge.WVJBWebViewClient.WVJBHandler {
	private Context	mContext;

	/**
	 * 构造函数，获取上下文
	 * 
	 * @param context
	 */
	public openURL(Context context) {
		mContext = context;
	}

	@Override
	public void request(Object data, WVJBResponseCallback callback) {
		try {
			if (data != null) {
				JSONObject mJson = JSON.parseObject(data.toString());
				String url = mJson.getString("url");
				Utils.startWebView(mContext, url);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
