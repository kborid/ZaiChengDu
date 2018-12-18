package com.z012.chengdu.sc.ui.base;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.prj.sdk.app.AppContext;
import com.prj.sdk.util.ActivityTack;
import com.umeng.analytics.MobclickAgent;
import com.z012.chengdu.sc.R;
import com.z012.chengdu.sc.ui.activity.WelcomeActivity;
import com.z012.chengdu.sc.ui.dialog.MyProgressDialog;

/**
 * 基类提供一些共有属性操作
 * 
 * @author LiaoBo
 * 
 */
public abstract class BaseActivity extends AppCompatActivity implements OnClickListener {

	private MyProgressDialog mProgressDialog;
	protected TextView tv_left_title, tv_center_title, tv_right_title;
	protected static String requestID;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// if (VERSION.SDK_INT >= VERSION_CODES.KITKAT) {
		// getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
		// getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
		// }
		if (null != savedInstanceState) {
            Intent intent = new Intent(this, WelcomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
		} else {
            ActivityTack.getInstanse().addActivity(this);
            AppContext.mCurrentContext = this;
        }
	}

	@Override
	protected void onStart() {
		super.onStart();

	}

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
	protected void onStop() {
		super.onStop();

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		ActivityTack.getInstanse().removeActivity(this);
	}

	// 初始化组件
	public void initViews() {
		tv_left_title = (TextView) findViewById(R.id.tv_left_title);
		tv_center_title = (TextView) findViewById(R.id.tv_center_title);
		tv_right_title = (TextView) findViewById(R.id.tv_right_title);
	}

	public void dealIntent() {
	}

	// 参数设置
	public void initParams() {
	}

	// 监听设置
	public void initListeners() {
		if (tv_left_title != null)
			tv_left_title.setOnClickListener(this);
		if (tv_right_title != null)
			tv_right_title.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tv_left_title:
			finish();
			break;
		case R.id.tv_right_title:

			break;
		default:
			break;
		}
	}

	public final void showProgressDialog(String tip, boolean cancelable) {
		showProgressDialog(this, tip, cancelable, null);
	}

	/**
	 * 显示loading对话框
	 */
	public final void showProgressDialog(Context cxt, String tip,
			boolean cancelable, DialogInterface.OnCancelListener mCancel) {
		if (mProgressDialog == null) {
			mProgressDialog = new MyProgressDialog(cxt);
		}
		mProgressDialog.setMessage(tip);
		mProgressDialog.setCanceledOnTouchOutside(false);
		// mProgressDialog.setCancelable(cancelable);
		mProgressDialog.setCancelable(false);
		if (cancelable) {
			mProgressDialog.setOnCancelListener(mCancel);
		}
		mProgressDialog.show();
	}

	public final boolean isProgressShowing() {
		if (mProgressDialog != null) {
			return mProgressDialog.isShowing();
		} else {
			return false;
		}
	}

	/**
	 * 销毁loading对话框
	 */
	public final void removeProgressDialog() {
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
		}
	}

	/**
	 * 通用提示对话框
	 * 
	 * @param tip
	 */
	protected final void showAlertDialog(String tip) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setInverseBackgroundForced(true);
		builder.setMessage(tip);
		builder.setTitle(getString(R.string.alert_title));
		builder.setPositiveButton(getString(R.string.alert_ok),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		builder.create().show();
	}

	protected final void showAlertDialog(String title, String tip) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setInverseBackgroundForced(true);
		builder.setMessage(tip);
		builder.setTitle(title);
		builder.setPositiveButton(getString(R.string.alert_ok),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		builder.create().show();
	}

	/**
	 * 通用提示对话框
	 * 
	 * @param tip
	 */
	protected final void showAlertDialog(String tip,
			DialogInterface.OnClickListener positive) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setInverseBackgroundForced(true);
		builder.setMessage(tip);
		builder.setTitle(getString(R.string.alert_title));
		builder.setPositiveButton(getString(R.string.alert_ok), positive);
		builder.create().show();
	}

	/**
	 * 通用提示对话框
	 * 
	 * @param tip
	 */
	public final void showAlertDialog(String tip,
			DialogInterface.OnClickListener positive,
			DialogInterface.OnClickListener negative) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setInverseBackgroundForced(true);
		builder.setMessage(tip);
		builder.setTitle(getString(R.string.alert_title));
		builder.setPositiveButton(getString(R.string.alert_ok), positive);
		builder.setNegativeButton(getString(R.string.alert_cancel), negative);
		builder.create().show();
	}

	// public void startAnim() {
	// overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
	// }
	//
	// public void finishAnim() {
	// overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
	// }

}
