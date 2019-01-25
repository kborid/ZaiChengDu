package com.z012.chengdu.sc.net.entity;

import java.io.Serializable;
import java.util.List;

public class WeatherInfoBean implements Serializable {

    private String pmdesc;
    private String status1;
    private String savedate_weather;
    private String temperature2;
    private String temperature1;
    private String limitnumber;
    private String pmdata;
    private String status2;
    private List<WeatherFutureInfoBean> future;

    public String getPmdesc() {
        return pmdesc;
    }

    public void setPmdesc(String pmdesc) {
        this.pmdesc = pmdesc;
    }

    public String getStatus1() {
        return status1;
    }

    public void setStatus1(String status1) {
        this.status1 = status1;
    }

    public String getSavedate_weather() {
        return savedate_weather;
    }

    public void setSavedate_weather(String savedate_weather) {
        this.savedate_weather = savedate_weather;
    }

    public String getTemperature2() {
        return temperature2;
    }

    public void setTemperature2(String temperature2) {
        this.temperature2 = temperature2;
    }

    public String getTemperature1() {
        return temperature1;
    }

    public void setTemperature1(String temperature1) {
        this.temperature1 = temperature1;
    }

    public String getLimitnumber() {
        return limitnumber;
    }

    public void setLimitnumber(String limitnumber) {
        this.limitnumber = limitnumber;
    }

    public String getPmdata() {
        return pmdata;
    }

    public void setPmdata(String pmdata) {
        this.pmdata = pmdata;
    }

    public String getStatus2() {
        return status2;
    }

    public void setStatus2(String status2) {
        this.status2 = status2;
    }

    public List<WeatherFutureInfoBean> getFuture() {
        return future;
    }

    public void setFuture(List<WeatherFutureInfoBean> future) {
        this.future = future;
    }

    @Override
    public String toString() {
        return "[" + "pmdesc = " + pmdesc + "savedate_weather = "
                + savedate_weather + "status1 = " + status1 + "status2 = "
                + status2 + "temperature1 = " + temperature1
                + "temperature2 = " + temperature2 + "pmdata = " + pmdata + "limitnumber = " + limitnumber + "]";
    }
}
