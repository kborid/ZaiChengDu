package com.z012.chengdu.sc.constants;

import com.prj.sdk.app.AppContext;
import com.prj.sdk.util.LogUtil;
import com.z012.chengdu.sc.R;

/**
 * 项目常量
 * 
 * @author LiaoBo
 */
public final class AppConst {
	public static final boolean	ISDEVELOP					= LogUtil.isDebug();									// 开发者模式

	public static final String	APPTYPE						= "type";												// 类型：0代表UAT，1代表生产

	public static final String	MAIN_IMG_DATA				= "MAIN_IMG_DATA";										// 首页图片缓存
	public static final String	LAST_USE_VERSIONCODE		= "LAST_USE_VERSIONCODE";								// 上一次使用的版本号
	public static final String	ACCESS_TICKET				= "accessTicket";										// 记录用户登录ticket
	public static final String	USERNAME					= "username";											// 用户名
	public static final String	LAST_LOGIN_DATE				= "LAST_LOGIN_DATE";									// 上次登录时间
	public static final String	USER_PHOTO_URL				= "user_photo_url";									// 用户头像地址
	public static final String	USER_INFO					= "user_info";											// 用户信息
	public static final String	ADVERTISEMENT_INFO			= "ad_info";											// 广告信息
	public static final String	APP_INFO					= "app _info";											// app信息
	
	public static final String 	ABOUT_ICON					= "about_icon";
	public static final String	ABOUT_US					= "about_us";
	public static final String	REGISTER_AGEMENT			= "register_agement";
	public static final String	PROBLEM						= "problem";
	public static final String	IDENTITY_PROTOCOL			= "identity_protocol";

	public static final String	APPID						= AppContext.mMainContext.getString(R.string.appId);
	public static final String	VERSION						= "2.0";
	public static final String	APPKEY						= AppContext.mMainContext.getString(R.string.appKey);
	public static final String	COUNT						= "20";												// 分页加载数量
	public static final String	PUSH_ENABLE					= "PUSH_ENABLE";										// 是否开启消息推送
	public static final String	THIRDPARTYBIND				= "ThirdPartyBind";									// 第三方账号绑定列表

	public static final int		ACTIVITY_IMAGE_CAPTURE		= 100001;
	public static final int		ACTIVITY_GET_IMAGE			= 100002;
	public static final int		ACTIVITY_TAILOR				= 100003;
	// 广播
	public static final String	ACTION_DYNAMIC_USER_INFO	= "ACTION_DYNAMIC_USER_INFO";							// 刷新用户信息
	public static final String	ACTION_PAY_STATUS			= "ACTION_PAY_STATUS";									// 支付状态
}
