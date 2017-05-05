package com.prj.sdk.constants;

import com.prj.sdk.app.AppContext;

/**
 * 项目常量
 * 
 * @author Liao
 * 
 */
public final class Const {

	// ---------------------解析服务器参数------------------------
	public static final String	TRUE				= "true";				// true字符串
	public static final String	FALSE				= "false";

	// ---------------------更新控制-------------------
	public static final int		UPDATE_NO			= 0;					// 不更新 已经是最新版本
	public static final int		UPDATE_KEXUAN		= 1;					// 可选更新
	public static final int		UPDATE_QIANZHI		= 2;					// 强制更新
	public static final int		UPDATE_NOTGET		= 3;					// 未获得更新信息
	public static int			UPDATE_CONTROL		= UPDATE_NOTGET;
	public static final String	VERSION				= "VERSION";			// 当前版本号
	public static final String	INSTRUCTIONS		= "INSTRUCTIONS";		// 版本说明

	public static final String	UNLOGIN_ACTION		= "unlogin " + AppContext.mMainContext.getPackageName(); // 未登录广播
	public static final String	IS_SHOW_TIP_DIALOG	= "is_show_tip_dialog"; // 是否显示登录提示对话框
	
	// --------------------项目区分常量----------
	public static final String CHENGDU = "chengdu";
	public static final String CHONGQING = "chongqing";
	public static final String GUIYANG = "guiyang";
}
