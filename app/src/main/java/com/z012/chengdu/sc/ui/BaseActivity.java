package com.z012.chengdu.sc.ui;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.prj.sdk.util.ActivityTack;
import com.umeng.analytics.MobclickAgent;
import com.z012.chengdu.sc.R;
import com.z012.chengdu.sc.ui.activity.WelcomeActivity;
import com.z012.chengdu.sc.ui.dialog.MyProgressDialog;

import butterknife.ButterKnife;

/**
 * 基类提供一些共有属性操作
 *
 * @author kborid
 */
public abstract class BaseActivity extends AppCompatActivity implements IBaseView {

    private MyProgressDialog mProgressDialog;
    protected TextView tv_left_title, tv_center_title, tv_right_title;
    protected static String requestID;

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 延伸显示区域到刘海
//        Window window = getWindow();
//        WindowManager.LayoutParams lp = window.getAttributes();
//        lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
//        window.setAttributes(lp);

        super.onCreate(savedInstanceState);
        if (null != savedInstanceState) {
            WelcomeActivity.startWelcomeActivity();
        } else {
            ActivityTack.getInstanse().addActivity(this);
        }
        setContentView(getLayoutResId());
        ButterKnife.bind(this);
        initParams();
        initListeners();
    }

    protected abstract int getLayoutResId();

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this); // 统计时长
    }

    @Override
    protected void onPause() {
        super.onPause();
        removeProgressDialog();// pause时关闭加载框
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityTack.getInstanse().removeActivity(this);
        if (null != mProgressDialog) {
            mProgressDialog = null;
        }
    }

    protected void initParams() {
        tv_left_title = (TextView) findViewById(R.id.tv_left_title);
        tv_center_title = (TextView) findViewById(R.id.tv_center_title);
        tv_right_title = (TextView) findViewById(R.id.tv_right_title);
        dealIntent();
    }

    protected void dealIntent() {
    }

    protected void initListeners() {
        if (null != tv_left_title) {
            tv_left_title.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }
    }

    @Override
    public void showProgressDialog(String msg, boolean isCancelable) {
        if (null == mProgressDialog) {
            mProgressDialog = new MyProgressDialog(this);
        }
        mProgressDialog.setMessage(msg);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }

    @Override
    public boolean isProgressShowing() {
        return null != mProgressDialog && mProgressDialog.isShowing();
    }

    @Override
    public void removeProgressDialog() {
        if (null != mProgressDialog) {
            mProgressDialog.dismiss();
        }
    }
}
