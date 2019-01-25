package com.z012.chengdu.sc.entity;

import java.io.Serializable;

public class WebInfoEntity implements Serializable {
    private String title;
    private String url;
    private String id;

    public WebInfoEntity(String url) {
        this.url = url;
    }

    public WebInfoEntity(String title, String url) {
        this.title = title;
        this.url = url;
    }

    public WebInfoEntity(String id, String title, String url) {
        this.id = id;
        this.title = title;
        this.url = url;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
