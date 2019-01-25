package com.z012.chengdu.sc.ui.permission;

import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;

import com.prj.sdk.app.AppContext;

/**
 * 检查权限的工具类
 *
 * @auth kborid
 * @date 2017/9/21.
 */
public class PermissionsChecker {

    private static final String TAG = PermissionsChecker.class.getSimpleName();

    // 判断权限集合
    public static boolean lackPermissions(String... permissions) {
        boolean hasLack = false;
        for (String permission : permissions) {
            if (lackPermission(permission)) {
                hasLack = true;
                break;
            }
        }
        return hasLack;
    }

    // 判断是否缺少权限
    private static boolean lackPermission(String permission) {
        return ContextCompat.checkSelfPermission(AppContext.mMainContext, permission) == PackageManager.PERMISSION_DENIED;
    }
}
