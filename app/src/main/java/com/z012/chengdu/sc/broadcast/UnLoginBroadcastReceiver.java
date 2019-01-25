package com.z012.chengdu.sc.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.content.LocalBroadcastManager;
import android.view.WindowManager;

import com.prj.sdk.app.AppContext;
import com.z012.chengdu.sc.SessionContext;
import com.z012.chengdu.sc.constants.AppConst;
import com.z012.chengdu.sc.ui.dialog.CustomDialog;
import com.z012.chengdu.sc.ui.dialog.CustomDialog.onCallBackListener;

/**
 * 未登录广播
 *
 * @author LiaoBo
 */
public class UnLoginBroadcastReceiver extends BroadcastReceiver {

    public static final String NEED_LOGIN = "needLogin";

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();
        if (!AppConst.ACTION_UNLOGIN.equals(action)) {
            return;
        }
        boolean needShowUnLoginDialog = intent.getBooleanExtra(NEED_LOGIN, false);
        if (needShowUnLoginDialog) {
            showUnLoginDialog(context);
        } else {
            intentLogin(context);
        }
    }

    /**
     * 显示提示
     *
     * @param context
     */
    public void showUnLoginDialog(final Context context) {
        // 注销登录状态
        SessionContext.cleanUserInfo();
        String msg = SessionContext.isLogin() ? "登录信息过期，是否重新登录？" : "您还没有登录，是否立即登录？";
        CustomDialog dlg = new CustomDialog(context);
        WindowManager.LayoutParams lp = dlg.getWindow().getAttributes();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            lp.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            lp.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }
        dlg.getWindow().setAttributes(lp);
        dlg.setBtnText("取消", "登录");
        dlg.show(msg);
        dlg.setCanceled(false);
        dlg.setListeners(new onCallBackListener() {

            @Override
            public void rightBtn(CustomDialog dialog) {
                intentLogin(context);
                dialog.dismiss();
            }

            @Override
            public void leftBtn(CustomDialog dialog) {
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
        Intent intent = new Intent("com.z012.sc.action.Login");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

}
