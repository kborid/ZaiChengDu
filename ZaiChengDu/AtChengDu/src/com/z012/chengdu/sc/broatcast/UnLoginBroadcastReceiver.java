package com.z012.chengdu.sc.broatcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.prj.sdk.app.AppContext;
import com.prj.sdk.constants.Const;
import com.z012.chengdu.sc.app.SessionContext;
import com.z012.chengdu.sc.constants.AppConst;
import com.z012.chengdu.sc.ui.activity.LoginActivity;
import com.z012.chengdu.sc.ui.dialog.CustomDialogUtil;
import com.z012.chengdu.sc.ui.dialog.CustomDialogUtil.onCallBackListener;

/**
 * 未登录广播
 * 
 * @author LiaoBo
 * 
 */
public class UnLoginBroadcastReceiver extends BroadcastReceiver {

	public static final String	ACTION_NAME			= Const.UNLOGIN_ACTION;

	@Override
	public void onReceive(Context context, Intent intent) {

		String action = intent.getAction();
		if (!ACTION_NAME.equals(action)) {
			return;
		}
		boolean is_show_tip_dialog = intent.getBooleanExtra(Const.IS_SHOW_TIP_DIALOG, false);
		if (is_show_tip_dialog) {
			showTip(AppContext.mCurrentContext);
		} else {
			intentLogin(AppContext.mCurrentContext);
		}

	}
	/**
	 * 显示提示
	 * 
	 * @param context
	 */
	public void showTip(final Context context) {

		String msg = "";
		if (SessionContext.isLogin()) {
			msg = "登录信息过期，是否重新登录？";
		} else {
			msg = "您还没有登录，是否立即登录？";
		}
		// 注销登录状态
		SessionContext.cleanUserInfo();

		CustomDialogUtil dlg = new CustomDialogUtil(context);
		dlg.setBtnText("取消", "登录");
		dlg.show(msg);
		dlg.setCanceled(false);
		dlg.setListeners(new onCallBackListener() {

			@Override
			public void rightBtn(CustomDialogUtil dialog) {
				intentLogin(context);
				dialog.dismiss();
			}

			@Override
			public void leftBtn(CustomDialogUtil dialog) {
				Intent mIntent = new Intent(AppConst.ACTION_DYNAMIC_USER_INFO);
				LocalBroadcastManager.getInstance(AppContext.mMainContext).sendBroadcast(mIntent);
				dialog.dismiss();
			}
		});
	}

	/**
	 * 跳转登录页面
	 */
	public void intentLogin(Context context) {
		// 跳出到登录页面
		Intent mIntent = new Intent(context, LoginActivity.class);
		mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(mIntent);
	}
	
}
