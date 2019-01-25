package com.z012.chengdu.sc.net;

import android.annotation.SuppressLint;

import com.prj.sdk.app.AppContext;
import com.prj.sdk.net.http.OkHttpClientFactory;
import com.prj.sdk.util.SharedPreferenceUtil;
import com.z012.chengdu.sc.R;
import com.z012.chengdu.sc.constants.AppConst;
import com.z012.chengdu.sc.net.api.ApiService;
import com.z012.chengdu.sc.net.callback.ErrorActionWrapper;
import com.z012.chengdu.sc.net.callback.ResponseCallback;
import com.z012.chengdu.sc.net.entity.AllServiceListBean;
import com.z012.chengdu.sc.net.entity.BannerListBean;
import com.z012.chengdu.sc.net.entity.CertUserAuth;
import com.z012.chengdu.sc.net.entity.NewsListBean;
import com.z012.chengdu.sc.net.entity.WeatherInfoBean;
import com.z012.chengdu.sc.net.response.ResponseComm;
import com.z012.chengdu.sc.net.response.ResponseFunc;

import java.util.HashMap;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.fastjson.FastJsonConverterFactory;

public class ApiManager {
    private static final int[] SERVER_URLS = {R.string.uattest_server, R.string.offical_server};

    private static ApiService apiService;

    public static String getBaseUrl() {
        int index = 1;
        if (AppConst.ISDEVELOP) {
            index = SharedPreferenceUtil.getInstance().getInt(AppConst.APPTYPE, 1);
        }
        return AppContext.mMainContext.getString(SERVER_URLS[index]);
    }

    private static Retrofit getRetrofit() {
        return new Retrofit.Builder()
                .client(OkHttpClientFactory.newOkHttpClient())
                .baseUrl(getBaseUrl())
                .addConverterFactory(FastJsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
    }

    private static ApiService getApiService() {
        if (apiService == null) {
            apiService = getRetrofit().create(ApiService.class);
        }
        return apiService;
    }

    @SuppressLint("CheckResult")
    public static void getTicketValid(HashMap<String, Object> params, final ResponseCallback<ResponseComm<Object>> callback) {
        getApiService().getTicketValid(params)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<ResponseComm<Object>>() {
                    @Override
                    public void accept(ResponseComm<Object> o) throws Exception {
                        callback.onSuccess(o);
                    }
                });
    }

    @SuppressLint("CheckResult")
    public static void getBanner(HashMap<String, Object> params, final ResponseCallback<ResponseComm<BannerListBean>> callback) {
        getApiService().getBanner(params)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<ResponseComm<BannerListBean>>() {
                    @Override
                    public void accept(ResponseComm<BannerListBean> o) throws Exception {
                        callback.onSuccess(o);
                    }
                });
    }

    @SuppressLint("CheckResult")
    public static void getNews(HashMap<String, Object> params, final ResponseCallback<ResponseComm<NewsListBean>> callback) {
        getApiService().getNews(params)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<ResponseComm<NewsListBean>>() {
                    @Override
                    public void accept(ResponseComm<NewsListBean> o) throws Exception {
                        callback.onSuccess(o);
                    }
                });
    }

    @SuppressLint("CheckResult")
    public static void getWeather(HashMap<String, Object> params, final ResponseCallback<ResponseComm<WeatherInfoBean>> callback) {
        getApiService().getWeather(params)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<ResponseComm<WeatherInfoBean>>() {
                    @Override
                    public void accept(ResponseComm<WeatherInfoBean> o) throws Exception {
                        callback.onSuccess(o);
                    }
                }, new ErrorActionWrapper() {
                    @Override
                    protected void call(String msg) {
                        super.call(msg);
                        callback.onFail(msg);
                    }
                });
    }

    @SuppressLint("CheckResult")
    public static void getAllServices(HashMap<String, Object> params, final ResponseCallback<AllServiceListBean> callback) {
        getApiService().getAllServices(params)
                .map(new ResponseFunc<AllServiceListBean>())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<AllServiceListBean>() {
                    @Override
                    public void accept(AllServiceListBean o) throws Exception {
                        callback.onSuccess(o);
                    }
                }, new ErrorActionWrapper() {
                    @Override
                    protected void call(String msg) {
                        super.call(msg);
                        callback.onFail(msg);
                    }
                });
    }

    @SuppressLint("CheckResult")
    public static void getCertResultByUID(HashMap<String, Object> params, final ResponseCallback<ResponseComm<CertUserAuth>> callback) {
        getApiService().getCertResultByUID(params)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<ResponseComm<CertUserAuth>>() {
                    @Override
                    public void accept(ResponseComm<CertUserAuth> o) throws Exception {
                        callback.onSuccess(o);
                    }
                });
    }

    @SuppressLint("CheckResult")
    public static void getFeedBack(HashMap<String, Object> params, final ResponseCallback<Object> callback) {
        getApiService().getFeedBack(params)
                .map(new ResponseFunc<>())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) throws Exception {
                        callback.onSuccess(o);
                    }
                }, new ErrorActionWrapper() {
                    @Override
                    protected void call(String msg) {
                        super.call(msg);
                        callback.onFail(msg);
                    }
                });
    }
}
