package com.z012.chengdu.sc.app;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.prj.sdk.util.SharedPreferenceUtil;
import com.prj.sdk.util.StringUtil;
import com.z012.chengdu.sc.constants.AppConst;
import com.z012.chengdu.sc.net.bean.AppListBean;
import com.z012.chengdu.sc.net.bean.PushAppBean;
import com.z012.chengdu.sc.net.bean.UserInfo;
import com.z012.chengdu.sc.net.bean.WeatherFutureInfoBean;

/**
 * 缓存全局数据
 * 
 * @author LiaoBo
 * 
 */
public class SessionContext {

	public static UserInfo				mUser;				// 用户信息
	private static List<PushAppBean>	mAppList;			// 首页推荐应用
	private static List<AppListBean>	mPushColumnBean;	// 推荐栏目及下级应用
	private static List<AppListBean>	mAllAppList;		// 所有应用
	
	private static List<WeatherFutureInfoBean> mWeatherInfo; // 未来天气信息

	private static String				mTicket;

	/**
	 * 
	 * 获取区域信息
	 * 
	 * @param i
	 *            1：id ； 2：name
	 * @return
	 */
	public static String getAreaInfo(int i) {
		String s;
		if (i == 1) {
			s = SharedPreferenceUtil.getInstance().getString("areaCode", "", true);// 默认成都 510000 510100
		} else {
			s = SharedPreferenceUtil.getInstance().getString("areaName", "", true);// 默认成都
		}

		return s;
	}

	/** 设置区域信息
	 * @param areaCode
	 * @param name
	 */
	public static void setAreaCode(String areaCode, String name) {
		SharedPreferenceUtil.getInstance().setString("areaCode", areaCode, true);
		SharedPreferenceUtil.getInstance().setString("areaName", name, true);
	}

	
	
	public static List<WeatherFutureInfoBean> getWeatherInfo() {
		if (mWeatherInfo == null) {
			mWeatherInfo = new ArrayList<WeatherFutureInfoBean>();
		}
		return mWeatherInfo;
	}

	public static void setWeatherInfo(List<WeatherFutureInfoBean> list) {
		if (mWeatherInfo == null) {
			mWeatherInfo = new ArrayList<WeatherFutureInfoBean>();
		} else {
			mWeatherInfo.clear();
		}
		if (list.size() >= 7) {
			list = list.subList(0, 7);
		}
		mWeatherInfo.addAll(list);
	}

	/**
	 * 获取推荐栏目及下级应用
	 * 
	 * @return
	 */
	public static List<AppListBean> getPushColumn() {
		if (mPushColumnBean == null) {
			mPushColumnBean = new ArrayList<AppListBean>();
		}
		return mPushColumnBean;
	}

	public static void setPushColumn(List<AppListBean> list) {
		if (mPushColumnBean == null) {
			mPushColumnBean = new ArrayList<AppListBean>();
		} else {
			mPushColumnBean.clear();
		}
		mPushColumnBean.addAll(list);
	}

	public static void addPushColumnItem(AppListBean bean) {
		if (mPushColumnBean == null) {
			mPushColumnBean = new ArrayList<AppListBean>();
		}
		mPushColumnBean.add(bean);
	}

	/**
	 * 获取应用列表
	 * 
	 * @return
	 */
	public static List<AppListBean> getAllAppList() {
		if (mAllAppList == null) {
			mAllAppList = new ArrayList<AppListBean>();
		}
		return mAllAppList;
	}

	public static void setAllAppItem(List<AppListBean> item) {
		if (mAllAppList == null) {
			mAllAppList = new ArrayList<AppListBean>();
		} else {
			mAllAppList.clear();
		}
		SessionContext.mAllAppList.addAll(item);
	}

	/**
	 * 获取首页推荐7个应用
	 * 
	 * @return
	 */
	public static List<PushAppBean> getAppList() {
		if (mAppList == null) {
			mAppList = new ArrayList<PushAppBean>();
		}

		return mAppList;
	}

	/**
	 * 添加首页推荐的7个应用（最多7）
	 * 
	 * @param bean
	 */
	public static void addAppItem(List<PushAppBean> bean) {
		if (mAppList == null) {
			mAppList = new ArrayList<PushAppBean>();
		} else {
			mAppList.clear();
		}
		mAppList.addAll(bean);
	}
	public static void addAppItem(PushAppBean bean) {
		if (mAppList == null) {
			mAppList = new ArrayList<PushAppBean>();
		}
		mAppList.add(bean);
	}

	/**
	 * 是否登录
	 * 
	 * @return
	 */
	public static boolean isLogin() {
		if (StringUtil.notEmpty(getTicket()) && mUser != null && mUser.USERBASIC != null) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 获取访问票据
	 * 
	 * @return
	 */
	public static String getTicket() {
		return mTicket;
	}

	public static void setTicket(String ticket) {
		mTicket = ticket;
	}

	/**
	 * 初始化用户数据
	 */
	public static void initUserInfo() {
		String json = SharedPreferenceUtil.getInstance().getString(AppConst.USER_INFO, "", true);
		String ticket = SharedPreferenceUtil.getInstance().getString(AppConst.ACCESS_TICKET, "", true);
		if (StringUtil.notEmpty(json) && StringUtil.notEmpty(ticket)) {
			mUser = JSON.parseObject(json, UserInfo.class);
			setTicket(ticket);
		} else {
			// mUser = new UserInfo();
		}
	}

	/**
	 * 清除用户数据和状态
	 */
	public static void cleanUserInfo() {
		mUser = null;
		mTicket = null;
		SharedPreferenceUtil.getInstance().setString(AppConst.LAST_LOGIN_DATE, "", false);// 置空登录时间
		SharedPreferenceUtil.getInstance().setString(AppConst.USER_INFO, "", false);
		SharedPreferenceUtil.getInstance().setString(AppConst.ACCESS_TICKET, "", true);
		SharedPreferenceUtil.getInstance().setString(AppConst.THIRDPARTYBIND, "", false);// 第三方绑定信息
		SharedPreferenceUtil.getInstance().setString(AppConst.USER_PHOTO_URL, "", false);
	}

	/**
	 * 销毁数据
	 */
	public static void destroy() {
		mUser = null;
		if (mAppList != null)
			mAppList.clear();
		// mMenuListSpecial.clear();
		if (mPushColumnBean != null)
			mPushColumnBean.clear();
		if (mAllAppList != null)
			mAllAppList.clear();
	}

}