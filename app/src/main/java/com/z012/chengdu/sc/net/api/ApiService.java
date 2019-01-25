package com.z012.chengdu.sc.net.api;

import com.z012.chengdu.sc.net.entity.AllServiceListBean;
import com.z012.chengdu.sc.net.entity.BannerListBean;
import com.z012.chengdu.sc.net.entity.CertUserAuth;
import com.z012.chengdu.sc.net.entity.NewsListBean;
import com.z012.chengdu.sc.net.entity.WeatherInfoBean;
import com.z012.chengdu.sc.net.response.ResponseComm;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {

    @POST("/center_weatherserver/service/CW0101")
    Observable<ResponseComm<WeatherInfoBean>> getWeather(@Body Object params);

    @POST("/cq_portal/service/CW1014")
    Observable<ResponseComm<Object>> getTicketValid(@Body Object params);

    @POST("/cq_portal/service/CW1006")
    Observable<ResponseComm<BannerListBean>> getBanner(@Body Object params);

    @POST("/cq_portal/service/CW0005")
    Observable<ResponseComm<NewsListBean>> getNews(@Body Object params);

    @POST("/cq_portal/service/CW1012")
    Observable<ResponseComm<AllServiceListBean>> getAllServices(@Body Object params);

    @POST("/cq_portal/service/PA10002")
    Observable<ResponseComm<CertUserAuth>> getCertResultByUID(@Body Object params);

    @POST("/cq_portal/service/CW9023")
    Observable<ResponseComm<Object>> getFeedBack(@Body Object params);
}
