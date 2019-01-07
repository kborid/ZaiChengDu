package com.prj.sdk.net.http;

import com.alibaba.fastjson.JSONObject;
import com.prj.sdk.constants.InfoType;
import com.prj.sdk.util.StringUtil;

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
			if(mEntity instanceof JSONObject && (InfoType.GET_REQUEST.toString().equals(httpType) || InfoType.DELETE_REQUEST.toString().equals(httpType))) {
				JSONObject mJson = (JSONObject)mEntity;
				StringBuffer params = new StringBuffer();				
				for(String key : mJson.keySet()) {
					params.append(key).append("=").append(mJson.getString(key) != null ? mJson.getString(key) : "").append("&");
				}	
			    if(StringUtil.notEmpty(url)) {
			    	if(url.contains("?")) {
			    		if(url.endsWith("&")) {
			    			url += params.toString();
			    		} else {
			    			url += "&" +params.toString();
			    		}
			    	} else {
			    		url += "?" + params.toString();
			    	}
			    }
			}
			
			if (InfoType.GET_REQUEST.toString().equals(httpType)) {
				request = OkHttpClientUtil.getInstance().buildGetRequest(url);
			} else if (InfoType.DELETE_REQUEST.toString().equals(httpType)) {
				request = OkHttpClientUtil.getInstance().buildDeleteRequest(url);
			} else if (InfoType.PUT_REQUEST.toString().equals(httpType)) {
				if (mEntity instanceof String) {
					String mJson = (String) mEntity;
					request = OkHttpClientUtil.getInstance().buildPutRequest(url, mJson);
				} else {
					if (mEntity instanceof JSONObject) {
						JSONObject mJson = (JSONObject) mEntity;
						if (isForm) {
							request = OkHttpClientUtil.getInstance().buildPutMultipartFormRequest(url, mJson);
						} else {
							request = OkHttpClientUtil.getInstance().buildPutFormRequest(url, mJson);
						}
					} else if(mEntity instanceof byte[]) {
						byte[] data = (byte[]) mEntity;
						request = OkHttpClientUtil.getInstance().buildPutRequest(url, data);
					} else if(mEntity == null) {
						request = OkHttpClientUtil.getInstance().buildPutRequest(url, new byte[]{});
					}
				}
			} else {
				if (mEntity instanceof String) {
					String mJson = (String) mEntity;
					request = OkHttpClientUtil.getInstance().buildPostRequest(url, mJson);
				} else {
					if (mEntity instanceof JSONObject) {
						JSONObject mJson = (JSONObject) mEntity;
						if (isForm) {
							request = OkHttpClientUtil.getInstance().buildPostMultipartFormRequest(url, mJson);
						} else {
							request = OkHttpClientUtil.getInstance().buildPostFormRequest(url, mJson);
						}
					} else if(mEntity instanceof byte[]) {
						byte[] data = (byte[]) mEntity;
						request = OkHttpClientUtil.getInstance().buildPostRequest(url, data);
					} else if(mEntity == null) {
						request = OkHttpClientUtil.getInstance().buildPostRequest(url, new byte[]{});
					}
				}
			}

			response = OkHttpClientUtil.getInstance().sync(request);
			return response;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void disconnect() {
		try {
			OkHttpClientUtil.getInstance().getOkHttpClient().dispatcher().cancelAll();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Request getRequest() {
		return request;
	}
	
	public Response getResponse() {
		return response;
	}
	
}
