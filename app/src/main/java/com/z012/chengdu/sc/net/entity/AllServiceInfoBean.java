package com.z012.chengdu.sc.net.entity;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.List;

/**
 * 所有栏目以及所属的所有服务
 *
 * @author kborid
 */
public class AllServiceInfoBean implements Serializable, Comparable<AllServiceInfoBean> {
    private String id;            // 56,
    private String pid;            // 55,
    private String catalogname;    // 生活账单
    private String catalogdesc;    // null,
    private String inserttime;    // 1456110980000,
    private String insertuser;    // wangxd
    private String updatetime;    // 1456110980000,
    private String updateuser;    // wangxd
    private int orderid;        // 1,
    private String calltype;        // 1,
    private String status;        // 1,
    private String imgurls1;        // null,
    private String imgurls2;        // null,
    private List<AppList> applist;        // 应用列表

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getCatalogname() {
        return catalogname;
    }

    public void setCatalogname(String catalogname) {
        this.catalogname = catalogname;
    }

    public String getCatalogdesc() {
        return catalogdesc;
    }

    public void setCatalogdesc(String catalogdesc) {
        this.catalogdesc = catalogdesc;
    }

    public String getInserttime() {
        return inserttime;
    }

    public void setInserttime(String inserttime) {
        this.inserttime = inserttime;
    }

    public String getInsertuser() {
        return insertuser;
    }

    public void setInsertuser(String insertuser) {
        this.insertuser = insertuser;
    }

    public String getUpdatetime() {
        return updatetime;
    }

    public void setUpdatetime(String updatetime) {
        this.updatetime = updatetime;
    }

    public String getUpdateuser() {
        return updateuser;
    }

    public void setUpdateuser(String updateuser) {
        this.updateuser = updateuser;
    }

    public int getOrderid() {
        return orderid;
    }

    public void setOrderid(int orderid) {
        this.orderid = orderid;
    }

    public String getCalltype() {
        return calltype;
    }

    public void setCalltype(String calltype) {
        this.calltype = calltype;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImgurls1() {
        return imgurls1;
    }

    public void setImgurls1(String imgurls1) {
        this.imgurls1 = imgurls1;
    }

    public String getImgurls2() {
        return imgurls2;
    }

    public void setImgurls2(String imgurls2) {
        this.imgurls2 = imgurls2;
    }

    public List<AppList> getApplist() {
        return applist;
    }

    public void setApplist(List<AppList> applist) {
        this.applist = applist;
    }

    @Override
    public int compareTo(@NonNull AllServiceInfoBean o) {
        if (orderid > o.orderid) {
            return 1;
        } else if (orderid < o.orderid) {
            return -1;
        } else {
            return 0;
        }
    }

    public static class AppList implements Serializable {
        private String id;              // d9d736376de646cd92d3f0b4dceaa9b2 ,
        private String appname;         // 水电气
        private String appdesc;         // 水电气
        private String appurls;         // http://uat.zaichengdu.com/cd_portal/public/sdqjf/service/sdqjf.jsp
        private String imgurls;         //
        private String config;          // {\"isLogin\":false,\"isRealName\":false}",
        private String inserttime;      // 1454383931000,
        private String insertuser;      // wangxd
        private String updatetime;      // 1454383931000,
        private String updateuser;      // wangxd
        private String versionid;       // 1,
        private String status;          // 1,
        private String isextendlink;    // 0
        private String calltype;        // 1

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getAppname() {
            return appname;
        }

        public void setAppname(String appname) {
            this.appname = appname;
        }

        public String getAppdesc() {
            return appdesc;
        }

        public void setAppdesc(String appdesc) {
            this.appdesc = appdesc;
        }

        public String getAppurls() {
            return appurls;
        }

        public void setAppurls(String appurls) {
            this.appurls = appurls;
        }

        public String getImgurls() {
            return imgurls;
        }

        public void setImgurls(String imgurls) {
            this.imgurls = imgurls;
        }

        public String getConfig() {
            return config;
        }

        public void setConfig(String config) {
            this.config = config;
        }

        public String getInserttime() {
            return inserttime;
        }

        public void setInserttime(String inserttime) {
            this.inserttime = inserttime;
        }

        public String getInsertuser() {
            return insertuser;
        }

        public void setInsertuser(String insertuser) {
            this.insertuser = insertuser;
        }

        public String getUpdatetime() {
            return updatetime;
        }

        public void setUpdatetime(String updatetime) {
            this.updatetime = updatetime;
        }

        public String getUpdateuser() {
            return updateuser;
        }

        public void setUpdateuser(String updateuser) {
            this.updateuser = updateuser;
        }

        public String getVersionid() {
            return versionid;
        }

        public void setVersionid(String versionid) {
            this.versionid = versionid;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getIsextendlink() {
            return isextendlink;
        }

        public void setIsextendlink(String isextendlink) {
            this.isextendlink = isextendlink;
        }

        public String getCalltype() {
            return calltype;
        }

        public void setCalltype(String calltype) {
            this.calltype = calltype;
        }
    }
}
