package com.z012.chengdu.sc.ui.widge.maqueue;

import com.z012.chengdu.sc.net.bean.NewsBean;

public class UPMarqueeBean {
    private String id;
    private String text;
    private String url;

    public UPMarqueeBean(String id, String text, String url) {
        this.id = id;
        this.text = text;
        this.url = url;
    }

    public UPMarqueeBean(NewsBean bean) {
        this.id = bean.id;
        this.text = bean.text;
        this.url = bean.targeturl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
