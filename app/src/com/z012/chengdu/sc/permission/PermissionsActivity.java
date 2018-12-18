package com.z012.chengdu.sc.permission;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;

import com.prj.sdk.util.LogUtil;
import com.z012.chengdu.sc.R;
import com.z012.chengdu.sc.app.PRJApplication;
import com.z012.chengdu.sc.ui.activity.WelcomeActivity;
import com.z012.chengdu.sc.ui.dialog.CustomDialog;

/**
 * 权限获取页面
 */
public class PermissionsActivity extends AppCompatActivity {

    private static final String TAG = "Permission";

    private static final int PERMISSION_REQUEST_CODE = 0; //系统权限管理页面的参数
    private static final String EXTRA_PERMISSIONS = "com.kborid.permissiontest.extra_permission"; //权限参数
    private static final String PACKAGE_URL_SCHEME = "package:"; // 方案

    private boolean isRequireCheck; //是否需要系统权限检测

    //启动当前权限页面的公开接口
    public static void startActivityForResult(Activity activity, int requestCode, String... permissions) {
        LogUtil.i(TAG, "startActivityForResult()");
        Intent intent = new Intent(activity, PermissionsActivity.class);
        intent.putExtra(EXTRA_PERMISSIONS, permissions);
        ActivityCompat.startActivityForResult(activity, intent, requestCode, null);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.user_login_enter_in, R.anim.user_login_enter_out);
        if (getIntent() == null || !getIntent().hasExtra(EXTRA_PERMISSIONS)) {
            throw new RuntimeException("PermissionsActivity需要使用静态startActivityForResult方法启动!");
        }
        if (null != savedInstanceState) {
            LogUtil.i(TAG, "restart LauncherActivity");
            Intent intent = new Intent(this, WelcomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
        isRequireCheck = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isRequireCheck) {
            String[] permissions = getPermissions();
            if (PRJApplication.getPermissionsChecker(this).lacksPermissions(permissions)) {
                requestPermissions(permissions); //请求权限
            } else {
                allPermissionsGranted(); //全部权限都已获取
            }
        } else {
            isRequireCheck = true;
        }
    }

    //返回传递的权限参数
    private String[] getPermissions() {
        return getIntent().getStringArrayExtra(EXTRA_PERMISSIONS);
    }

    //请求权限兼容低版本
    private void requestPermissions(String... permissions) {
        LogUtil.i(TAG, "requestPermissions()");
        ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
    }

    //全部权限均已获取
    private void allPermissionsGranted() {
        LogUtil.i(TAG, "allPermissionsGranted()");
        setResult(PermissionsDef.PERMISSIONS_GRANTED);
        finish();
    }

    /**
     * 用户权限处理,
     * 如果全部获取, 则直接过.
     * 如果权限缺失, 则提示Dialog.
     *
     * @param requestCode  请求码
     * @param permissions  权限
     * @param grantResults 结果
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        LogUtil.i(TAG, "onRequestPermissionsResult()");
        if (requestCode == PERMISSION_REQUEST_CODE && hasAllPermissionsGranted(grantResults)) {
            isRequireCheck = true;
            allPermissionsGranted();
        } else {
            isRequireCheck = false;
            showMissingPermissionDialog();
        }
    }

    //含有全部的权限
    private boolean hasAllPermissionsGranted(@NonNull int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }

    //显示缺失权限提示
    private void showMissingPermissionDialog() {
        LogUtil.i(TAG, "showMissingPermissionDialog()");
        CustomDialog mDialog = new CustomDialog(this);
        mDialog.setTitle("提示");
        String msg = String.format("“%1$s”缺少必要权限。\n\n请点击“设置”-“权限”打开所需权限。", getResources().getString(R.string.app_name));
        // 拒绝, 退出应用
        mDialog.setBtnText("关闭", "设置");
        mDialog.setListeners(new CustomDialog.onCallBackListener() {
            @Override
            public void leftBtn(CustomDialog dialog) {
                setResult(PermissionsDef.PERMISSIONS_DENIED);
                finish();
            }

            @Override
            public void rightBtn(CustomDialog dialog) {
                startAppSettings();
            }
        });
        mDialog.setCancelable(false);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show(msg);
    }

    //启动应用的设置
    private void startAppSettings() {
        LogUtil.i(TAG, "startAppSettings()");
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse(PACKAGE_URL_SCHEME + getPackageName()));
        startActivity(intent);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.user_login_exit_in, R.anim.user_login_exit_out);
    }
}

