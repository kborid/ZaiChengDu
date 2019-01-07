package com.prj.sdk.net.http;

import android.text.TextUtils;

import com.alibaba.fastjson.JSONObject;

import java.io.File;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.concurrent.TimeUnit;

import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * OkHttp 封装类
 *
 * @author LiaoBo
 */
public class OkHttpClientUtil {
	private static final String TAG = "OkHttpClientUtil";
	private static final int DEFAULT_READ_TIMEOUT_MILLIS = 10 * 1000; // 10s
	private static final int DEFAULT_WRITE_TIMEOUT_MILLIS = 10 * 1000; // 10s
	private static final int DEFAULT_CONNECT_TIMEOUT_MILLIS = 10 * 1000; // 10s

	private static OkHttpClientUtil mInstance;
	private OkHttpClient mOkHttpClient;

	private OkHttpClientUtil() {
		OkHttpClient.Builder builder = new OkHttpClient.Builder();
		builder.connectTimeout(DEFAULT_CONNECT_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
		builder.readTimeout(DEFAULT_READ_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
		builder.writeTimeout(DEFAULT_WRITE_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
		mOkHttpClient = builder.build();
	}

	public static OkHttpClientUtil getInstance() {
		if (mInstance == null) {
			synchronized (OkHttpClientUtil.class) {
				if (mInstance == null) {
					mInstance = new OkHttpClientUtil();
				}
			}
		}
		return mInstance;
	}

	/**
	 * 获取OkHttpClient对象
	 *
	 * @return
	 */
	public OkHttpClient getOkHttpClient() {
		return mOkHttpClient;
	}

	/**
	 * 同步请求
	 *
	 * @param request
	 * @return
	 */
	public Response sync(Request request) {
		try {
            return mOkHttpClient.newCall(request).execute();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 异步请求
	 *
	 * @param request
	 * @param responseCallback
	 */
	public void async(Request request, Callback responseCallback) {
		mOkHttpClient.newCall(request).enqueue(responseCallback);
	}

	private String guessMimeType(String path) {
		FileNameMap fileNameMap = URLConnection.getFileNameMap();
		String contentTypeFor = fileNameMap.getContentTypeFor(path);
		if (contentTypeFor == null) {
			contentTypeFor = "application/octet-stream";
		}
		return contentTypeFor;
	}

//	private Headers dealHeaders(Map<String, Object> header) {
//		Map<String, String> temp = new HashMap<String, String>();
//		if (header != null) {
//			for (String key : header.keySet()) {
//				if (TextUtils.isEmpty(key) || TextUtils.isEmpty(String.valueOf(header.get(key)))) {
//					continue;
//				}
//
//				temp.put(key, String.valueOf(header.get(key)));
//			}
//		}
//		return Headers.of(temp);
//	}

	// ============GET============
	public Request buildGetRequest(String url) {
        return new Request.Builder().url(url).get().build();
	}

	public Response get(String url) {
		Request request = buildGetRequest(url);
		return sync(request);
	}

	public void getAsyn(String url, Callback responseCallback) {
		Request request = buildGetRequest(url);
		async(request, responseCallback);
	}

	// ============POST============
	public Request buildPostRequest(String url, String mJson) {
		MediaType mMediaType = MediaType.parse("application/json; charset=utf-8");
		RequestBody requestBody = RequestBody.create(mMediaType, mJson);
        return new Request.Builder().url(url).post(requestBody).build();
	}

	public Request buildPostRequest(String url, byte[] data) {
		MediaType mMediaType = MediaType.parse("application/octet-stream; charset=utf-8");
		RequestBody requestBody = RequestBody.create(mMediaType, data);
        return new Request.Builder().url(url).post(requestBody).build();
	}

	public Request buildPostFormRequest(String url, JSONObject mJson) {
		FormBody.Builder builder = new FormBody.Builder();
		for (String key : mJson.keySet()) {
			if (TextUtils.isEmpty(key) || TextUtils.isEmpty(mJson.getString(key))) {
				continue;
			}

			String value = mJson.getString(key);
			builder.add(key, value);
		}

		RequestBody requestBody = builder.build();
		return new Request.Builder().url(url).post(requestBody).build();
	}

	public Request buildPostMultipartFormRequest(String url, JSONObject mJson) {
		MultipartBody.Builder builder = new MultipartBody.Builder()/*.type(MultipartBuilder.FORM)*/;
		for (String key : mJson.keySet()) {
			if (TextUtils.isEmpty(key)) {
				continue;
			}

			if (mJson.get(key) instanceof File) {
				File mFile = (File) mJson.get(key);
				if (!mFile.exists()) {
					continue;
				}
				RequestBody fileBody = RequestBody.create(MediaType.parse(guessMimeType(mFile.getName())), mFile);
				builder.addPart(Headers.of("Content-Disposition", "form-data; name=\"" + key + "\"; filename=\"" + mFile.getName() + "\""), fileBody);
			} else {
				String value = mJson.getString(key);
				if (TextUtils.isEmpty(value)) {
					continue;
				}

				builder.addPart(Headers.of("Content-Disposition", "form-data; name=\"" + key + "\""), RequestBody.create(null, value));
			}
		}
		RequestBody requestBody = builder.build();
		return new Request.Builder().url(url).post(requestBody).build();
	}

	public Response post(String url, String mJson) {
		Request request = buildPostRequest(url, mJson);
		return sync(request);
	}

	public Response post(String url, byte[] data) {
		Request request = buildPostRequest(url, data);
		return sync(request);
	}

	public Response post(String url, JSONObject mJson) {
		Request request = buildPostFormRequest(url, mJson);
		return sync(request);
	}

	public Response postMultipart(String url, JSONObject mJson) {
		Request request = buildPostMultipartFormRequest(url, mJson);
		return sync(request);
	}

	public void postAsyn(String url, String mJson, Callback responseCallback) {
		Request request = buildPostRequest(url, mJson);
		async(request, responseCallback);
	}

	public void postAsyn(String url, byte[] data, Callback responseCallback) {
		Request request = buildPostRequest(url, data);
		async(request, responseCallback);
	}

	public void postAsyn(String url, JSONObject mJson, Callback responseCallback) {
		Request request = buildPostFormRequest(url, mJson);
		async(request, responseCallback);
	}

	public void postMultipartAsyn(String url, JSONObject mJson, Callback responseCallback) {
		Request request = buildPostMultipartFormRequest(url, mJson);
		async(request, responseCallback);
	}

	// ============DELETE============
	public Request buildDeleteRequest(String url) {
        return new Request.Builder().url(url).delete().build();
	}

	public Response delete(String url) {
		Request request = buildDeleteRequest(url);
		return sync(request);
	}

	public void deleteAsyn(String url, Callback responseCallback) {
		Request request = buildDeleteRequest(url);
		async(request, responseCallback);
	}

	// ============PUT============
	public Request buildPutRequest(String url, String mJson) {
		MediaType mMediaType = MediaType.parse("application/json; charset=utf-8");
		RequestBody requestBody = RequestBody.create(mMediaType, mJson);
        return new Request.Builder().url(url).put(requestBody).build();
	}

	public Request buildPutRequest(String url, byte[] data) {
		MediaType mMediaType = MediaType.parse("application/octet-stream; charset=utf-8");
		RequestBody requestBody = RequestBody.create(mMediaType, data);
        return new Request.Builder().url(url).put(requestBody).build();
	}

	public Request buildPutFormRequest(String url, JSONObject mJson) {
		FormBody.Builder builder = new FormBody.Builder();
		for (String key : mJson.keySet()) {
			if (TextUtils.isEmpty(key) || TextUtils.isEmpty(mJson.getString(key))) {
				continue;
			}

			String value = mJson.getString(key);
			builder.add(key, value);
		}

		RequestBody requestBody = builder.build();
		return new Request.Builder().url(url).put(requestBody).build();
	}

	public Request buildPutMultipartFormRequest(String url, JSONObject mJson) {
		MultipartBody.Builder builder = new MultipartBody.Builder();/*.type(MultipartBody.Builder.FORM);*/
		for (String key : mJson.keySet()) {
			if (TextUtils.isEmpty(key)) {
				continue;
			}

			if (mJson.get(key) instanceof File) {
				File mFile = (File) mJson.get(key);
				if (!mFile.exists()) {
					continue;
				}
				RequestBody fileBody = RequestBody.create(MediaType.parse(guessMimeType(mFile.getName())), mFile);
				builder.addPart(Headers.of("Content-Disposition", "form-data; name=\"" + key + "\"; filename=\"" + mFile.getName() + "\""), fileBody);
			} else {
				String value = mJson.getString(key);
				if (TextUtils.isEmpty(value)) {
					continue;
				}

				builder.addPart(Headers.of("Content-Disposition", "form-data; name=\"" + key + "\""), RequestBody.create(null, value));
			}
		}
		RequestBody requestBody = builder.build();
		return new Request.Builder().url(url).put(requestBody).build();
	}

	public Response put(String url, String mJson) {
		Request request = buildPutRequest(url, mJson);
		return sync(request);
	}

	public Response put(String url, byte[] data) {
		Request request = buildPutRequest(url, data);
		return sync(request);
	}

	public Response put(String url, JSONObject mJson) {
		Request request = buildPutFormRequest(url, mJson);
		return sync(request);
	}

	public Response putMultipart(String url, JSONObject mJson) {
		Request request = buildPutMultipartFormRequest(url, mJson);
		return sync(request);
	}

	public void putAsyn(String url, String mJson, Callback responseCallback) {
		Request request = buildPutRequest(url, mJson);
		async(request, responseCallback);
	}

	public void putAsyn(String url, byte[] data, Callback responseCallback) {
		Request request = buildPutRequest(url, data);
		async(request, responseCallback);
	}

	public void putAsyn(String url, JSONObject mJson, Callback responseCallback) {
		Request request = buildPutFormRequest(url, mJson);
		async(request, responseCallback);
	}

	public void putMultipartAsyn(String url, JSONObject mJson, Callback responseCallback) {
		Request request = buildPutMultipartFormRequest(url, mJson);
		async(request, responseCallback);
	}
}