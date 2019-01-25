package com.z012.chengdu.sc.net.entity;

import java.util.List;

public class NewsListBean {
    private List<NewsItemBean> hotnewstodaylist;

    public List<NewsItemBean> getHotnewstodaylist() {
        return hotnewstodaylist;
    }

    public void setHotnewstodaylist(List<NewsItemBean> hotnewstodaylist) {
        this.hotnewstodaylist = hotnewstodaylist;
    }

    public static class NewsItemBean {
        private String id;
        private String text;
        private String targeturl;
        private String createtime;
        private String creater;
        private String updatetime;
        private String updater;
        private String status;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getTargeturl() {
            return targeturl;
        }

        public void setTargeturl(String targeturl) {
            this.targeturl = targeturl;
        }

        public String getCreatetime() {
            return createtime;
        }

        public void setCreatetime(String createtime) {
            this.createtime = createtime;
        }

        public String getCreater() {
            return creater;
        }

        public void setCreater(String creater) {
            this.creater = creater;
        }

        public String getUpdatetime() {
            return updatetime;
        }

        public void setUpdatetime(String updatetime) {
            this.updatetime = updatetime;
        }

        public String getUpdater() {
            return updater;
        }

        public void setUpdater(String updater) {
            this.updater = updater;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}
