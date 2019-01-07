package com.prj.sdk.net.http;

import java.io.InputStream;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;

import com.prj.sdk.app.AppContext;
import com.prj.sdk.constants.InfoType;
import com.prj.sdk.util.LogUtil;

/**
 * http请求封装类 增加代理处理 URL验证 超时控制等
 * 
 * @author Liao
 * 
 */
public class HttpClientExecutor {
	private static final String			TAG					= HttpClientExecutor.class.getName();

	private static final int			TIMEOUT				= 10000;
	private static final int			TIMEOUT_SOCKET		= 1000 * 25;

	// 移动 联通wap10.0.0.172
	public static final int				TYPE_CM_CU_WAP		= 0;
	// 电信wap 10.0.0.200
	public static final int				TYPE_CT_WAP			= 1;
	// 电信,移动,联通,wifi 等net网络
	public static final int				TYPE_OTHER_NET		= 2;
	public static Uri					PREFERRED_APN_URI	= Uri.parse("content://telephony/carriers/preferapn");

	/************ Singleton ***********/
	private static HttpClientExecutor	instance			= new HttpClientExecutor();

	private SSLSocketFactory			socketFactory;

	private HttpClientExecutor() {

	}

	public static HttpClientExecutor getInstance() {
		return instance;
	}

	private SSLSocketFactory getSSLSocketFactory() throws Exception {
		if (socketFactory == null) {
			// 单项认证
			KeyStore trustStore = KeyStore.getInstance("BKS", "BC");
			InputStream tsIn = AppContext.mMainContext.getAssets().open("ubang_custom_trust_debug.keystore");
			try {
				trustStore.load(tsIn, "mdm@soarsky".toCharArray());
			} finally {
				try {
					tsIn.close();
				} catch (Exception ignore) {
				}
			}
			socketFactory = new SSLSocketFactory(trustStore);
		}
		return socketFactory;
	}

	private HttpClient newHttpClient() throws Exception {
		HttpParams params = new BasicHttpParams();
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
		HttpProtocolParams.setUseExpectContinue(params, true);

		// 自定义三个timeout参数
		ConnManagerParams.setTimeout(params, TIMEOUT);
		HttpConnectionParams.setConnectionTimeout(params, TIMEOUT);
		HttpConnectionParams.setSoTimeout(params, TIMEOUT_SOCKET);

		// 双向认证
		/*
		 * KeyStore keyStore = KeyStore.getInstance("PKCS12"); KeyStore trustStore = KeyStore.getInstance("BKS", "BC"); InputStream ksIn =
		 * AppContext.mMainContext.getAssets().open("ubang_custom.p12"); InputStream tsIn =
		 * AppContext.mMainContext.getAssets().open("ubang_custom_trust.keystore"); try { keyStore.load(ksIn, "654321".toCharArray()); trustStore.load(tsIn,
		 * "654321".toCharArray()); } finally { try { ksIn.close(); } catch (Exception ignore) {} try { tsIn.close(); } catch (Exception ignore) {} }
		 * SSLSocketFactory socketFactory = new SSLSocketFactory(keyStore, "654321", trustStore);
		 */

		SchemeRegistry schReg = new SchemeRegistry();
		schReg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		// schReg.register(new Scheme("https", getSSLSocketFactory(), NetURL.REST_PORT));

		// 创建线程安全的 ClientConnectionManager
		ThreadSafeClientConnManager connectionManager = new ThreadSafeClientConnManager(params, schReg);

		switch (checkUserProxy()) {
		case TYPE_CM_CU_WAP: {
			// 通过代理解决中国移动联通GPRS中wap无法访问的问题
			HttpHost proxy = new HttpHost("10.0.0.172", 80, "http");
			params.setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
		}
			break;
		case TYPE_CT_WAP: {
			// 通过代理解决中国移动联通GPRS中wap无法访问的问题
			HttpHost proxy = new HttpHost("10.0.0.200", 80, "http");
			params.setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
		}
			break;
		}

		HttpClient mHttpClient = new DefaultHttpClient(connectionManager, params);
		return mHttpClient;
	}

	/***
	 * 判断Network具体类型（联通移动wap，电信wap，其他net）
	 * 
	 * */
	public static int checkUserProxy() {
		try {
			final ConnectivityManager connectivityManager = (ConnectivityManager) AppContext.mMainContext.getSystemService(Context.CONNECTIVITY_SERVICE);
			final NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
			if (networkInfo != null && networkInfo.isAvailable()) {
				int netType = networkInfo.getType();
				if (netType == ConnectivityManager.TYPE_MOBILE) {
					final Cursor c = AppContext.mMainContext.getContentResolver().query(PREFERRED_APN_URI, null, null, null, null);
					if (c != null) {
						c.moveToFirst();
						final String user = c.getString(c.getColumnIndex("user"));
						if (user != null && user.startsWith("ctwap")) {
							return TYPE_CT_WAP;
						}
						c.close();
					}

					String netMode = networkInfo.getExtraInfo();
					if (netMode != null) {
						netMode = netMode.toLowerCase();
						if (netMode.equals("cmwap") || netMode.equals("3gwap") || netMode.equals("uniwap")) {
							return TYPE_CM_CU_WAP;
						}
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return TYPE_OTHER_NET;
		}
		return TYPE_OTHER_NET;
	}

	public HttpRequestBase getHttpRequestBase(String url, String httpType, Object mEntity) {
		HttpRequestBase httpBase = null;
		try {
			if (!checkUrl(url))
				return null;

			if ((InfoType.GET_REQUEST.toString().equals(httpType) || InfoType.DELETE_REQUEST.toString().equals(httpType)) && mEntity != null
					&& mEntity instanceof List<?>) {
				List<NameValuePair> mList = (List<NameValuePair>) mEntity;
				url += "?";
				for (NameValuePair mPair : mList) {
					url += mPair.getName() + "=" + mPair.getValue() + "&";
				}

				if (LogUtil.isDebug()) {
					LogUtil.d(TAG, "start request:" + "new path:" + url);
				}
			}

			if (InfoType.POST_REQUEST.toString().equals(httpType)) {
				httpBase = new HttpPost(url);
			} else if (InfoType.GET_REQUEST.toString().equals(httpType)) {
				httpBase = new HttpGet(url);
			} else if (InfoType.PUT_REQUEST.toString().equals(httpType)) {
				httpBase = new HttpPut(url);
			} else if (InfoType.DELETE_REQUEST.toString().equals(httpType)) {
				httpBase = new HttpDelete(url);
			} else {
				httpBase = new HttpPost(url);
			}
			httpBase.setHeader("accept", "*/*");
			return httpBase;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 执行 POST 请求，POST 请求的服务器响应
	 */
	public byte[] executeHttpRequest(HttpRequestBase httpBase, Map<String, Object> header, Object mEntity) {
		try {
			if (header == null) {
				header = new HashMap<String, Object>();
			}

			for (String key : header.keySet()) {
				httpBase.addHeader(key, String.valueOf(header.get(key)));
			}

			// 设置实体
			if ((httpBase instanceof HttpPost || httpBase instanceof HttpPut) && mEntity != null && mEntity instanceof List<?>) {
				((HttpEntityEnclosingRequestBase) httpBase).setEntity(new UrlEncodedFormEntity((List<NameValuePair>) mEntity, "UTF-8"));
			} else if((httpBase instanceof HttpPost || httpBase instanceof HttpPut) && mEntity != null && mEntity instanceof String) {
				((HttpEntityEnclosingRequestBase) httpBase).setEntity(new StringEntity((String) mEntity));
			}
			// 发送请求
			HttpResponse httpResponse = newHttpClient().execute(httpBase);

			// 处理返回消息
			StatusLine statusLine = null;
			if (httpResponse == null || (statusLine = httpResponse.getStatusLine()) == null)
				return null;

			int responseCode = statusLine.getStatusCode();
			if (responseCode == HttpStatus.SC_OK) {
				return EntityUtils.toByteArray(httpResponse.getEntity());
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (httpBase != null) {
				httpBase.abort();
			}
		}

		return null;
	}

	/**
	 * 执行 GET 请求，GET 请求的服务器响应为指定资源类型
	 */
	public byte[] executeGet(String url) {
		byte[] resData = null;
		HttpRequestBase httpBase = null;
		try {
			httpBase = getHttpRequestBase(url, InfoType.GET_REQUEST.toString(), null);
			// 发送请求
			HttpResponse httpResponse = newHttpClient().execute(httpBase);

			// 处理返回消息
			StatusLine statusLine = null;
			if (httpResponse == null || (statusLine = httpResponse.getStatusLine()) == null)
				return null;

			int responseCode = statusLine.getStatusCode();

			if (responseCode == HttpStatus.SC_OK) {
				resData = EntityUtils.toByteArray(httpResponse.getEntity());
			} else if (responseCode == HttpStatus.SC_MOVED_PERMANENTLY || responseCode == HttpStatus.SC_MOVED_TEMPORARILY
					|| responseCode == HttpStatus.SC_SEE_OTHER || responseCode == HttpStatus.SC_TEMPORARY_REDIRECT) {
				String newUrl = new String(httpResponse.getFirstHeader("location").getValue().getBytes("ISO-8859-1"), "UTF-8");
				executeGet(newUrl);
			}

		} catch (Exception e) {
			e.printStackTrace();
			if (httpBase != null) {
				httpBase.abort();
			}
		}
		return resData;
	}

	/**
	 * 获取HttpResponse
	 * 
	 * @param httpBase
	 */
	public HttpResponse getHttpResponse(HttpRequestBase httpBase) throws Exception {
		HttpResponse httpResponse = newHttpClient().execute(httpBase);
		return httpResponse;
	}

	/**
	 * 验证url的合法性
	 * 
	 * @param url
	 * @return{
	 * 
	 */
	private static boolean checkUrl(String url) {
		return url.matches("^[a-zA-z]+://[^\\s]*$");
	}
}
