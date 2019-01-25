package com.prj.sdk.net.http;

import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * http请求封装类 增加代理处理 URL验证 超时控制等
 * 
 * 
 */
public class HttpHelper {
	private static final String	TAG	= HttpHelper.class.getName();

	private Request request;
    private Response response;

	public HttpHelper() {
	}

	public byte[] executeHttpRequest(String url, String httpType, Object mEntity, boolean isForm) {
		ResponseBody mResponseBody = null;
		try {
			Response    response = getResponse(url, httpType, mEntity, isForm);
			mResponseBody = response.isSuccessful() ? response.body() : null;
			return mResponseBody != null ? mResponseBody.bytes() : null;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(mResponseBody != null) {
					mResponseBody.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * 执行 POST 请求，POST 请求的服务器响应
	 */
	public Response getResponse(String url, String httpType, Object mEntity, boolean isForm) {
		try {
			String mJson = (String) mEntity;
			request = OkHttpClientUtil.getInstance().buildPostRequest(url, mJson);
			response = OkHttpClientUtil.getInstance().sync(request);
			return response;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
