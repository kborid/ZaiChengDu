package com.prj.sdk.net.http;

import okhttp3.MediaType;
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
	private static OkHttpClientUtil mInstance;
	private OkHttpClient mOkHttpClient;
    private static final String DEFAULT_AGENT = "Mozilla/5.0 (Linux;"
            + "Android " + android.os.Build.VERSION.RELEASE + "; "
            + android.os.Build.MODEL
            + ") AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/45.0.2454.95 Mobile Safari/537.36";


	private OkHttpClientUtil() {
		mOkHttpClient = OkHttpClientFactory.newOkHttpClient();
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

	public Response sync(Request request) {
		try {
            return mOkHttpClient.newCall(request).execute();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public Request buildPostRequest(String url, String mJson) {
		MediaType mMediaType = MediaType.parse("application/json; charset=utf-8");
		RequestBody requestBody = RequestBody.create(mMediaType, mJson);
        return new Request.Builder().url(url).post(requestBody).build();
	}
}