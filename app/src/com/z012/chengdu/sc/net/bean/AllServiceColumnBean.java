package com.z012.chengdu.sc.net.bean;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.List;

/**
 * 所有栏目以及所属的所有服务
 * 
 * @author LiaoBo
 */
public class AllServiceColumnBean implements Serializable, Comparable<AllServiceColumnBean> {
	public String			id;			// 56,
	public String			pid;			// 55,
	public String			catalogname;	// 生活账单
	public String			catalogurls;	// null,
	public String			imgurls1;		// null,
	public String			imgurls2;		// null,
	public String			catalogdesc;	// null,
	public String			inserttime;	// 1456110980000,
	public String			insertuser;	// wangxd
	public String			updatetime;	// 1456110980000,
	public String			updateuser;	// wangxd
	public int				orderid;		// 1,
	public String			calltype;		// 1,
	public String			status;		// 1,
	public String			extend1;		// null,
	public String			extend2;		// null,
	public List<AppList>	applist;		// 应用列表

	@Override
	public int compareTo(@NonNull AllServiceColumnBean o) {
		if (orderid > o.orderid) {
			return 1;
		} else if (orderid < o.orderid){
			return -1;
		} else {
			return 0;
		}
	}

	public class AppList {
		public String	id;			// d9d736376de646cd92d3f0b4dceaa9b2 ,
		public String	appname;		// 水电气
		public String	appdesc;		// 水电气
		public String	appurls;		// http://uat.zaichengdu.com/cd_portal/public/sdqjf/service/sdqjf.jsp
		public String	imgurls;		//
		public String	config;		// {\"isLogin\":false,\"isRealName\":false}",
		public String	inserttime;	// 1454383931000,
		public String	insertuser;	// wangxd
		public String	updatetime;	// 1454383931000,
		public String	updateuser;	// wangxd
		public String	versionid;		// 1,
		public String	status;		// 1,
		public String	isextendlink;	// 0
		public String	calltype;		// 1
	}
}
