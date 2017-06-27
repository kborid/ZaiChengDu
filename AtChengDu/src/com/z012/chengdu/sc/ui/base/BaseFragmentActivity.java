package com.z012.chengdu.sc.ui.base;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.prj.sdk.app.AppContext;
import com.prj.sdk.util.ActivityTack;

/**
 * FragmentActivity 基类提供公共属性
 * 
 * @author LiaoBo
 */
public abstract class BaseFragmentActivity extends FragmentActivity implements OnCheckedChangeListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActivityTack.getInstanse().addActivity(this);
		AppContext.mCurrentContext = this;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		ActivityTack.getInstanse().removeActivity(this);
	}

	public void onResume() {
		super.onResume();
		// MobclickAgent.onResume(this); //统计时长
	}

	public void onPause() {
		super.onPause();
		// removeProgressDialog();// pause时关闭加载框
		// MobclickAgent.onPause(this);
	}

	public void initViews() {
	}

	public void dealIntent() {
	}

	// 参数设置
	public void initParams() {
	}

	// 监听设置
	public void initListeners() {
	}

}
