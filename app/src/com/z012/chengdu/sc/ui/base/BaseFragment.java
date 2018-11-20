package com.z012.chengdu.sc.ui.base;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.prj.sdk.util.LogUtil;
import com.umeng.analytics.MobclickAgent;
import com.z012.chengdu.sc.ui.dialog.MyProgressDialog;

/**
 * fragment基类，提供公共属性
 * 
 * @author LiaoBo
 */
public abstract class BaseFragment extends Fragment implements OnClickListener {
	private MyProgressDialog mProgressDialog;
	protected static String requestID;
	/** Fragment当前状态是否可见 */
	protected boolean isVisible;
	private boolean isPrepared = false;

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);

		if (isResumed()) {
			onVisibilityChangedToUser(isVisibleToUser, true);
		}

		if (isPrepared) {
			isPrepared = false;
			onInits();
		}
	}

	/**
	 * 可见
	 */
	protected void onVisible() {
	}

	/**
	 * 不可见
	 */
	protected void onInvisible() {
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		isPrepared = true;
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	/**
	 * 初始化方法，只有在显示时才会有且只执行一次
	 */
	protected abstract void onInits();

	protected void initViews(View view) {
	};

	public void dealIntent() {
	}

	// 参数设置
	protected void initParams() {
	}

	protected void initListeners() {
	};

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onResume() {
		super.onResume();
		if (getUserVisibleHint()) {
			onVisibilityChangedToUser(true, false);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		if (getUserVisibleHint()) {
			onVisibilityChangedToUser(false, false);
		}
	}

	/**
	 * 当Fragment对用户的可见性发生了改变的时候就会回调此方法
	 * 
	 * @param isVisibleToUser
	 *            true：用户能看见当前Fragment；false：用户看不见当前Fragment
	 * @param isHappenedInSetUserVisibleHintMethod
	 *            true：本次回调发生在setUserVisibleHintMethod方法里；false：
	 *            发生在onResume或onPause方法里
	 */
	public void onVisibilityChangedToUser(boolean isVisibleToUser,
			boolean isHappenedInSetUserVisibleHintMethod) {
		if (isVisibleToUser) {
			isVisible = true;
			onVisible();
			MobclickAgent.onPageStart(this.getClass().getName());
			LogUtil.d(
					getClass().getSimpleName(),
					" - display - "
							+ (isHappenedInSetUserVisibleHintMethod ? "setUserVisibleHint"
							: "onResume"));
		} else {
			isVisible = false;
			onInvisible();
			MobclickAgent.onPageEnd(this.getClass().getName());
			LogUtil.d(
					getClass().getSimpleName(),
					" - hidden - "
							+ (isHappenedInSetUserVisibleHintMethod ? "setUserVisibleHint"
							: "onPause"));
		}
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onDetach() {
		super.onDetach();
	}

	public final void showProgressDialog(String tip, boolean cancelable) {
		showProgressDialog(getActivity(), tip, cancelable, null);
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

}
