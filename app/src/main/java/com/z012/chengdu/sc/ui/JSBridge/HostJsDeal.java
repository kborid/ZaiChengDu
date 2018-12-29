package com.z012.chengdu.sc.ui.JSBridge;

import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;

import com.prj.sdk.app.AppContext;
import com.prj.sdk.constants.InfoType;
import com.prj.sdk.net.bean.ResponseData;
import com.prj.sdk.net.data.DataCallback;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.NetworkUtil;
import com.prj.sdk.util.Utils;
import com.prj.sdk.widget.CustomToast;
import com.z012.chengdu.sc.R;
import com.z012.chengdu.sc.broatcast.UnLoginBroadcastReceiver;

/**
 * 使用addJavascriptInterface接口处理js请求
 * 
 * @author LiaoBo
 * 
 */
public class HostJsDeal {
	private WebView				mWebView;
	private ProgressDialog		mDialog;
	private Activity			mContext;
	private AlertDialog.Builder	builder;

	public HostJsDeal(WebView webview, Activity context) {
		this.mWebView = webview;
		this.mContext = context;
	}

	/**
	 * 打开登录页面
	 */
	@JavascriptInterface
	public void openLogin() {
		mContext.sendBroadcast(new Intent(UnLoginBroadcastReceiver.ACTION_NAME));
	}

	/**
	 * 网络是否可用
	 * 
	 * @return
	 */
	@JavascriptInterface
	public boolean isNetworkAvailable() {
		return NetworkUtil.isNetworkAvailable();
	}
	
	/**
	 * 显示对话框
	 * 
	 * @param title
	 * @param tip
	 */
	@JavascriptInterface
	public void showAlertDialog(String title, String tip) {
		if (builder == null) {
			builder = new AlertDialog.Builder(mContext);
			builder.setInverseBackgroundForced(true);
			builder.setMessage(tip);
			builder.setTitle(title);
			builder.setPositiveButton(mContext.getString(R.string.alert_ok), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			builder.create();
		}
		builder.show();
	}

	/**
	 * 跳转到当前页面的下个页面
	 * 
	 * @param path
	 */
	@JavascriptInterface
	public void showNext(final String path) {
		mWebView.post(new Runnable() {

			@Override
			public void run() {
				URL absoluteUrl, parseUrl = null;
				try {
					absoluteUrl = new URL(mWebView.getUrl());
					parseUrl = new URL(absoluteUrl, path);
				} catch (MalformedURLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				mWebView.loadUrl(parseUrl.toString());
			}
		});
	}

	/**
	 * 返回上个页面
	 */
	@JavascriptInterface
	public void showBack() {
		mWebView.post(new Runnable() {
			@Override
			public void run() {
				if (mWebView.canGoBack()) {
					mWebView.goBack();
				} else {
					mContext.finish();
				}
			}
		});

	}
	@JavascriptInterface
	public void showToast(String str) {
		if (str != null) {
			CustomToast.show(str, 0);
		}
	}

	/**
	 * 显示log
	 * 
	 * @param str
	 */
	@JavascriptInterface
	public void showJsLog(String str) {
		if (str != null) {
			LogUtil.d("====js日志=============>>", str);
		}
	}

	/**
	 * 向内存中保持值
	 * 
	 * @param key
	 * @param value
	 */
	@JavascriptInterface
	public void setMemoryValue(String key, String value) {
		if (AppContext.mMemoryMap != null) {
			AppContext.mMemoryMap.put(key, value);
		} else {
			AppContext.mMemoryMap = new HashMap<String, Object>();
		}
	}

	/**
	 * 取出内存中的值
	 * 
	 * @param key
	 * @return
	 */
	@JavascriptInterface
	public String getMemoryValue(String key) {
		if (AppContext.mMemoryMap != null) {
			return (String) AppContext.mMemoryMap.get(key);
		} else {
			return "";
		}

	}

	// public void loadData(String url, String type, String content, String tip) {
	// LogUtil.d("=========>>>", content + " " + url);
	// ResponseData data = new ResponseData();
	// data.path = url;
	// data.type = InfoType.POST_REQUEST.toString();
	// data.data = content;
	// if (!isProgressShowing())
	// showProgressDialog(tip, true);
	// requestID = DataLoader.getInstance().loadData(HtmlActivity.this, data);
	// }

	/**
	 * 异步加载数据
	 * 
	 * @param webView
	 * @param url
	 * @param type
	 * @param prams
	 * @param tip
	 */
	@JavascriptInterface
	public void loadData(int flag, String url, String prams, final String tip) {
		
		ResponseData data = new ResponseData();
		data.path = url;
		data.type = InfoType.POST_REQUEST.toString();
		data.data = prams;
		data.flag = flag;
		data.isLocal = true;
		if (mDialog == null) {
			mDialog = new ProgressDialog(mWebView.getContext());
			// mDialog.setTitle("提示");
			mDialog.setMessage("正在加载，请稍后...");
			// mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			mDialog.setCancelable(true);
		}
		mDialog.show();

		DataLoader.getInstance().loadData(new DataCallback() {

			@Override
			public void preExecute(ResponseData request) {
				// TODO Auto-generated method stub

			}

			@Override
			public void notifyMessage(final ResponseData request, final ResponseData response) throws Exception {
				// JSONObject mJson = JSON.parseObject(response.body.toString());
				mWebView.post(new Runnable() {
					@Override
					public void run() {
						StringBuilder sb = new StringBuilder();
//						sb.append("javascript:updateHtml('").append(response.data).append("',").append(request.flag).append(")");
						sb.append("javascript:").append(tip).append("'").append(response.data).append("',").append(request.flag).append(")");
						mWebView.loadUrl(sb.toString());
						if (mDialog != null && mDialog.isShowing()) {
							mDialog.dismiss();
						}
					}
				});

			}

			@Override
			public void notifyError(ResponseData request, ResponseData response, Exception e) {
				String message;
				if (mDialog != null && mDialog.isShowing()) {
					mDialog.dismiss();
				}
				if (e != null && e instanceof ConnectException) {
					message = mWebView.getContext().getString(R.string.dialog_tip_net_error);
				} else {
					message = mWebView.getContext().getString(R.string.dialog_tip_null_error);
				}
				CustomToast.show(message, Toast.LENGTH_LONG);
			}
		}, data);
	}

	/**
	 * 调用外部浏览器
	 * 
	 * @param webView
	 * @param url
	 */
	@JavascriptInterface
	public void showExternal(WebView webView, String url) {
		Utils.startWebView(webView.getContext(), url);
	}

}
