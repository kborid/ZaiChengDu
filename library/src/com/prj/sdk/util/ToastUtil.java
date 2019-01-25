package com.prj.sdk.util;

import android.widget.Toast;

import com.prj.sdk.app.AppContext;

/**
 * Toast显示内容 解决Toast叠加显示
 *
 * @author kborid
 */
public class ToastUtil {
    private static Toast mToast;

    /**
     * 关闭显示
     */
    public static void cancel() {
        if (mToast != null) {
            mToast.cancel();
        }
    }

    /**
     * 显示Toast
     *
     * @param msg
     * @param duration
     */
    public static void show(CharSequence msg, int duration) {
        cancel();
        mToast = Toast.makeText(AppContext.mMainContext, msg, duration);
        mToast.setDuration(duration);
        mToast.setText(msg);
        mToast.show();
    }
}