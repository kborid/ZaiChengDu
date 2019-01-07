package com.z012.chengdu.sc.ui.base;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
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
 * 
 */
public abstract class BaseActivity extends AppCompatActivity implements OnClickListener {

	private MyProgressDialog mProgressDialog;
    protected TextView tv_left_title, tv_center_title, tv_right_title;
	protected static String requestID;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (null != savedInstanceState) {
            Intent intent = new Intent(this, WelcomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
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
		MobclickAgent.onPageStart(this.getClass().getName()); // 统计页面(仅有Activity的应用中SDK自动调用，不需要单独写。"SplashScreen"为页面名称，可自定义)
		MobclickAgent.onResume(this); // 统计时长
	}

	@Override
	protected void onPause() {
		super.onPause();
		removeProgressDialog();// pause时关闭加载框
		MobclickAgent.onPageEnd(this.getClass().getName());
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
    public void onClick(View v) {
    }

    public final void showProgressDialog(String tip, boolean isCancelable) {
		if (null == mProgressDialog) {
			mProgressDialog = new MyProgressDialog(this);
		}
		mProgressDialog.setMessage(tip);
		mProgressDialog.setCanceledOnTouchOutside(false);
		mProgressDialog.setCancelable(false);
		mProgressDialog.show();
	}

    public final boolean isProgressShowing() {
		return null != mProgressDialog && mProgressDialog.isShowing();
	}

    public final void removeProgressDialog() {
		if (null != mProgressDialog) {
			mProgressDialog.dismiss();
		}
	}
}
