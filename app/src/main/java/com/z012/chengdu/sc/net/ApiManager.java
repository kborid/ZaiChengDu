package com.z012.chengdu.sc.net;

import com.prj.sdk.app.AppContext;
import com.prj.sdk.net.http.OkHttpClientFactory;
import com.prj.sdk.util.SharedPreferenceUtil;
import com.z012.chengdu.sc.R;
import com.z012.chengdu.sc.constants.AppConst;
import com.z012.chengdu.sc.net.api.ApiService;
import com.z012.chengdu.sc.net.entity.AllServiceListBean;
import com.z012.chengdu.sc.net.entity.BannerListBean;
import com.z012.chengdu.sc.net.entity.CertUserAuth;
import com.z012.chengdu.sc.net.entity.NewsListBean;
import com.z012.chengdu.sc.net.entity.WeatherInfoBean;
import com.z012.chengdu.sc.net.response.ResponseComm;
import com.z012.chengdu.sc.net.response.ResponseFunc;

import java.util.HashMap;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.fastjson.FastJsonConverterFactory;

public class ApiManager {
    private static final int[] SERVER_URLS = {R.string.uattest_server, R.string.offical_server, R.string.local_server};

    private static ApiService apiService;

    public static String getBaseUrl() {
        int index = 1;
        if (AppConst.ISDEVELOP) {
            index = SharedPreferenceUtil.getInstance().getInt(AppConst.APPTYPE, 0);
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

    public static void getTicketValid(HashMap<String, Object> params, Observer<ResponseComm<Object>> observer) {
        getApiService().getTicketValid(params)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    public static void getBanner(HashMap<String, Object> params, Observer<ResponseComm<BannerListBean>> observer) {
        getApiService().getBanner(params)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    public static void getNews(HashMap<String, Object> params, Observer<ResponseComm<NewsListBean>> observer) {
        getApiService().getNews(params)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    public static void getWeather(HashMap<String, Object> params, Observer<ResponseComm<WeatherInfoBean>> observer) {
        getApiService().getWeather(params)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    public static void getAllServices(HashMap<String, Object> params, Observer<AllServiceListBean> observer) {
        getApiService().getAllServices(params)
                .map(new ResponseFunc<AllServiceListBean>())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    public static void getCertResultByUID(HashMap<String, Object> params, Observer<ResponseComm<CertUserAuth>> observer) {
        getApiService().getCertResultByUID(params)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    public static void getFeedBack(HashMap<String, Object> params, Observer<Object> observer) {
        getApiService().getFeedBack(params)
                .map(new ResponseFunc<>())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }
}
