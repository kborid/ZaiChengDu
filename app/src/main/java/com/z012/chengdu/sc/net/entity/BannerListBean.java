package com.z012.chengdu.sc.net.entity;

import java.util.List;

/**
 * 首页banner 以及城市新鲜事
 *
 * @author kborid
 */
public class BannerListBean {

    private List<BannerItemBean> datalist;
    private Object params;

    public List<BannerItemBean> getDatalist() {
        return datalist;
    }

    public void setDatalist(List<BannerItemBean> datalist) {
        this.datalist = datalist;
    }

    public Object getParams() {
        return params;
    }

    public void setParams(Object params) {
        this.params = params;
    }

    public static class BannerItemBean {
        private String insertuser;
        private String inserttime;
        private String bndesc;
        private int orderid;
        private String imgurls;
        private int id;
        private String bnname;
        private String updatetime;
        private int calltype;
        private String updateuser;
        private String linkurls;

        public String getInsertuser() {
            return insertuser;
        }

        public void setInsertuser(String insertuser) {
            this.insertuser = insertuser;
        }

        public String getInserttime() {
            return inserttime;
        }

        public void setInserttime(String inserttime) {
            this.inserttime = inserttime;
        }

        public String getBndesc() {
            return bndesc;
        }

        public void setBndesc(String bndesc) {
            this.bndesc = bndesc;
        }

        public int getOrderid() {
            return orderid;
        }

        public void setOrderid(int orderid) {
            this.orderid = orderid;
        }

        public String getImgurls() {
            return imgurls;
        }

        public void setImgurls(String imgurls) {
            this.imgurls = imgurls;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getBnname() {
            return bnname;
        }

        public void setBnname(String bnname) {
            this.bnname = bnname;
        }

        public String getUpdatetime() {
            return updatetime;
        }

        public void setUpdatetime(String updatetime) {
            this.updatetime = updatetime;
        }

        public int getCalltype() {
            return calltype;
        }

        public void setCalltype(int calltype) {
            this.calltype = calltype;
        }

        public String getUpdateuser() {
            return updateuser;
        }

        public void setUpdateuser(String updateuser) {
            this.updateuser = updateuser;
        }

        public String getLinkurls() {
            return linkurls;
        }

        public void setLinkurls(String linkurls) {
            this.linkurls = linkurls;
        }
    }
}
