package com.z012.chengdu.sc.ui.JSBridge.functions;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.prj.sdk.util.StringUtil;
import com.z012.chengdu.sc.ui.JSBridge.WVJBWebViewClient.WVJBResponseCallback;
import com.z012.chengdu.sc.ui.activity.HtmlActivity;

/**
 * 3.4 异常处理,显示异常并且会退出整个webView
 * 
 * @author LiaoBo
 */
public class showException implements com.z012.chengdu.sc.ui.JSBridge.WVJBWebViewClient.WVJBHandler {
	private Context	mContext;
	
	public showException(Context context) {
		mContext = context;
	}

	@Override
	public void request(Object data, final WVJBResponseCallback callback) {
		try {

//			if (callback == null) {
//				return;
//			}
			if (StringUtil.empty(data)) {
				return;
			}
			// 解析请求参数
			JSONObject mJson = JSON.parseObject(data.toString());
			String exception = mJson.getString("exception");
			AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
			builder.setMessage(exception);
			OnClickListener dialogButtonOnClickListener = new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int clickedButton) {
					((HtmlActivity)mContext).finish();//退出web
//					//返回deviceId
//					JSONObject mJson = new JSONObject();
//					mJson.put("deviceId", Utils.getIMEI()+Utils.getWifiMac());
//					callback.callback(mJson.toString());
				}
			};
			builder.setPositiveButton("关闭", dialogButtonOnClickListener);
			builder.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}