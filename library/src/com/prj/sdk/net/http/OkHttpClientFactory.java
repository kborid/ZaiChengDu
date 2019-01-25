package com.prj.sdk.net.http;

import com.orhanobut.logger.Logger;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * 获取okHttpClient工厂
 */

public class OkHttpClientFactory {

    private static final String TAG = OkHttpClientFactory.class.getSimpleName();

    private static final String DEFAULT_AGENT = "Mozilla/5.0 (Linux;Android "
            + android.os.Build.VERSION.RELEASE + "; " + android.os.Build.MODEL
            + ") AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/45.0.2454.95 Mobile Safari/537.36";

    public static OkHttpClient newOkHttpClient() {
        class HttpLogger implements HttpLoggingInterceptor.Logger {
            private StringBuilder mMessage = new StringBuilder();

            @Override
            public void log(String message) {
                // 请求或者响应开始
                if (message.startsWith("--> POST") || message.startsWith("--> GET")) {
                    mMessage.setLength(0);
                }
                // 以{}或者[]形式的说明是响应结果的json数据，需要进行格式化
                if ((message.startsWith("{") && message.endsWith("}"))
                        || (message.startsWith("[") && message.endsWith("]"))) {
//                    message = JsonUtil.formatJson(JsonUtil.decodeUnicode(message));
                }
                mMessage.append(message.concat("\n"));
                // 请求或者响应结束，打印整条日志
                if (message.startsWith("<-- END HTTP")) {
                    Logger.t(TAG).d(mMessage.toString());
                }
            }
        }

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor(new HttpLogger());
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        return new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .addInterceptor(interceptor)
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        try {
                            Request request = chain.request()
                                    .newBuilder()
                                    .addHeader("Content-Type", "application/json")
                                    .addHeader("User-Agent", DEFAULT_AGENT)
                                    .build();
                            return chain.proceed(request);
                        } catch (Exception e) {
                            e.printStackTrace();
                            return null;
                        }
                    }
                })
                .build();
    }
}
